package com.mrer.cleanease.service.impl;

import com.mrer.cleanease.ExceptionHandler.BusinessException;
import com.mrer.cleanease.ExceptionHandler.ResourceNotFoundException;
import com.mrer.cleanease.config.CustomUserDetails;
import com.mrer.cleanease.dto.request.CreateOrderRequest;
import com.mrer.cleanease.dto.request.OrderItemRequest;
import com.mrer.cleanease.dto.request.UpdateOrderRequest;
import com.mrer.cleanease.dto.response.OrderResponse;
import com.mrer.cleanease.entity.*;
import com.mrer.cleanease.mapper.OrderItemsMapper;
import com.mrer.cleanease.mapper.OrderMapper;
import com.mrer.cleanease.repository.LaundryServiceRepository;
import com.mrer.cleanease.repository.OrderItemsRepository;
import com.mrer.cleanease.repository.OrderRepository;
import com.mrer.cleanease.repository.UserRepository;
import com.mrer.cleanease.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
//import org.apache.kafka.common.errors.AuthorizationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemsRepository orderItemsRepository;
    private final UserRepository userRepository;
    private final LaundryServiceRepository laundryServiceRepository;
    private final OrderMapper orderMapper;
    private final OrderItemsMapper orderItemsMapper;

    @Override
    public OrderResponse createOrder(CreateOrderRequest request) {
        if (request.getCustomerId() == null) {
            throw new IllegalArgumentException("Customer ID must not be null");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        Long loggedInUserId = principal.getId();
        String role = principal.getAuthorities().iterator().next().getAuthority();

        if (role.equals("ROLE_CUSTOMER")) {
            request.setCustomerId(loggedInUserId);
            log.debug("Overriding customerId from request with logged-in user's ID: {}", loggedInUserId);
        } else if ((role.equals("ROLE_ADMIN") || role.equals("ROLE_STAFF")) && request.getCustomerId() == null) {
            throw new IllegalArgumentException("Customer ID must not be null for ADMIN/STAFF orders");
        }
        validateOrderRequest(request);
        User customer = userRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " +request.getCustomerId()));

        Order order = orderMapper.toEntity(request, customer);
        order.setOrderNumber(generateOrderNumber());

        // Save order to get ID
        Order savedOrder = orderRepository.save(order);

        List<OrderItems> orderItems = createOrderItems(request.getItems(), savedOrder);
        savedOrder.setItems(orderItems);

        BigDecimal totalAmount = calculateTotalAmount(orderItems);
        savedOrder.setTotalAmount(totalAmount);

        Order finalOrder = orderRepository.save(savedOrder);
        log.info("Order created successfully with order number: {}", finalOrder.getOrderNumber());
        return orderMapper.toResponse(finalOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        log.info("Fetching order by ID: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: {} " + orderId));
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByOrderNumber(String orderNumber) {
        log.info("Fetching order by OrderNumber: {}", orderNumber);
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: {}"+orderNumber));
        return orderMapper.toResponse(order);
    }
    // customer
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        Long loggedInUserId = principal.getId();
        User customer = userRepository.findById(loggedInUserId)
                .orElseThrow(() -> new ResourceNotFoundException(" Customer not found :"));

        List<Order> orders = orderRepository.findByCustomerOrderByCreatedAtDesc(customer);

        return orders.stream()
                .map(orderMapper::toResponse)
                .collect(Collectors.toList());
    }

    // admin && staff
    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByCustomerId(Long customerId, Pageable pageable) {
        log.debug("Fetching orders for customer ID: {} with pagination", customerId);

        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));

        Page<Order> orders = orderRepository.findByCustomerOrderByCreatedAtDesc(customer, pageable);

        return orders.map(orderMapper::toResponse);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        log.debug("Fetching all orders with Pagination: {}", pageable);
        Page<Order> orders = orderRepository.findAllByOrderByCreatedAtDesc(pageable);

        return orders.map(orderMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByStatus(Enums.OrderStatus status) {
        log.debug("Fetching Orders by status");
        List<Order> orders = orderRepository.findByStatus(status);

        return orders.stream()
                .map(orderMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponse updateOrder(Long orderId, UpdateOrderRequest request) {
        log.info("Updating order ID: {}", orderId);

        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

            // Check if order can be modified
            if (!canModifyOrderStatus(order.getStatus())) {
                throw new BusinessException("Order cannot be modified after pickup. Current status: " + order.getStatus());
            }

            // Update basic fields
            if (request.getPickupDate() != null) {
                validatePickupDate(request.getPickupDate());
                order.setPickupDate(request.getPickupDate());
            }

            if (request.getDeliveryDate() != null) {
                validateDeliveryDate(request.getDeliveryDate(), order.getPickupDate());
                order.setDeliveryDate(request.getDeliveryDate());
            }

            if (request.getDeliveryAddress() != null) {
                order.setDeliveryAddress(request.getDeliveryAddress());
            }

            if (request.getSpecialInstructions() != null) {
                order.setSpecialInstructions(request.getSpecialInstructions());
            }

            // Update order items if provided
            if (request.getItems() != null && !request.getItems().isEmpty()) {
                // Remove existing items
                orderItemsRepository.deleteAll(order.getItems());
                order.getItems().clear();

                // Create new items
                List<OrderItems> newItems = createOrderItems(request.getItems(), order);
                order.setItems(newItems);

                // Recalculate total amount
                BigDecimal totalAmount = calculateTotalAmount(newItems);
                order.setTotalAmount(totalAmount);
            }

            Order updatedOrder = orderRepository.save(order);

            log.info("Order updated successfully: {}", updatedOrder.getOrderNumber());
            return orderMapper.toResponse(updatedOrder);

        } catch (ResourceNotFoundException | BusinessException ex) {
            log.warn("Order update failed for ID {}: {}", orderId, ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while updating order ID {}: {}", orderId, ex.getMessage(), ex);
            throw new RuntimeException("An unexpected error occurred while updating the order", ex);
        }
    }

    @Override
    public OrderResponse updateOrderStatus(Long orderId, Enums.OrderStatus newStatus) {
        log.info("Update status for order Id: {}, with new status: {}", orderId, newStatus);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails principal = (CustomUserDetails) auth.getPrincipal();
        boolean isAdminOrStaff = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_STAFF"));
        if (!isAdminOrStaff) throw new SecurityException("Not authorized to update order status");

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found " + orderId));

        validateStatusTransition(order.getStatus(), newStatus);

        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        Order updatedOrder = orderRepository.save(order);

        log.info("Order status updated successfully for order: {} to {}", order.getOrderNumber(), newStatus);
        return orderMapper.toResponse(updatedOrder);
    }

    @Override
    public OrderResponse cancelOrder(Long orderId) {
        log.info("order is cancel with orderId: {}", orderId);

        Order order =  orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: {}"+orderId));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        Long loggedInUserId = principal.getId();
        if(!order.getCustomer().getId().equals(loggedInUserId)){
            throw new AccessDeniedException("You are not allowed to cancel order");
        }
        if (order.getStatus() == Enums.OrderStatus.CANCELLED) {
            throw new BusinessException("Order is already cancelled");
        }
        if (!canModifyOrderStatus(order.getStatus())) {
            throw new BusinessException("Order cannot be cancelled after pickup. Current status: " + order.getStatus());
        }

        order.setStatus(Enums.OrderStatus.CANCELLED);
        Order cancelledOrder = orderRepository.save(order);

        log.info("Order {} for customer {} cancelled successfully",
                cancelledOrder.getOrderNumber(), cancelledOrder.getCustomer().getId());

        return orderMapper.toResponse(cancelledOrder);

    }

    @Override
    public List<OrderResponse> getOrdersByPickupDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return List.of();
    }

    @Override
    public List<OrderResponse> getOrdersByDeliveryDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return List.of();
    }

    @Override
    public Long getCustomerOrderCount(Long customerId, Enums.OrderStatus status) {
        return 0L;
    }

    @Override
    public boolean canModifyOrder(Long orderId) {
        return false;
    }

    @Override
    public void validateOrderRequest(CreateOrderRequest request) {
        log.debug("Validating order request");

        // Validate order items
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException("Order must contain at least one item");
        }

        // Validate pickup date
        validatePickupDate(request.getPickupDate());

        // Validate delivery date
        validateDeliveryDate(request.getDeliveryDate(), request.getPickupDate());

        // Validate services exist and are active
        for (OrderItemRequest item : request.getItems()) {
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new BusinessException("Order item quantity must be greater than 0");
            }

            LaundryService service = laundryServiceRepository.findById(item.getServiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Service not found with ID: " + item.getServiceId()));

            if (!service.isActive()) {
                throw new BusinessException("Service is not active: " + service.getName());
            }
        }

    }

 //   Helper methods

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    private List<OrderItems> createOrderItems(List<OrderItemRequest> itemRequests, Order order) {
        List<OrderItems> orderItems = new ArrayList<>();

        for (OrderItemRequest itemRequest : itemRequests) {
            LaundryService service = laundryServiceRepository.findById(itemRequest.getServiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Service not found with ID: " + itemRequest.getServiceId()));

            OrderItems orderItem = orderItemsMapper.toEntity(itemRequest, service);
            orderItem.setOrder(order);
            orderItem.calculateTotalPrice();

            OrderItems savedItem = orderItemsRepository.save(orderItem);
            orderItems.add(savedItem);
        }

        return orderItems;
    }

    private BigDecimal calculateTotalAmount(List<OrderItems> orderItems) {
        return orderItems.stream()
                .map(OrderItems::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validatePickupDate(LocalDateTime pickupDate) {
        if (pickupDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BusinessException("Pickup date must be at least 24 hours from now");
        }
    }

    private void validateDeliveryDate(LocalDateTime deliveryDate, LocalDateTime pickupDate) {
        if (deliveryDate.isBefore(pickupDate.plusDays(1))) {
            throw new BusinessException("Delivery date must be at least 1 day after pickup date");
        }
    }

    private boolean canModifyOrderStatus(Enums.OrderStatus currentStatus) {
        return currentStatus == Enums.OrderStatus.PENDING ||
                currentStatus == Enums.OrderStatus.CONFIRMED ||
                currentStatus == Enums.OrderStatus.IN_PROCESS;
    }

//    private Long getCurrentUserId() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
//            throw new BusinessException("Unauthorized access: no logged-in user found");
//        }
//
//        // Assuming your `User` entity implements UserDetails directly
//        if (userDetails instanceof User user) {
//            return user.getId();
//        }
//
//        throw new BusinessException("Unable to resolve logged-in user");
//    }
//
//    private boolean hasRole(String role) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication == null) return false;
//
//        return authentication.getAuthorities().stream()
//                .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role));
//    }


    private void validateStatusTransition(Enums.OrderStatus currentStatus, Enums.OrderStatus newStatus) {
        // Define valid status transitions based on business logic
        switch (currentStatus) {
            case PENDING:
                if (newStatus != Enums.OrderStatus.CONFIRMED &&
                        newStatus != Enums.OrderStatus.CANCELLED) {
                    throw new BusinessException("Invalid status transition from PENDING to " + newStatus);
                }
                break;
            case CONFIRMED:
                if (newStatus != Enums.OrderStatus.IN_PROCESS &&
                        newStatus != Enums.OrderStatus.CANCELLED) {
                    throw new BusinessException("Invalid status transition from CONFIRMED to " + newStatus);
                }
                break;
            case IN_PROCESS:
                if (newStatus != Enums.OrderStatus.PICKED_UP &&
                        newStatus != Enums.OrderStatus.CANCELLED) {
                    throw new BusinessException("Invalid status transition from IN_PROGRESS to " + newStatus);
                }
                break;
            case PICKED_UP:
                if (newStatus != Enums.OrderStatus.CLEANING) {
                    throw new BusinessException("Invalid status transition from PICKED_UP to " + newStatus);
                }
                break;
            case CLEANING:
                if (newStatus != Enums.OrderStatus.READY) {
                    throw new BusinessException("Invalid status transition from CLEANING to " + newStatus);
                }
                break;
            case READY:
                if (newStatus != Enums.OrderStatus.OUT_FOR_DELIVERY) {
                    throw new BusinessException("Invalid status transition from READY to " + newStatus);
                }
                break;
            case OUT_FOR_DELIVERY:
                if (newStatus != Enums.OrderStatus.DELIVERED) {
                    throw new BusinessException("Invalid status transition from OUT_FOR_DELIVERY to " + newStatus);
                }
                break;
            case DELIVERED:
                if (newStatus != Enums.OrderStatus.COMPLETED) {
                    throw new BusinessException("Invalid status transition from DELIVERED to "+ newStatus);
                }
                break;
            case COMPLETED:
            case CANCELLED:
            case FAILED:
                throw new BusinessException("Cannot change status from " + currentStatus);
            default:
                throw new BusinessException("Unknown order status: " + currentStatus);
        }
    }
}
