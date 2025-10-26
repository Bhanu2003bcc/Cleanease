package com.mrer.cleanease.service;

import com.mrer.cleanease.dto.request.PaymentRequest;
import com.mrer.cleanease.dto.response.PaymentResponse;

import java.util.List;

public interface PaymentService {
    PaymentResponse createCardPayment(PaymentRequest request);
    PaymentResponse processCashPayment(PaymentRequest request);
    void handleStripeWebhooks(String payment, String sigHeader);
    PaymentResponse getPaymentByOrderId(Long orderId);
    List<PaymentResponse> getPaymentByUserId(Long userId);
    void cancelPayment(Long PaymentId);
}
