package com.advisor.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil("test-secret-key-minimum-32-characters-long-for-hs256");
    }

    @Test
    void generateToken_thenExtractEmail_returnsOriginalEmail() {
        String token = jwtUtil.generateToken("user@example.com", "user-uuid-123");
        assertThat(jwtUtil.extractEmail(token)).isEqualTo("user@example.com");
    }

    @Test
    void generateToken_thenExtractUserId_returnsOriginalUserId() {
        String token = jwtUtil.generateToken("user@example.com", "user-uuid-123");
        assertThat(jwtUtil.extractUserId(token)).isEqualTo("user-uuid-123");
    }

    @Test
    void isTokenValid_withValidToken_returnsTrue() {
        String token = jwtUtil.generateToken("user@example.com", "user-uuid-123");
        assertThat(jwtUtil.isTokenValid(token)).isTrue();
    }
}
