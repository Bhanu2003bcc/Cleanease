package com.mrer.cleanease.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mrer.cleanease.entity.Enums;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentResponse {
    private Long id;
    private Long userId;
    private Long orderId;
    private BigDecimal amount;
    private String stripePaymentIntentId;
    private Enums.PaymentStatus status;
    private Enums.PaymentMethod paymentMethod;
    private LocalDateTime timeStamp;

    private String clientSecret;

    private String statusMessage;
    private String failureReason;
}
