// src/main/java/com/gymsystem/payments/dto/SubscribeRequest.java
package com.gymsystem.payments.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Simple monthly plan selection for MVP. */
@Data
public class SubscribeRequest {
    @NotBlank
    private String planName;      // e.g., STANDARD_MONTHLY
    @NotNull @Min(100)
    private Long priceCents;      // e.g., 9900 = R$ 99,00
    @NotBlank
    private String currency;      // e.g., BRL
}
