package com.mrer.cleanease.service;

import com.mrer.cleanease.dto.request.CreateOrderRequest;
import com.mrer.cleanease.dto.request.UpdateOrderRequest;
import com.mrer.cleanease.dto.response.OrderResponse;
import com.mrer.cleanease.entity.Enums;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest request);
    OrderResponse getOrderById(Long orderId);
    OrderResponse getOrderByOrderNumber(String orderNumber);
    List<OrderResponse> getMyOrders();
    Page<OrderResponse> getOrdersByCustomerId(Long customerId, Pageable pageable);
    Page<OrderResponse> getAllOrders(Pageable pageable);
    List<OrderResponse> getOrdersByStatus(Enums.OrderStatus status);
    OrderResponse updateOrder(Long orderId, UpdateOrderRequest request);
    OrderResponse updateOrderStatus(Long orderId, Enums.OrderStatus newStatus);
    OrderResponse cancelOrder(Long orderId);
    List<OrderResponse> getOrdersByPickupDateRange(LocalDateTime startDate, LocalDateTime endDate);
    List<OrderResponse> getOrdersByDeliveryDateRange(LocalDateTime startDate, LocalDateTime endDate);
    Long getCustomerOrderCount(Long customerId, Enums.OrderStatus status);
    boolean canModifyOrder(Long orderId);
    void validateOrderRequest(CreateOrderRequest request);

}
