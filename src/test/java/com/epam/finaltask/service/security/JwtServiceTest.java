package com.epam.finaltask.service.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Set test values using reflection (since they're @Value injected)
        ReflectionTestUtils.setField(jwtService, "jwtSigningKey",
                "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600000L); // 1 hour
    }

    @Test
    void generateToken_ValidUser_ReturnsToken() {
        // Given
        UserDetails userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities(new ArrayList<>())
                .build();

        // When
        String token = jwtService.generateToken(userDetails);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts separated by dots
    }

    @Test
    void extractUsername_ValidToken_ReturnsUsername() {
        // Given
        UserDetails userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities(new ArrayList<>())
                .build();

        String token = jwtService.generateToken(userDetails);

        // When
        String extractedUsername = jwtService.extractUsername(token);

        // Then
        assertNotNull(extractedUsername);
        assertEquals("testuser", extractedUsername);
    }

    @Test
    void isTokenValid_ValidToken_ReturnsTrue() {
        // Given
        UserDetails userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities(new ArrayList<>())
                .build();

        String token = jwtService.generateToken(userDetails);

        // When
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        // Then
        assertTrue(isValid);
    }

    @Test
    void isTokenValid_WrongUser_ReturnsFalse() {
        // Given
        UserDetails user1 = User.builder()
                .username("user1")
                .password("password")
                .authorities(new ArrayList<>())
                .build();

        UserDetails user2 = User.builder()
                .username("user2")
                .password("password")
                .authorities(new ArrayList<>())
                .build();

        String token = jwtService.generateToken(user1);

        // When
        boolean isValid = jwtService.isTokenValid(token, user2);

        // Then
        assertFalse(isValid);
    }

    @Test
    void isTokenValid_ExpiredToken_ReturnsFalse() throws InterruptedException {
        // Given - Create service with very short expiration
        JwtService shortExpirationService = new JwtService();
        ReflectionTestUtils.setField(shortExpirationService, "jwtSigningKey",
                "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(shortExpirationService, "jwtExpiration", 1L); // 1ms

        UserDetails userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities(new ArrayList<>())
                .build();

        String token = shortExpirationService.generateToken(userDetails);

        // Wait for token to expire
        Thread.sleep(10);

        // When
        boolean isValid = shortExpirationService.isTokenValid(token, userDetails);

        // Then
        assertFalse(isValid);
    }

    @Test
    void getMaxAgeSeconds_ReturnsCorrectValue() {
        // When
        int maxAge = jwtService.getMaxAgeSeconds();

        // Then
        assertEquals(3600, maxAge); // 3600000ms / 1000 = 3600 seconds (1 hour)
    }

    @Test
    void extractUsername_InvalidToken_ThrowsException() {
        // Given
        String invalidToken = "invalid.token.here";

        // When & Then
        assertThrows(Exception.class, () ->
                jwtService.extractUsername(invalidToken)
        );
    }

    @Test
    void isTokenValid_MalformedToken_ReturnsFalse() {
        // Given
        UserDetails userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities(new ArrayList<>())
                .build();

        String malformedToken = "this.is.not.a.valid.token";

        // When & Then
        assertThrows(Exception.class, () ->
                jwtService.isTokenValid(malformedToken, userDetails)
        );
    }
}
