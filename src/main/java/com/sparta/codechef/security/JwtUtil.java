//package com.sparta.codechef.security;
//
//import com.sparta.codechef.common.enums.TokenType;
//import com.sparta.codechef.common.enums.UserRole;
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.JwtParser;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.security.Keys;
//import jakarta.annotation.PostConstruct;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
//
//import javax.crypto.SecretKey;
//import java.net.URLEncoder;
//import java.nio.charset.StandardCharsets;
//import java.util.Date;
//
//@Slf4j(topic = "JWT Util")
//@Component
//public class JwtUtil {
//
//    // Header KEY 값
//    public static final String AUTHORIZATION_HEADER = "Authorization";
//    public static final String REFRESH_TOKEN_HEADER = "Refresh-Token";
//    public static final String REDIS_REFRESH_TOKEN_PREFIX = "Refresh_";
//    // 사용자 권한 값의 KEY
//    public static final String AUTHORIZATION_KEY = "auth";
//    // Token 식별자
//    public static final String BEARER_PREFIX = "Bearer ";
//
//    @Value("${JWT_SECRET_KEY}")
//    private String secretKey;
//
//    private SecretKey key;
//
////    @PostConstruct
////    private void init() {
////        // 키 설정
////        key = getSecretKeyFromBase64(secretKey);
////    }
//
//    public void addTokenToHeader(HttpServletResponse response, String token) {
//        token = URLEncoder.encode(token, StandardCharsets.UTF_8)
//                .replaceAll("\\+", "%20");
//
//        response.addHeader(AUTHORIZATION_HEADER, token);
//    }
//
//    public boolean canSubstringToken(String token) {
//        if (StringUtils.hasText(token) && token.startsWith(BEARER_PREFIX)) {
//            return true;
//        }
//
//        return false;
//    }
//    public String substringToken(String token) {
//        if (StringUtils.hasText(token) && token.startsWith(BEARER_PREFIX)) {
//            return token.substring(BEARER_PREFIX.length());
//        }
//
//        log.error("Not Found Token");
//        throw new NullPointerException("Not Found Token");
//    }
//
////    public String getUserId(String token) {
////        return getJwtParser().parseSignedClaims(token)
////                .getPayload().getSubject();
////    }
////
////    public String getRole(String token) {
////
////        return getJwtParser().parseSignedClaims(token)
////                .getPayload()
////                .get(AUTHORIZATION_KEY, String.class);
////    }
////
////    public boolean isExpired(String token) {
////        return getJwtParser().parseSignedClaims(token)
////                .getPayload()
////                .getExpiration()
////                .before(new Date());
////    }
////
////    private JwtParser getJwtParser() {
////        return Jwts.parser()
////                .verifyWith(key)
////                .build();
////    }
////
////    private SecretKey getSecretKeyFromBase64(String base64) {
////        return Keys.hmacShaKeyFor(Base64Coder.decode(base64));
////    }
////
////    public String getCategory(String token) {
////        return getJwtParser().parseSignedClaims(token)
////                .getPayload()
////                .get("category", String.class);
////    }
////
////
////    public String createAccessToken(Long userId, String email, UserRole role) {
////        Date now = new Date();
////        return BEARER_PREFIX + Jwts.builder()
////                .claim("category", TokenType.ACCESS.name())
////                .expiration(new Date(now.getTime() + TokenType.ACCESS.getLifeTime()))
////                .subject(String.valueOf(userId))
////                .claim("email", email)
////                .claim("userRole", role.getUserRole())
////                .issuedAt(now)
////                .signWith(key)
////                .compact();
////    }
////
////    public String createRefreshToken(Long userId, String email,  UserRole role) {
////        Date now = new Date();
////        return BEARER_PREFIX + Jwts.builder()
////                .claim("category", TokenType.REFRESH.name())
////                .expiration(new Date(now.getTime() + TokenType.REFRESH.getLifeTime()))
////                .subject(String.valueOf(userId))
////                .claim("email", email)
////                .claim("userRole", role.getUserRole())
////                .issuedAt(now)
////                .signWith(key)
////                .compact();
////    }
////
////    public Claims extractClaims(String token) {
////        return getJwtParser().parseSignedClaims(token).getPayload();
////    }
//}
