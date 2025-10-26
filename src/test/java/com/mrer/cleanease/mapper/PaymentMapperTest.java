package com.mrer.cleanease.mapper;

import com.mrer.cleanease.dto.request.PaymentRequest;
import com.mrer.cleanease.dto.response.PaymentResponse;
import com.mrer.cleanease.entity.Enums;
import com.mrer.cleanease.entity.Order;
import com.mrer.cleanease.entity.Payment;
import com.mrer.cleanease.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class PaymentMapperTest {

    private PaymentMapper paymentMapper;

    @BeforeEach
    public void setUp() {
        paymentMapper = new PaymentMapper();
    }

    @Test
    public void testToEntity_shouldMapCorrectly() {
        // Given
        PaymentRequest request = new PaymentRequest(1L, 2L, new BigDecimal("150.00"), Enums.PaymentMethod.CARD);
        User user = new User();
        user.setId(2L);

        Order order = new Order();
        order.setId(1L);

        // When
        Payment payment = paymentMapper.toEntity(request, user, order);

        // Then
        assertThat(payment).isNotNull();
        assertThat(payment.getAmount()).isEqualByComparingTo("150.00");
        assertThat(payment.getUser()).isEqualTo(user);
        assertThat(payment.getOrder()).isEqualTo(order);
        assertThat(payment.getPaymentMethod()).isEqualTo(Enums.PaymentMethod.CARD);
        assertThat(payment.getStatus()).isEqualTo(Enums.PaymentStatus.PROCESSING);
        assertThat(payment.getStripePaymentIntentId()).isNull();
    }

    @Test
    public void testToResponse_shouldMapCorrectly() {
        // Given
        User user = new User();
        user.setId(5L);

        Order order = new Order();
        order.setId(10L);

        Payment payment = new Payment();
        payment.setId(100L);
        payment.setAmount(new BigDecimal("250.50"));
        payment.setUser(user);
        payment.setOrder(order);
        payment.setPaymentMethod(Enums.PaymentMethod.CARD);
        payment.setStatus(Enums.PaymentStatus.SUCCEEDED);
        payment.setStripePaymentIntentId("pi_123456");
        payment.setTimeStamp(LocalDateTime.of(2025, 8, 7, 12, 0));

        // When
        PaymentResponse response = paymentMapper.toResponse(payment);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getUserId()).isEqualTo(5L);
        assertThat(response.getOrderId()).isEqualTo(10L);
        assertThat(response.getAmount()).isEqualByComparingTo("250.50");
        assertThat(response.getPaymentMethod()).isEqualTo(Enums.PaymentMethod.CARD);
        assertThat(response.getStatus()).isEqualTo(Enums.PaymentStatus.SUCCEEDED);
        assertThat(response.getStripePaymentIntentId()).isEqualTo("pi_123456");
        assertThat(response.getTimeStamp()).isEqualTo(LocalDateTime.of(2025, 8, 7, 12, 0));
    }

    @Test
    public void testToEntity_withNullInput_shouldReturnNull() {
        assertThat(paymentMapper.toEntity(null, null, null)).isNull();
    }

    @Test
    public void testToResponse_withNullInput_shouldReturnNull() {
        assertThat(paymentMapper.toResponse(null)).isNull();
    }
}
