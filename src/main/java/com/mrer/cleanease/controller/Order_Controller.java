package com.mrer.cleanease.controller;

import com.mrer.cleanease.dto.request.CreateOrderRequest;
import com.mrer.cleanease.dto.request.UpdateOrderRequest;
import com.mrer.cleanease.dto.response.ApiResponse;
import com.mrer.cleanease.dto.response.OrderResponse;
import com.mrer.cleanease.entity.Enums;
import com.mrer.cleanease.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/order")
public class Order_Controller {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@Valid @RequestBody CreateOrderRequest request){
        log.info("Creating new order for customer ID: {}", request.getCustomerId());

            OrderResponse orderResponse = orderService.createOrder(request);
            ApiResponse<OrderResponse> response = ApiResponse.<OrderResponse>builder()
                    .success(true)
                    .message("Order successful")
                    .timestamp(LocalDateTime.now())
                    .data(orderResponse)
                    .build();
            return new ResponseEntity<>(response, HttpStatus.CREATED);

    }

    @GetMapping("/order_id/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Long id){
        log.info("Fetching my order with my Id: {}", id);

        OrderResponse orderResponse =orderService.getOrderById(id);
        ApiResponse<OrderResponse> response = ApiResponse.<OrderResponse>builder()
                .success(true)
                .message("Order found successfully")
                .data(orderResponse)
                .build();

        return new ResponseEntity<>(response, HttpStatus.FOUND);
    }

    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<OrderResponse>> getMyOrders() {
        return ResponseEntity.ok(orderService.getMyOrders());
    }

    @GetMapping("/admin/orders/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<Page<OrderResponse>> getOrdersByCustomerIdPaginated(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.debug("Admin/Staff fetching orders for customer ID: {} with pagination", customerId);

        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<OrderResponse> responses = orderService.getOrdersByCustomerId(customerId, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<List<OrderResponse>> getOrderByStatus(@PathVariable Enums.OrderStatus status){
        log.info("Fetching orders for status: {}", status);

        List<OrderResponse> responses = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/update/{orderId}")
    public ResponseEntity<OrderResponse> updateOrder(
                                        @PathVariable Long orderId,
                                        @Valid @RequestBody UpdateOrderRequest request){
        log.info("Update order with Id: {}", orderId);

        OrderResponse response = orderService.updateOrder(orderId,request);
        return ResponseEntity.ok(response);

    }

    @PatchMapping("/admin/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId, @RequestParam Enums.OrderStatus status){
        log.info("Updating order status for ID: {} to {}", orderId, status);
        OrderResponse response = orderService.updateOrderStatus(orderId, status);
        return  ResponseEntity.ok(response);

    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long orderId){
        log.info("Cancel order for Order Id: {}", orderId);

        OrderResponse response = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(response);
    }

}
