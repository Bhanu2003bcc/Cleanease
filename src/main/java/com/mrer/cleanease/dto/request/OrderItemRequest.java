package com.mrer.cleanease.dto.request;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class OrderItemRequest {

    @NotNull
    private Long serviceId;

    @NotNull
    private Integer quantity;
}
