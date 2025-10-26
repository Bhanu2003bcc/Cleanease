package com.mrer.cleanease.mapper;

import com.mrer.cleanease.dto.request.PaymentRequest;
import com.mrer.cleanease.dto.response.PaymentResponse;
import com.mrer.cleanease.entity.Enums;
import com.mrer.cleanease.entity.Order;
import com.mrer.cleanease.entity.Payment;
import com.mrer.cleanease.entity.User;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    /**
     * Maps a PaymentRequest DTO to a Payment entity.
     *
     * @param request the DTO with payment request details
     * @param user    the user making the payment
     * @param order   the order associated with the payment
     * @return a Payment entity populated with request data
     */
    public Payment toEntity(PaymentRequest request, User user, Order order) {
        if (request == null || user == null || order == null) return null;

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setUser(user);
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());

        // These can be set later during payment processing (e.g., in Stripe callback)
        payment.setStatus(Enums.PaymentStatus.PROCESSING);  // Default status
        payment.setStripePaymentIntentId(null);          // To be set when Stripe returns it

        return payment;
    }

    public PaymentResponse toResponse(Payment payment) {
        if (payment == null) return null;

        return PaymentResponse.builder()
                .id(payment.getId())
                .userId(payment.getUser().getId())
                .orderId(payment.getOrder().getId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .stripePaymentIntentId(payment.getStripePaymentIntentId())
                .timeStamp(payment.getTimeStamp())
                .build();
    }
}
