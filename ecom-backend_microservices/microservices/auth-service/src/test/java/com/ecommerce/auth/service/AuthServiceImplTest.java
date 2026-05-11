package com.ecommerce.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ecommerce.auth.dto.JwtResponse;
import com.ecommerce.auth.dto.LoginRequest;
import com.ecommerce.auth.dto.MessageResponse;
import com.ecommerce.auth.dto.SignupRequest;
import com.ecommerce.auth.model.AppRole;
import com.ecommerce.auth.model.RefreshToken;
import com.ecommerce.auth.model.Role;
import com.ecommerce.auth.model.User;
import com.ecommerce.auth.repository.RoleRepository;
import com.ecommerce.auth.repository.UserRepository;
import com.ecommerce.auth.security.JwtUtils;
import com.ecommerce.auth.security.UserDetailsImpl;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void authenticateUser_returnsJwtAndRefreshToken() {
        // Given: login request
        LoginRequest loginRequest = new LoginRequest("alice", "secret");

        // And: authentication manager returns an Authentication with a UserDetails principal
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        UserDetailsImpl principal = new UserDetailsImpl(
                7L,
                "alice",
                "alice@example.com",
                "pw",
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                true,
                false);

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("access.jwt");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh.jwt");
        refreshToken.setUserId(7L);
        when(refreshTokenService.createRefreshToken(7L)).thenReturn(refreshToken);

        // When
        JwtResponse response = authService.authenticateUser(loginRequest);

        // Then
        assertThat(response.getAccessToken()).isEqualTo("access.jwt");
        assertThat(response.getRefreshToken()).isEqualTo("refresh.jwt");
        org.mockito.Mockito.verify(userRepository).updateLastLogin(eq(7L), any());
    }

    @Test
    void registerUser_whenUsernameExists_returnsFailure() {
        // Given
        SignupRequest signupRequest = new SignupRequest("alice", "alice@example.com", "pw", Set.of(), null, null, null);
        when(userRepository.existsByUserName("alice")).thenReturn(true);

        // When
        MessageResponse response = authService.registerUser(signupRequest);

        // Then
        assertTrue(!response.isSuccess());
    }

    @Test
    void registerUser_whenNoRoles_assignsUserRole_andSavesUser() {
        // Given: signup without roles (service should default to ROLE_USER)
        SignupRequest signupRequest = new SignupRequest("bob", "bob@example.com", "pw", null, null, null, null);
        when(userRepository.existsByUserName("bob")).thenReturn(false);
        when(userRepository.existsByEmail("bob@example.com")).thenReturn(false);
        when(passwordEncoder.encode("pw")).thenReturn("encoded");

        Role userRole = new Role(1, AppRole.ROLE_USER);
        when(roleRepository.findByRoleName(AppRole.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0, User.class));

        // When
        authService.registerUser(signupRequest);

        // Then
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getRoles())
                .extracting(Role::getRoleName)
                .contains(AppRole.ROLE_USER);
    }
}
