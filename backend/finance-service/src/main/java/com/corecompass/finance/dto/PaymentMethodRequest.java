package com.corecompass.finance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PaymentMethodRequest {

    @NotBlank(message = "name is required")
    @Size(max = 60)
    private String name;

    @Size(max = 10)
    private String icon;
}