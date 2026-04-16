package com.epam.finaltask.service.security;

import com.epam.finaltask.model.RefreshToken;
import com.epam.finaltask.model.User;
import com.epam.finaltask.repository.RefreshTokenRepository;
import com.epam.finaltask.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        // Set the refresh token duration via reflection (since it's @Value injected)
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenDurationMs", 86400000L); // 24 hours
    }

    @Test
    void createRefreshToken_ValidUser_Success() {
        // Given
        String username = "testuser";

        User user = new User();
        user.setUsername(username);
        user.setId(UUID.randomUUID());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(86400000L));

        when(userRepository.findUserByUsername(username)).thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        // When
        RefreshToken result = refreshTokenService.createRefreshToken(username);

        // Then
        assertNotNull(result);
        assertNotNull(result.getToken());
        assertEquals(user, result.getUser());
        verify(userRepository, times(1)).findUserByUsername(username);
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void createRefreshToken_UserNotFound_ThrowsException() {
        // Given
        String username = "nonexistent";

        when(userRepository.findUserByUsername(username)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () ->
                refreshTokenService.createRefreshToken(username)
        );
    }

    @Test
    void verifyExpiration_ValidToken_Success() {
        // Given
        RefreshToken validToken = new RefreshToken();
        validToken.setToken(UUID.randomUUID().toString());
        validToken.setExpiryDate(Instant.now().plusMillis(3600000L)); // 1 hour in future

        // When
        RefreshToken result = refreshTokenService.verifyExpiration(validToken);

        // Then
        assertNotNull(result);
        assertEquals(validToken.getToken(), result.getToken());
        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
    }

    @Test
    void verifyExpiration_ExpiredToken_DeletesToken() {
        // Given
        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setToken(UUID.randomUUID().toString());
        expiredToken.setExpiryDate(Instant.now().minusMillis(3600000L)); // 1 hour in past

        doNothing().when(refreshTokenRepository).delete(any(RefreshToken.class));

        // When
        RefreshToken result = refreshTokenService.verifyExpiration(expiredToken);

        // Then
        assertNotNull(result);
        verify(refreshTokenRepository, times(1)).delete(expiredToken);
    }

    @Test
    void isExpired_ExpiredToken_ReturnsTrue() {
        // Given
        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setExpiryDate(Instant.now().minusMillis(1000L)); // 1 second in past

        // When
        boolean result = refreshTokenService.isExpired(expiredToken);

        // Then
        assertTrue(result);
    }

    @Test
    void isExpired_ValidToken_ReturnsFalse() {
        // Given
        RefreshToken validToken = new RefreshToken();
        validToken.setExpiryDate(Instant.now().plusMillis(3600000L)); // 1 hour in future

        // When
        boolean result = refreshTokenService.isExpired(validToken);

        // Then
        assertFalse(result);
    }


    @Test
    void deleteByUserId_UserNotFound_ThrowsException() {
        // Given
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () ->
                refreshTokenService.deleteByUserId(userId)
        );
    }

    @Test
    void getMaxAgeSeconds_ReturnsCorrectValue() {
        // When
        int maxAge = refreshTokenService.getMaxAgeSeconds();

        // Then
        assertEquals(86400, maxAge); // 86400000ms / 1000 = 86400 seconds (24 hours)
    }
}
