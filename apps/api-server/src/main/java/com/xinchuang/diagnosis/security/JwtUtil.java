package com.xinchuang.diagnosis.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xinchuang.diagnosis.common.exception.BusinessException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JwtUtil {
    private final ObjectMapper objectMapper;
    private final String secret;
    private final long expireSeconds;

    public JwtUtil(ObjectMapper objectMapper,
                   @Value("${security.jwt.secret}") String secret,
                   @Value("${security.jwt.expire-seconds}") long expireSeconds) {
        if (!StringUtils.hasText(secret) || secret.length() < 32) {
            throw new IllegalStateException("JWT_SECRET 必须配置为至少 32 位的随机密钥");
        }
        this.objectMapper = objectMapper;
        this.secret = secret;
        this.expireSeconds = expireSeconds;
    }

    public String generateToken(Long userId, String username) {
        try {
            Map<String, Object> header = new LinkedHashMap<>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("sub", String.valueOf(userId));
            payload.put("username", username);
            payload.put("exp", Instant.now().getEpochSecond() + expireSeconds);
            String headerPart = base64Url(objectMapper.writeValueAsBytes(header));
            String payloadPart = base64Url(objectMapper.writeValueAsBytes(payload));
            String signature = sign(headerPart + "." + payloadPart);
            return headerPart + "." + payloadPart + "." + signature;
        } catch (Exception ex) {
            throw new BusinessException(500, "生成登录凭证失败");
        }
    }

    public JwtClaims parseToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new BusinessException(401, "未登录或登录已过期");
        }
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new BusinessException(401, "登录凭证无效");
            }
            String expected = sign(parts[0] + "." + parts[1]);
            if (!constantTimeEquals(expected, parts[2])) {
                throw new BusinessException(401, "登录凭证无效");
            }
            byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
            Map<String, Object> payload = objectMapper.readValue(payloadBytes, new TypeReference<>() {});
            long exp = Long.parseLong(String.valueOf(payload.get("exp")));
            if (Instant.now().getEpochSecond() >= exp) {
                throw new BusinessException(401, "登录已过期，请重新登录");
            }
            JwtClaims claims = new JwtClaims();
            claims.setUserId(Long.parseLong(String.valueOf(payload.get("sub"))));
            claims.setUsername(String.valueOf(payload.get("username")));
            return claims;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(401, "登录凭证无效");
        }
    }

    private String sign(String content) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return base64Url(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
    }

    private String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private boolean constantTimeEquals(String left, String right) {
        return MessageDigestUtil.equals(left.getBytes(StandardCharsets.UTF_8), right.getBytes(StandardCharsets.UTF_8));
    }

    public static class JwtClaims {
        private Long userId;
        private String username;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }
}
