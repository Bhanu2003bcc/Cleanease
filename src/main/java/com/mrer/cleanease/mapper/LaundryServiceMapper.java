package com.mrer.cleanease.mapper;

import com.mrer.cleanease.dto.request.LaundryServiceRequest;
import com.mrer.cleanease.dto.response.LaundryServiceResponse;
import com.mrer.cleanease.entity.LaundryService;
import org.springframework.stereotype.Component;

@Component
public class LaundryServiceMapper {

    public LaundryService toEntity(LaundryServiceRequest dto){
        if (dto == null) return null;

        LaundryService service = new LaundryService();
        service.setName(dto.getName());
        service.setDescription(dto.getDescription());
        service.setPrice(dto.getPrice());
        service.setCategory(dto.getCategory());
        return service;
    }

    public LaundryServiceResponse toResponse(LaundryService entity){
        if (entity == null) return null;
        return LaundryServiceResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .build();
    }
}
