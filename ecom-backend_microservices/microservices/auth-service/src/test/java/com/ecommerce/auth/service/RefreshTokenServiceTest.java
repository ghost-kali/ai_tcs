package com.ecommerce.auth.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.ecommerce.auth.exception.TokenRefreshException;
import com.ecommerce.auth.model.RefreshToken;
import com.ecommerce.auth.repository.RefreshTokenRepository;
import com.ecommerce.auth.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RefreshTokenService service;

    @Test
    void createRefreshToken_deletesExisting_andSavesNew() {
        // Given: small expiration value so expiryDate can be computed
        ReflectionTestUtils.setField(service, "refreshTokenExpirationMs", 1000);
        when(refreshTokenRepository.findByUserId(7L)).thenReturn(Optional.of(new RefreshToken()));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0, RefreshToken.class));

        // When
        service.createRefreshToken(7L);

        // Then
        verify(refreshTokenRepository).delete(any(RefreshToken.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void verifyExpiration_whenExpired_deletesAndThrows() {
        // Given: token with expiryDate in the past
        RefreshToken token = new RefreshToken();
        token.setUserId(1L);
        token.setExpiryDate(Instant.now().minusSeconds(5));

        // When + Then
        assertThrows(TokenRefreshException.class, () -> service.verifyExpiration(token));
        verify(refreshTokenRepository).delete(token);
    }
}
