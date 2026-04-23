package com.ecommerce.order.controller;

import com.ecommerce.order.dto.AddressRequest;
import com.ecommerce.order.dto.AddressResponse;
import com.ecommerce.order.security.JwtAuthenticationToken;
import com.ecommerce.order.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    public ResponseEntity<AddressResponse> create(@Valid @RequestBody AddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(addressService.createAddress(currentUserId(), request));
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponse> update(@PathVariable Long addressId,
                                                  @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.ok(addressService.updateAddress(currentUserId(), addressId, request));
    }

    @GetMapping
    public ResponseEntity<List<AddressResponse>> getAll() {
        return ResponseEntity.ok(addressService.getAddresses(currentUserId()));
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> delete(@PathVariable Long addressId) {
        addressService.deleteAddress(currentUserId(), addressId);
        return ResponseEntity.noContent().build();
    }

    private Long currentUserId() {
        return ((JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getUserId();
    }
}
