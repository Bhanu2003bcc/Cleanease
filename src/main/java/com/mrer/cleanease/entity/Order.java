package com.mrer.cleanease.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Column(nullable = false, unique = true)
    private String orderNumber;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItems> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private Enums.OrderStatus status = Enums.OrderStatus.PENDING;


    @Enumerated(EnumType.STRING)
    private Enums.PaymentStatus paymentStatus = Enums.PaymentStatus.PROCESSING;

    private String deliveryAddress;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime pickupDate;
    private LocalDateTime deliveryDate;
    private String specialInstructions;

    private BigDecimal totalAmount =  BigDecimal.ZERO;
}
