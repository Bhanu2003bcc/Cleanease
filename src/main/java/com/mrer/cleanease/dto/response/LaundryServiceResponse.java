package com.mrer.cleanease.dto.response;

import com.mrer.cleanease.entity.Enums;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LaundryServiceResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Enums.ServiceCategory category;
    private Integer estimatedDays;
    private String imageUrl;
}
