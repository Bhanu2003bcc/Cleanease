package com.mrer.cleanease.mapper;

import com.mrer.cleanease.dto.request.OrderItemRequest;
import com.mrer.cleanease.dto.response.OrderItemResponse;
import com.mrer.cleanease.entity.LaundryService;
import com.mrer.cleanease.entity.OrderItems;
import org.springframework.stereotype.Component;

@Component
public class OrderItemsMapper {

    public OrderItems toEntity(OrderItemRequest dto, LaundryService service) {
        if (dto == null || service == null) return null;

        OrderItems item = new OrderItems();
        item.setService(service);
        item.setQuantity(dto.getQuantity());
        item.setUnitPrice(service.getPrice());
        return item;
    }

    public OrderItemResponse toResponse(OrderItems items){
        if (items == null || items.getService() == null) return null;

        return OrderItemResponse.builder()
                .id(items.getId())
                .serviceName(items.getService().getName())
                .quantity(items.getQuantity())
                .unitPrice(items.getUnitPrice())
                .totalPrice(items.getTotalPrice())
                .build();
    }
}
