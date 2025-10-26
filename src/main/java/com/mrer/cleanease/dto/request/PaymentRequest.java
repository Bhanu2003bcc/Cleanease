package com.mrer.cleanease.dto.request;

import com.mrer.cleanease.entity.Enums;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {
    @NotNull
    private Long orderId;
    @NotNull
    private Long userId;
    @NotNull
    private BigDecimal amount = BigDecimal.ZERO;
    @NotNull
    private Enums.PaymentMethod paymentMethod;


}
