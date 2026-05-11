package com.ecommerce.auth.controller;

import com.ecommerce.auth.dto.PageResponse;
import com.ecommerce.auth.dto.UserDTO;
import com.ecommerce.auth.model.AppRole;
import com.ecommerce.auth.model.User;
import com.ecommerce.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserRepository userRepository;

    @GetMapping("/sellers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<UserDTO>> getSellers(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        Page<User> page = userRepository.findByRole(
                AppRole.ROLE_SELLER,
                PageRequest.of(Math.max(0, pageNumber), Math.max(1, pageSize))
        );

        return ResponseEntity.ok(PageResponse.<UserDTO>builder()
                .content(page.getContent().stream().map(this::toDto).collect(Collectors.toList()))
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .lastPage(page.isLast())
                .build());
    }

    private UserDTO toDto(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(r -> String.valueOf(r.getRoleName()))
                .collect(Collectors.toSet());

        return new UserDTO(
                user.getUserId(),
                user.getUserName(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getActive(),
                user.getEmailVerified(),
                user.getLastLogin(),
                user.getCreatedAt(),
                roles
        );
    }
}
