package com.mrer.cleanease.controller;

import com.mrer.cleanease.dto.request.PaymentRequest;
import com.mrer.cleanease.dto.response.PaymentResponse;
import com.mrer.cleanease.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/card")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> createCardPayment(@Valid @RequestBody PaymentRequest request){
        log.info("Card is used to payment for Order : {}", request.getOrderId());
        PaymentResponse paymentResponse = paymentService.createCardPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentResponse);
    }

    @PostMapping("/webhook")
    public ResponseEntity<Map<String, String>> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        log.info("POST /api/v1/payments/webhook - Handling Stripe webhook");

        try {
            paymentService.handleStripeWebhooks(payload, sigHeader);
            return ResponseEntity.ok(Map.of("status", "success"));
        } catch (Exception e) {
            log.error("Webhook processing failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @PostMapping("/cash")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<PaymentResponse> processCashPayment(
            @Valid @RequestBody PaymentRequest request) {

        log.info("POST /api/v1/payments/cash - Processing cash payment for order: {}", request.getOrderId());

        PaymentResponse response = paymentService.processCashPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(
             @PathVariable Long orderId) {

        log.info("GET /api/v1/payments/order/{} - Fetching payment by order ID", orderId);

        PaymentResponse response = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByUserId(
             @PathVariable Long userId) {

        log.info("GET /api/v1/payments/user/{} - Fetching payments by user ID", userId);

        List<PaymentResponse> responses = paymentService.getPaymentByUserId(userId);
        return ResponseEntity.ok(responses);
    }


    @DeleteMapping("/{paymentId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Void> cancelPayment(
             @PathVariable Long paymentId) {

        log.info("DELETE /api/v1/payments/{} - Cancelling payment", paymentId);

        paymentService.cancelPayment(paymentId);
        return ResponseEntity.noContent().build();
    }
}
