package com.xinchuang.diagnosis.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xinchuang.diagnosis.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

class JwtUtilTest {
    private final JwtUtil jwtUtil = new JwtUtil(new ObjectMapper(), "test-secret-for-junit-at-least-32-characters", 3600);

    @Test
    void generatedTokenCanBeParsedIntoOriginalClaims() {
        String token = jwtUtil.generateToken(42L, "tester");

        JwtUtil.JwtClaims claims = jwtUtil.parseToken(token);

        assertEquals(42L, claims.getUserId());
        assertEquals("tester", claims.getUsername());
    }

    @Test
    void malformedTokenIsRejectedAsUnauthorized() {
        BusinessException exception = assertThrows(BusinessException.class, () -> jwtUtil.parseToken("invalid-token"));

        assertEquals(401, exception.getCode());
    }

    @Test
    void rejectsMissingOrWeakSigningSecret() {
        ObjectMapper objectMapper = new ObjectMapper();
        assertThrows(IllegalStateException.class, () -> new JwtUtil(objectMapper, "", 3600));
        assertThrows(IllegalStateException.class, () -> new JwtUtil(objectMapper, "short-secret", 3600));
    }
}
