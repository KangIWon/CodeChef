package com.sparta.codechef.security;

import com.sparta.codechef.common.ErrorStatus;
import com.sparta.codechef.common.enums.TokenType;
import com.sparta.codechef.common.enums.UserRole;
import com.sparta.codechef.common.exception.ApiException;
import com.sparta.codechef.domain.chat.v2.entity.WSChatUser;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import javax.crypto.SecretKey;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j(topic = "JWT Util")
@Component
public class JwtUtil {

    // Header KEY 값
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String REFRESH_TOKEN_HEADER = "Refresh-Token";
    public static final String REDIS_REFRESH_TOKEN_PREFIX = "Refresh_";
    // 사용자 권한 값의 KEY
    public static final String AUTHORIZATION_KEY = "auth";
    // Token 식별자
    public static final String BEARER_PREFIX = "Bearer ";

    @Value("${JWT_SECRET_KEY}")
    private String secretKey;

    private SecretKey key;

    @PostConstruct
    private void init() {
        // 키 설정
        key = getSecretKeyFromBase64(secretKey);
    }

    public void addTokenToHeader(HttpServletResponse response, String token) {
        try {
            token = URLEncoder.encode(token, StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");
            response.addHeader(AUTHORIZATION_HEADER, token);
        } catch (Exception e) {
            log.error("Failed to encode token", e);
        }
    }

    public boolean canSubstringToken(String token) {
        return StringUtils.hasText(token) && token.startsWith(BEARER_PREFIX);
    }

    public String substringToken(String token) {
        if (StringUtils.hasText(token) && token.startsWith(BEARER_PREFIX)) {
            return token.substring(BEARER_PREFIX.length());
        }

        log.error("Not Found Token");
        throw new IllegalArgumentException("Not Found Token");
    }

    public String getUserId(String token) {
        try {
            return extractClaims(token).getSubject();
        } catch (Exception e) {
            log.error("Failed to extract user ID", e);
            throw new IllegalArgumentException("Invalid token");
        }
    }

    public String getRole(String token) {
        try {
            return extractClaims(token).get(AUTHORIZATION_KEY, String.class);
        } catch (Exception e) {
            log.error("Failed to extract role", e);
            throw new IllegalArgumentException("Invalid token");
        }
    }

    public boolean isExpired(String token) {
        try {
            Date expiration = extractClaims(token).getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            log.error("Failed to check token expiration", e);
            throw new IllegalArgumentException("Invalid token");
        }
    }

    private JwtParser getJwtParser() {
        return Jwts.parserBuilder().setSigningKey(key).build();
    }

    private SecretKey getSecretKeyFromBase64(String base64) {
        try {
            return Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64));
        } catch (Exception e) {
            log.error("Failed to decode secret key", e);
            throw new IllegalArgumentException("Invalid secret key");
        }
    }

    public String getCategory(String token) {
        try {
            return extractClaims(token).get("category", String.class);
        } catch (Exception e) {
            log.error("Failed to extract category", e);
            throw new IllegalArgumentException("Invalid token");
        }
    }

    public String createAccessToken(Long userId, String email, UserRole role) {
        Date now = new Date();
        return BEARER_PREFIX + Jwts.builder()
                .claim("category", TokenType.ACCESS.name())
                .setExpiration(new Date(now.getTime() + TokenType.ACCESS.getLifeTime()))
                .setSubject(String.valueOf(userId))
                .claim("email", email)
                .claim("userRole", role.getUserRole())
                .setIssuedAt(now)
                .signWith(key)
                .compact();
    }

    public String createRefreshToken(Long userId, String email, UserRole role) {
        Date now = new Date();
        return BEARER_PREFIX + Jwts.builder()
                .claim("category", TokenType.REFRESH.name())
                .setExpiration(new Date(now.getTime() + TokenType.REFRESH.getLifeTime()))
                .setSubject(String.valueOf(userId))
                .claim("email", email)
                .claim("userRole", role)
                .setIssuedAt(now)
                .signWith(key)
                .compact();
    }

    public Claims extractClaims(String token) {
        return getJwtParser().parseClaimsJws(token).getBody();
    }

    public AuthUser getAuthUserFromToken(String token) {
        Claims claims = extractClaims(token);

        return new AuthUser(
                Long.parseLong(claims.getSubject()),
                claims.get("email", String.class),
                UserRole.of(claims.get("userRole", String.class))
        );
    }

    public AuthUser validateToken(String token) {
        try {
            return this.getAuthUserFromToken(token);

        } catch (SecurityException | MalformedJwtException e) {
            log.error("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.", e);
            throw new ApiException(ErrorStatus.UNAUTHORIZED_INVALID_TOKEN);
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token, 만료된 JWT token 입니다.", e);
            throw new ApiException(ErrorStatus.UNAUTHORIZED_EXPIRED_TOKEN);
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.", e);
            throw new ApiException(ErrorStatus.BAD_REQUEST_UNSUPPORTED_TOKEN);
        } catch (Exception e) {
            log.error("Internal server error", e);
            throw new ApiException(ErrorStatus.FAILED_TO_AUTHORIZE_USER);
        }
    }
}
