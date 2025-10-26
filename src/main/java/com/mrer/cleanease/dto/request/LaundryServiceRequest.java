package com.mrer.cleanease.dto.request;

import com.mrer.cleanease.entity.Enums;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LaundryServiceRequest {
    private String name;
    private String description;
    private BigDecimal price;
    private Enums.ServiceCategory category;
    private Integer estimatedDays;
}
