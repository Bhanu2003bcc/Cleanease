package com.mrer.cleanease.mapper;

import com.mrer.cleanease.dto.request.CreateOrderRequest;
import com.mrer.cleanease.dto.response.OrderResponse;
import com.mrer.cleanease.entity.Enums;
import com.mrer.cleanease.entity.Order;
import com.mrer.cleanease.entity.User;
import lombok.Builder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.mrer.cleanease.entity.Enums.OrderStatus.PENDING;

@Component
public class OrderMapper {

    private final OrderItemsMapper orderItemsMapper;
    private final UserMapper userMapper;

    public OrderMapper(OrderItemsMapper orderItemsMapper, UserMapper userMapper) {
        this.orderItemsMapper = orderItemsMapper;
        this.userMapper = userMapper;
    }

    public Order toEntity(CreateOrderRequest dto, User user){
        if(dto == null || user == null) return null;

        Order order = new Order();
        order.setCustomer(user);
        order.setPickupDate(dto.getPickupDate());
        order.setDeliveryDate(dto.getDeliveryDate());
        order.setStatus(PENDING);
        return order;
    }

    public OrderResponse toResponse(Order order){
        if (order == null) return null;
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customer(userMapper.toResponse(order.getCustomer()))
                .pickupDate(order.getPickupDate())
                .deliveryDate(order.getDeliveryDate())
                .deliveryAddress(order.getDeliveryAddress())
                .specialInstructions(order.getSpecialInstructions())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .items(order.getItems().stream()
                        .map(orderItemsMapper::toResponse) // if single item selected not the list of items
                        .filter(Objects::nonNull)      // this can prevent the nullPointerException
                        .collect(Collectors.toList()))
                .build();
    }
}
