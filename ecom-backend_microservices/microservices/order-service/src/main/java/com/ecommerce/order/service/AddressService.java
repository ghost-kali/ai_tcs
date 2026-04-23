package com.ecommerce.order.service;

import com.ecommerce.order.dto.AddressRequest;
import com.ecommerce.order.dto.AddressResponse;
import com.ecommerce.order.model.Address;

import java.util.List;

public interface AddressService {
    AddressResponse createAddress(Long userId, AddressRequest request);
    AddressResponse updateAddress(Long userId, Long addressId, AddressRequest request);
    List<AddressResponse> getAddresses(Long userId);
    void deleteAddress(Long userId, Long addressId);
    Address getAddressEntity(Long userId, Long addressId);
}
