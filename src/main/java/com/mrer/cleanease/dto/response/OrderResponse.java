package com.mrer.cleanease.dto.response;

import com.mrer.cleanease.entity.Enums;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponse {

    private Long id;
    private String orderNumber;
    private List<OrderItemResponse> items;
    private Enums.OrderStatus status;
    private LocalDateTime pickupDate;
    private LocalDateTime deliveryDate;
    private String specialInstructions;
    private String deliveryAddress;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private UserResponse customer;
}
