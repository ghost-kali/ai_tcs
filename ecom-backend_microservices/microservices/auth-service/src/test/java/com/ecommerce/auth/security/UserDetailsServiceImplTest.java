package com.ecommerce.auth.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.ecommerce.auth.model.AppRole;
import com.ecommerce.auth.model.Role;
import com.ecommerce.auth.model.User;
import com.ecommerce.auth.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl service;

    @Test
    void loadUserByUsername_buildsUserDetails() {
        // Given: a user with ROLE_USER present in repository
        User user = new User("alice", "alice@example.com", "pw");
        user.setUserId(1L);
        user.setActive(true);
        user.setEmailVerified(false);
        user.setRoles(Set.of(new Role(AppRole.ROLE_USER)));

        when(userRepository.findByUserNameWithRoles("alice")).thenReturn(Optional.of(user));

        // When
        var details = service.loadUserByUsername("alice");

        // Then
        assertThat(details.getUsername()).isEqualTo("alice");
        assertThat(details.getAuthorities()).extracting(a -> a.getAuthority()).contains("ROLE_USER");
    }

    @Test
    void loadUserByUsername_whenMissing_throws() {
        // Given
        when(userRepository.findByUserNameWithRoles("missing")).thenReturn(Optional.empty());

        // When + Then
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("missing"));
    }
}
