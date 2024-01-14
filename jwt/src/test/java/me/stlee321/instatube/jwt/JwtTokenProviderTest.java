package me.stlee321.instatube.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {JwtConfig.class, JwtTokenProvider.class})
@TestPropertySource(locations = "classpath:jwt-application-test.properties")
@DisplayName("JwtTokenProvider 테스트")
class JwtTokenProviderTest {

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Nested
    @DisplayName("토큰 유효성 테스트")
    class TokenValidateTest {

        String username;
        String token;

        @BeforeEach
        void setUsername() {
            username = "stlee";
            token = jwtTokenProvider.createAccessToken(username);
        }


        @Test
        @DisplayName("유효한 토큰")
        void checkTokenValidity() {
            boolean result = jwtTokenProvider.validate(token);
            assertTrue(result);
        }

        @Test
        @DisplayName("null 토큰 -> false")
        void nullTokenReturnsFalse() {
            boolean result = jwtTokenProvider.validate(null);
            assertFalse(result);
        }

        @Test
        @DisplayName("빈 토큰 -> false")
        void emptyTokenReturnsFalse() {
            boolean result = jwtTokenProvider.validate("");
            assertFalse(result);
        }

        @Test
        @DisplayName("유효하지 않은 토큰 -> false")
        void invalidTokenReturnsFalse() {
            boolean result = jwtTokenProvider.validate("alsdkfja");
            assertFalse(result);
        }


    }

}