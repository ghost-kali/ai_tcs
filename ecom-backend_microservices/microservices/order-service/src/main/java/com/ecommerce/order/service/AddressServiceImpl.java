package com.ecommerce.order.service;

import com.ecommerce.order.dto.AddressRequest;
import com.ecommerce.order.dto.AddressResponse;
import com.ecommerce.order.model.Address;
import com.ecommerce.order.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;

    @Override
    public AddressResponse createAddress(Long userId, AddressRequest request) {
        Address address = new Address();
        apply(address, request, userId);
        return toResponse(addressRepository.save(address));
    }

    @Override
    public AddressResponse updateAddress(Long userId, Long addressId, AddressRequest request) {
        Address address = getAddressEntity(userId, addressId);
        apply(address, request, userId);
        return toResponse(addressRepository.save(address));
    }

    @Override
    public List<AddressResponse> getAddresses(Long userId) {
        return addressRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public void deleteAddress(Long userId, Long addressId) {
        Address address = getAddressEntity(userId, addressId);
        addressRepository.delete(address);
    }

    @Override
    public Address getAddressEntity(Long userId, Long addressId) {
        return addressRepository.findByAddressIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found"));
    }

    private void apply(Address address, AddressRequest request, Long userId) {
        address.setUserId(userId);
        address.setBuildingName(request.getBuildingName());
        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPincode(request.getPincode());
        address.setCountry(request.getCountry());
    }

    private AddressResponse toResponse(Address address) {
        return AddressResponse.builder()
                .addressId(address.getAddressId())
                .buildingName(address.getBuildingName())
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .pincode(address.getPincode())
                .country(address.getCountry())
                .build();
    }
}
