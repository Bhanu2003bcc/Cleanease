package com.mrer.cleanease.repository;

import com.mrer.cleanease.entity.Enums;
import com.mrer.cleanease.entity.Order;
import com.mrer.cleanease.entity.Payment;
import com.mrer.cleanease.entity.User;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrder(Order order);
    Optional<Payment> findByStripePaymentIntentId(String stripeId);
    List<Payment> findByStatus(Enums.PaymentStatus status);

    List<Payment> findByUserOrderByTimeStampDesc(User user);

    List<Payment> findByUserAndStatus(User user, Enums.PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.user.id = :userId AND p.status = :status")
    List<Payment> findByUserIdAndStatus(@Param("userId") Long userId,
                                        @Param("status") Enums.PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.timeStamp BETWEEN :startDate AND :endDate")
    List<Payment> findPaymentsByDateRange(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status")
    Long countByStatus(@Param("status") Enums.PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.paymentMethod = :method AND p.status = :status")
    List<Payment> findByPaymentMethodAndStatus(@Param("method") Enums.PaymentMethod method,
                                               @Param("status") Enums.PaymentStatus status);

    // For admin analytics
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'SUCCEEDED' AND p.paymentDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalSuccessfulPaymentsBetweenDates(@Param("startDate") LocalDateTime startDate,
                                                      @Param("endDate") LocalDateTime endDate);
}

