package com.ecommerce.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecommerce.order.dto.AddressRequest;
import com.ecommerce.order.dto.AddressResponse;
import com.ecommerce.order.model.Address;
import com.ecommerce.order.repository.AddressRepository;

@ExtendWith(MockitoExtension.class)
class AddressServiceImplTest {

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private AddressServiceImpl service;

    @Test
    void createAddress_mapsAndSaves() {
        // Given: repository assigns an id when saving
        when(addressRepository.save(any(Address.class))).thenAnswer(inv -> {
            Address a = inv.getArgument(0, Address.class);
            a.setAddressId(1L);
            return a;
        });

        AddressRequest request = new AddressRequest();
        request.setBuildingName("B");
        request.setStreet("S");
        request.setCity("C");
        request.setState("ST");
        request.setPincode("12345");
        request.setCountry("IN");

        // When
        AddressResponse response = service.createAddress(9L, request);

        // Then
        assertThat(response.getAddressId()).isEqualTo(1L);
        assertThat(response.getCity()).isEqualTo("C");
    }

    @Test
    void getAddressEntity_whenMissing_throwsNotFound() {
        // Given
        when(addressRepository.findByAddressIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        // When + Then
        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> service.getAddressEntity(2L, 1L));
    }
}
