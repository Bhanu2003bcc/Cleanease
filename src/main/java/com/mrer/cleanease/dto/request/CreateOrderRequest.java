package com.mrer.cleanease.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderRequest {

    @NotNull
    private Long customerId;

    @NotEmpty
    private List<OrderItemRequest> items;

    @NotNull
    private LocalDateTime pickupDate;

    @NotNull
    @Future
    private LocalDateTime deliveryDate;
    private String specialInstructions;

    private String deliveryAddress;
}
