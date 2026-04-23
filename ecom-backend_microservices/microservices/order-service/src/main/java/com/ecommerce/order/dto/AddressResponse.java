package com.ecommerce.order.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AddressResponse {
    private Long addressId;
    private String buildingName;
    private String street;
    private String city;
    private String state;
    private String pincode;
    private String country;
}
