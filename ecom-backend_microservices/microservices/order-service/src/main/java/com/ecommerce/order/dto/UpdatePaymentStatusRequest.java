package com.ecommerce.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePaymentStatusRequest {
    @NotBlank
    private String transactionReference;

    // "COMPLETED", "FAILED", etc. Defaults to COMPLETED if missing/invalid.
    private String status;
}

