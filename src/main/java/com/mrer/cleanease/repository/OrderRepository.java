package com.mrer.cleanease.repository;

import com.mrer.cleanease.entity.Enums;
import com.mrer.cleanease.entity.Order;
import com.mrer.cleanease.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByCustomer(User customer);
    List<Order> findByStatus(Enums.OrderStatus status);
    List<Order> findByCustomerOrderByCreatedAtDesc(User customer);
    List<Order> findAllByOrderByCreatedAtDesc(); // for admin
    Page<Order> findByCustomerOrderByCreatedAtDesc(User customer, Pageable pageable);

    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<Order> findByStatusIn(List<Enums.OrderStatus> statuses);

    @Query("SELECT o FROM Order o WHERE o.pickupDate BETWEEN :startDate AND :endDate")
    List<Order> findByPickupDateBetween(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    @Query("SELECT o FROM Order o WHERE o.deliveryDate BETWEEN :startDate AND :endDate")
    List<Order> findByDeliveryDateBetween(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.customer = :customer AND o.status = :status")
    Long countByCustomerAndStatus(@Param("customer") User customer, @Param("status") Enums.OrderStatus status);

    // Check if order can be modified (before PICKED_UP status)
    @Query("SELECT CASE WHEN o.status IN ('PENDING', 'CONFIRMED', 'IN_PROGRESS') THEN true ELSE false END FROM Order o WHERE o.id = :orderId")
    boolean canModifyOrder(@Param("orderId") Long orderId);

}