package com.mrer.cleanease.mapper;

import com.mrer.cleanease.dto.request.CreateOrderRequest;
import com.mrer.cleanease.dto.response.OrderItemResponse;
import com.mrer.cleanease.dto.response.OrderResponse;
import com.mrer.cleanease.dto.response.UserResponse;
import com.mrer.cleanease.entity.Enums;
import com.mrer.cleanease.entity.Order;
import com.mrer.cleanease.entity.OrderItems;
import com.mrer.cleanease.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OrderMapperTest {

    private OrderMapper orderMapper;
    private UserMapper userMapper;
    private OrderItemsMapper orderItemsMapper;

    @BeforeEach
    void setUp() {
        userMapper = mock(UserMapper.class);
        orderItemsMapper = mock(OrderItemsMapper.class);
        orderMapper = new OrderMapper(orderItemsMapper, userMapper);
    }

    @Test
    void testToEntity() {
        CreateOrderRequest dto = new CreateOrderRequest();
        dto.setPickupDate(LocalDateTime.now());
        dto.setDeliveryDate(LocalDateTime.now().plusDays(3));

        User user = new User();

        Order order = orderMapper.toEntity(dto, user);

        assertNotNull(order);
        assertEquals(user, order.getCustomer());
        assertEquals(dto.getPickupDate(), order.getPickupDate());
        assertEquals(dto.getDeliveryDate(), order.getDeliveryDate());
        assertEquals(Enums.OrderStatus.PENDING, order.getStatus());
    }

    @Test
    void testToResponse() {
        User user = new User();
        user.setId(1L);
        OrderItems item = new OrderItems();
        Order order = new Order();
        order.setId(100L);
        order.setCustomer(user);
        order.setPickupDate(LocalDateTime.now());
        order.setDeliveryDate(LocalDateTime.now().plusDays(3));
        order.setStatus(Enums.OrderStatus.COMPLETED);
        order.setTotalAmount(BigDecimal.valueOf(500.0));
        order.setItems(Collections.singletonList(item));

        UserResponse userResponse = new UserResponse();
        OrderItemResponse itemResponse = new OrderItemResponse();

        when(userMapper.toResponse(user)).thenReturn(userResponse);
        when(orderItemsMapper.toResponse(item)).thenReturn(itemResponse);

        OrderResponse response = orderMapper.toResponse(order);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(userResponse, response.getCustomer());
        assertEquals(1, response.getItems().size());
        assertEquals(500.0, response.getTotalAmount());
    }
}
