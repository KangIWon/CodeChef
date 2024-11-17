package com.sparta.codechef.domain.payment.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.exception.ApiException;
import com.sparta.codechef.domain.payment.dto.request.CancelSubscriptionRequest;
import com.sparta.codechef.domain.payment.entity.BillingKey;
import com.sparta.codechef.domain.payment.repository.BillingKeyRepository;
import com.sparta.codechef.domain.payment.entity.PaymentHistory;
import com.sparta.codechef.domain.payment.repository.PaymentHistoryRepository;
import com.sparta.codechef.domain.payment.status.BillingKeyStatus;
import com.sparta.codechef.domain.user.entity.User;
import com.sparta.codechef.domain.user.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@Getter
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentHistoryRepository paymentHistoryRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final String secretKey;
    private final ObjectMapper objectMapper;
    private final BillingKeyRepository billingKeyRepository;

    /**
     * 유저 커스터키 가져오는 로직(없으면 생성 후 -> 저장 -> 반환)
     *
     * @param userId
     * @return 커스터머키
     */
    @Transactional
    public String getOrCreateCustomerKey(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND_USER));

        if (user.getCustomerKey() == null || user.getCustomerKey().isEmpty()) {
            // UUID 기반으로 무작위 고객 키 생성, 특수 문자 추가
            // UUID 기반으로 무작위 고객 키 생성 (형식 제한 준수)
            String newCustomerKey = UUID.randomUUID().toString().replace("-", "");

            // 50자 이하로 제한
            if (newCustomerKey.length() > 50) {
                newCustomerKey = newCustomerKey.substring(0, 50);
            }

            user.saveCustomerKey(newCustomerKey);
            userRepository.save(user);
            log.info("Generated new customerKey for userId {}: {}", userId, newCustomerKey);
            return newCustomerKey;
        }
        log.info("Existing customerKey for userId {}: {}", userId, user.getCustomerKey());
        return user.getCustomerKey();
    }

    /**
     * FailUrl로 반환시, 예외 던지는 로직
     *
     * @param code
     * @param message
     */
    public void handleFailure(String code, String message) {
        switch (code) {
            case "PAY_PROCESS_CANCELED":
                logErrorCodeAndMessageBySavingCardFail(code, message);
                throw new ApiException(ErrorStatus.PAYMENT_CANCELED);

            case "PAY_PROCESS_ABORTED":
                logErrorCodeAndMessageBySavingCardFail(code, message);
                throw new ApiException(ErrorStatus.PAYMENT_ABORTED);

            case "REJECT_CARD_COMPANY":
                logErrorCodeAndMessageBySavingCardFail(code, message);
                throw new ApiException(ErrorStatus.CARD_REJECTED);

            default:
                logErrorCodeAndMessageBySavingCardFail(code, message);
                throw new ApiException(ErrorStatus.UNKNOWN_ERROR);
        }
    }

    /**
     * @param customerKey
     * @param authKey
     * @return 빌링키
     */
    @Transactional
    public void processBillingKey(String customerKey, String authKey) {
        User user = userRepository.findByCustomerKey(customerKey)
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND_USER));

        boolean isExist = billingKeyRepository.existsPersonalBillingKeyByUserId(user.getId());
        if (isExist) {
            // 기존 BillingKey 업데이트
            BillingKey billingKey = billingKeyRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND_BILLING_KEY));
            String userBillingKey = requestBillingKey(user, authKey);
            billingKey.updateBillingKey(userBillingKey);
            // 변경된 사항은 트랜잭션 커밋 시 자동으로 저장됩니다.
        } else {
            // 새 BillingKey 생성 및 저장
            String userBillingKey = requestBillingKey(user, authKey);
            BillingKey newBillingKey = BillingKey.builder()
                    .personalBillingKey(userBillingKey)
                    .user(user)
                    .status(BillingKeyStatus.ACTIVE)
                    .billingDate(LocalDate.now()) // 필요 시 추가 필드 설정
                    .build();
            billingKeyRepository.save(newBillingKey);
        }
    }

    public String requestBillingKey(User user, String authKey) {
        String customerKey = user.getCustomerKey();

        // Base64 인코딩
        String encodingSecretKey = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + encodingSecretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 요청 바디 설정
        Map<String, String> body = new HashMap<>();
        body.put("authKey", authKey);
        body.put("customerKey", customerKey);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);

        try {
            // API 호출
            ResponseEntity<Map> responseEntity = restTemplate.postForEntity(
                    "https://api.tosspayments.com/v1/billing/authorizations/issue",
                    requestEntity,
                    Map.class
            );

            // 응답 로그 추가
            log.info("Toss API Response Status: {}", responseEntity.getStatusCode());
            log.info("Toss API Response Body: {}", responseEntity.getBody());

            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = responseEntity.getBody();
                if (responseBody == null) {
                    log.error("빌링키 발급 실패: 응답 바디가 null입니다.");
                    throw new ApiException(ErrorStatus.PAYMENT_FAILED);
                }

                String userBillingKey = (String) responseBody.get("billingKey");

                if (userBillingKey == null || userBillingKey.isEmpty()) {
                    log.error("빌링키가 응답에 포함되지 않았습니다.");
                    throw new ApiException(ErrorStatus.BILLING_KEY_IS_NULL);
                }

                return userBillingKey;
            } else {
                // 에러 응답 처리
                log.error("빌링키 발급 실패: {}", responseEntity.getBody());
                throw new ApiException(ErrorStatus.PAYMENT_FAILED);
            }
        } catch (RestClientException e) {
            // RestTemplate 관련 예외 처리
            log.error("빌링키 발급 중 RestClientException 발생: {}", e.getMessage());
            throw new ApiException(ErrorStatus.PAYMENT_FAILED);
        } catch (Exception e) {
            // 기타 예외 처리
            log.error("빌링키 발급 중 오류 발생: {}", e.getMessage());
            throw new ApiException(ErrorStatus.PAYMENT_FAILED);
        }
    }


    /**
     * 결제 요청 로직
     *
     * @param userId 유저 ID
     * @param amount 결제 금액
     */
    @Transactional
    @Retryable(
            value = {RestClientException.class, ApiException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public void processPayment(Long userId, int amount) {

        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND_USER));

        // 결제 정보 조회
        BillingKey billingkey = billingKeyRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND_BILLING_KEY));
        String userBillingKey = billingkey.getPersonalBillingKey();
        String customerKey = user.getCustomerKey();

        if (userBillingKey == null) {
            throw new ApiException(ErrorStatus.BILLING_KEY_IS_NULL);
        }

        String orderId = generateOrderId();
        String orderName = "plus 유저 결제";

        // PaymentHistory 생성 (초기 상태는 PENDING)
        PaymentHistory paymentHistory = PaymentHistory.builder()
                .orderId(orderId)
                .amount(amount)
                .orderName(orderName)
                .approveAt(null) // 초기에는 null로 설정
                .status("PENDING")
                .billingKey(billingkey)
                .build();

        // 인증 헤더 설정
        String encodedSecretKey = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + encodedSecretKey);
        headers.add("Idempotency-Key", UUID.randomUUID().toString());
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 요청 바디 설정
        Map<String, Object> body = new HashMap<>();
        body.put("customerKey", customerKey);
        body.put("amount", amount);
        body.put("orderId", orderId);
        body.put("orderName", orderName);
        body.put("customerEmail", user.getEmail());
        body.put("customerName", user.getUserName());

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            // 자동 결제 API 호출
            String url = "https://api.tosspayments.com/v1/billing/" + userBillingKey;
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(
                    url,
                    requestEntity,
                    String.class
            );

            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                HttpHeaders responseHeaders = responseEntity.getHeaders();
                String responseBody = responseEntity.getBody();
                if (responseBody != null) {
                    // JSON 파싱 유연성 강화
                    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                            false);
                    Map<String, Object> responseMap = objectMapper.readValue(responseBody,
                            Map.class);
                    List<String> idempotencyKeys = responseHeaders.get("Idempotency-Key");
                    if (idempotencyKeys != null && !idempotencyKeys.isEmpty()) {
                        String idempotencyKey = idempotencyKeys.get(0); // 첫 번째 값 가져오기
                        log.info("Idempotency-Key: {}", idempotencyKey);
                        paymentHistory.updateIdempotencyKey(idempotencyKey);
                    }
                    paymentHistory.updateFromTossApprovalResponse(responseMap);
                    LocalDateTime approveAt = paymentHistory.getApproveAt();
                    billingkey.updateScheduledDate(approveAt);
                    paymentHistoryRepository.save(paymentHistory);
                }
            } else {
                // 비정상 응답 처리 (실패 응답)
                String responseBody = responseEntity.getBody();
                if (responseBody != null) {
                    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                            false);
                    Map<String, Object> errorResponse = objectMapper.readValue(responseBody,
                            Map.class);
                    Map<String, Object> error = (Map<String, Object>) errorResponse.get("error");
                    String code = (String) error.get("code");
                    String message = (String) error.get("message");
                    logErrorCodeAndMessageByPaymentFail(code, message);
                    paymentHistory.updateFailureInfo(code, message);
                    paymentHistoryRepository.save(paymentHistory);
                }
                throw new ApiException(ErrorStatus.PAYMENT_FAILED);
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            // HTTP 오류 처리
            String responseBody = e.getResponseBodyAsString();
            log.error("결제 요청 중 오류 발생: {}", responseBody);
            try {
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                Map<String, Object> errorResponse = objectMapper.readValue(responseBody, Map.class);
                Map<String, Object> error = (Map<String, Object>) errorResponse.get("error");
                String code = (String) error.get("code");
                String message = (String) error.get("message");
                paymentHistory.updateFailureInfo(code, message);
            } catch (Exception parsingException) {
                // 파싱 실패 시 기본 에러 코드 및 메시지 설정
                paymentHistory.updateFailureInfo("UNKNOWN_ERROR", "결제 요청 중 예기치 않은 오류가 발생했습니다.");
            }
            paymentHistoryRepository.save(paymentHistory);
            logErrorCodeAndMessageByPaymentFail(e.getStatusCode().toString(),
                    e.getResponseBodyAsString());
            throw new ApiException(ErrorStatus.PAYMENT_FAILED);
        } catch (Exception e) {
            // 기타 예외 처리
            log.error("결제 처리 중 예외 발생: {}", e.getMessage());
            paymentHistory.updateFailureInfo("UNKNOWN_ERROR", e.getMessage());
            paymentHistoryRepository.save(paymentHistory);
            throw new ApiException(ErrorStatus.PAYMENT_FAILED);
        }
    }


    @Transactional
    public void cancelBilling(Long userId, CancelSubscriptionRequest cancelSubscriptionRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND_USER));
        BillingKey billingKey = billingKeyRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND_BILLING_KEY));
        PaymentHistory paymentHistory = paymentHistoryRepository.findLatestByBillingKey(billingKey)
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND_PAYMENT_HISTORY));

        LocalDate paymentDate = paymentHistory.getApproveAt().toLocalDate();
        long daysSincePayment = ChronoUnit.DAYS.between(paymentDate, LocalDate.now());
        int refundAmount = calculateRefundAmount(paymentHistory.getAmount(), daysSincePayment);

        // Toss Payments API에 환불 요청 보내기
        String cancelReason = cancelSubscriptionRequest.getCancelReason();
        String idempotencyKey = paymentHistory.getPaymentIdempotencyKey();
        sendRefundRequest(paymentHistory, refundAmount, cancelReason, idempotencyKey);

        // 환불 후 결제 상태 업데이트
        paymentHistory.updateStatus("CANCELED", null, null);
        billingKey.cancelSubscription();
        paymentHistoryRepository.save(paymentHistory);
    }

    private void sendRefundRequest(PaymentHistory paymentHistory, int cancelAmount,
            String cancelReason, String idempotencyKey) {
        String encodingSecretKey = Base64.getEncoder().encodeToString((secretKey + ":").getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + encodingSecretKey);
        headers.add("Idempotency-Key", idempotencyKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("cancelReason", cancelReason);
        body.put("cancelAmount", cancelAmount);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> responseEntity = restTemplate.postForEntity(
                    "https://api.tosspayments.com/v1/payments/" + paymentHistory.getPaymentKey()
                            + "/cancel",
                    requestEntity,
                    Map.class
            );

            if (responseEntity.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("환불 요청 실패: " + responseEntity.getBody());
            }
        } catch (Exception e) {
            paymentHistory.updateStatus("FAILED", "ERROR_CODE", e.getMessage());
            throw new ApiException(ErrorStatus.REFUND_CANCELED);
        }
    }

    private int calculateRefundAmount(int originalAmount, long daysSincePayment) {
        if (daysSincePayment <= 3) {
            // 3일 이내 전액 환불
            return originalAmount;
        } else if (daysSincePayment <= 15) {
            // 4일부터 15일까지 10%씩 차감하여 환불 금액 계산
            int refundPercentage = 100 - (int) ((daysSincePayment - 3) * 10); // 4일째부터 10%씩 차감
            return (originalAmount * refundPercentage) / 100;
        } else {
            // 16일 이후 환불 불가
            throw new ApiException(ErrorStatus.REFUND_REJECTED);
        }
    }

    // 조건에 맞는 orderId 생성 메서드
    private String generateOrderId() {
        String uuid = UUID.randomUUID().toString().replaceAll("[^a-zA-Z0-9_-]", "");
        String orderId = uuid.substring(0, Math.min(uuid.length(), 64));
        if (orderId.length() < 6) {
            orderId = orderId + "123456";
        }
        return orderId;
    }

    void logErrorCodeAndMessageBySavingCardFail(String code, String message) {
        log.error("카드등록 - 에러 코드: {}, 에러 메세지: {}", code, message);
    }

    void logErrorCodeAndMessageByPaymentFail(String code, String message) {
        log.error("결제 - 에러 코드: {}, 에러 메세지: {}", code, message);
    }
}
