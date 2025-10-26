package com.mrer.cleanease.dto.request;


import jakarta.validation.constraints.Future;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateOrderRequest {
    private List<OrderItemRequest> items;

    @Future(message = "Pickup date must be in the future")
    private LocalDateTime pickupDate;

    @Future(message = "Delivery date must be in the future")
    private LocalDateTime deliveryDate;

    private String specialInstructions;
    private String deliveryAddress;
}
