package com.mrer.cleanease.service.impl;

import com.mrer.cleanease.ExceptionHandler.PaymentException;
import com.mrer.cleanease.ExceptionHandler.ResourceNotFoundException;
import com.mrer.cleanease.dto.request.PaymentRequest;
import com.mrer.cleanease.dto.response.PaymentResponse;
import com.mrer.cleanease.entity.Enums;
import com.mrer.cleanease.entity.Order;
import com.mrer.cleanease.entity.Payment;
import com.mrer.cleanease.entity.User;
import com.mrer.cleanease.mapper.PaymentMapper;
import com.mrer.cleanease.repository.OrderRepository;
import com.mrer.cleanease.repository.PaymentRepository;
import com.mrer.cleanease.repository.UserRepository;
import com.mrer.cleanease.service.OrderService;
import com.mrer.cleanease.service.PaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;

import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final PaymentMapper paymentMapper;
    private final OrderService orderService;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @Override
    public PaymentResponse createCardPayment(PaymentRequest request) {
        log.info("Creating card payment for order: {} by user: {}", request.getOrderId(), request.getUserId());

        User user = getUserById(request.getUserId());
        Order order = getOrderById(request.getOrderId());

        Optional<Payment> existingPayment = paymentRepository.findByOrder(order);
        if (existingPayment.isPresent()) {
            Payment existing = existingPayment.get();
            if (existing.getStatus() == Enums.PaymentStatus.SUCCEEDED) {
                throw new PaymentException("Payment already completed for this order");
            }
            return updateExistingPayment(existing, request);
        }
        try{
            PaymentIntent paymentIntent = createStripePaymentIntent(request, order, user);
            Payment payment = paymentMapper.toEntity(request, user, order);
            payment.setStripePaymentIntentId(paymentIntent.getId());
            payment.setStatus(Enums.PaymentStatus.PROCESSING);

            Payment savedPayment = paymentRepository.save(payment);
            PaymentResponse paymentResponse = paymentMapper.toResponse(savedPayment);

            paymentResponse.setClientSecret(paymentIntent.getClientSecret());
            log.info("Payment intent created successfully: {}", paymentIntent.getId());
            return paymentResponse;

        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PaymentResponse processCashPayment(PaymentRequest request) {
        log.info("Processing cash payment for order: {} by user: {}", request.getOrderId(), request.getUserId());

        User user = getUserById(request.getUserId());
        Order order = getOrderById(request.getOrderId());

        Optional<Payment> existingPayment = paymentRepository.findByOrder(order);
        if (existingPayment.isPresent()){
            Payment existing = existingPayment.get();
            if (existing.getStatus() == Enums.PaymentStatus.SUCCEEDED){
                throw new PaymentException("Payment already competed for this order");
            }
        }

        Payment payment = paymentMapper.toEntity(request, user,  order);
        payment.setStatus(Enums.PaymentStatus.SUCCEEDED);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStripePaymentIntentId("CASH_PAYMENT");

        Payment savedpayment = paymentRepository.save(payment);
        orderService.updateOrderStatus(order.getId(), Enums.OrderStatus.CONFIRMED);
        return paymentMapper.toResponse(savedpayment);
    }



    @Override
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        Order order = getOrderById(orderId);
        Payment payment = paymentRepository.findByOrder(order)
                .orElseThrow(()-> new ResourceNotFoundException("Payment not found for order: " +orderId));

        return paymentMapper.toResponse(payment);
    }

    @Override
    public List<PaymentResponse> getPaymentByUserId(Long userId) {
        User user = getUserById(userId);
        List<Payment> payments = paymentRepository.findByUserOrderByTimeStampDesc(user);
        return payments.stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void cancelPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found" + paymentId));

        if(payment.getStatus() == Enums.PaymentStatus.SUCCEEDED){
            throw new PaymentException("Cannot cancel competed payment");
        }

        if (payment.getStripePaymentIntentId() != null && !payment.getStripePaymentIntentId().equals("CASH_PAYMENT")) {
            try {
                PaymentIntent intent = PaymentIntent.retrieve(payment.getStripePaymentIntentId());
                if ("requires_payment_method".equals(intent.getStatus()) ||
                        "requires_confirmation".equals(intent.getStatus())) {
                    intent.cancel();
                }
            } catch (StripeException e) {
                log.warn("Failed to cancel Stripe payment intent: {}", e.getMessage());
            }
        }

        payment.setStatus(Enums.PaymentStatus.CANCELLED);
        paymentRepository.save(payment);

        log.info("Payment cancelled: {}", paymentId);

    }

    @Override
    @Transactional
    public void handleStripeWebhooks(String payload, String sigHeader) {
        log.info("Received Stripe webhook");

        try {
            com.stripe.model.Event event = com.stripe.net.Webhook.constructEvent(
                    payload, sigHeader, webhookSecret
            );

            switch (event.getType()) {
                case "payment_intent.succeeded":
                    handlePaymentSucceeded(event);
                    break;
                case "payment_intent.payment_failed":
                    handlePaymentFailed(event);
                    break;
                case "payment_intent.requires_action":
                    handelPaymentRequiresAction(event);
                    break;
                default:
                    log.info("Unhandled webhook event type: {}", event.getType());
            }
        } catch (Exception e) {
            log.error("Error processing Stripe webhook: {}", e.getMessage());
            throw new PaymentException("Webhook processing failed");
        }
    }
    // ------------------Helper Method---------------------------- //

    private PaymentIntent createStripePaymentIntent(PaymentRequest request, Order order, User user) throws StripeException{
     long amountInCents = request.getAmount().multiply(BigDecimal.valueOf(100)).longValue();
        Map<String, String> metaData = new HashMap<>();
        metaData.put("order_id", order.getId().toString());
        metaData.put("user_id", user.getId().toString());
        metaData.put("user_mail", user.getEmail());


        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency("inr")
                .addPaymentMethodType("card")
                .setDescription("CleanEase Service Payment - Order #" + order.getId())
                .putAllMetadata(metaData)
                .setConfirmationMethod(PaymentIntentCreateParams.ConfirmationMethod.MANUAL)
                .build();

        return PaymentIntent.create(params);
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
    }

    private PaymentResponse updateExistingPayment(Payment existingPayment, PaymentRequest request){
        try{
            if (existingPayment.getStripePaymentIntentId() != null &&
            !existingPayment.getStripePaymentIntentId().equals("CASH")){
                PaymentIntent intent = PaymentIntent.retrieve(existingPayment.getStripePaymentIntentId());
                updatePaymentFromStripe(existingPayment, intent);

                PaymentResponse response = paymentMapper.toResponse(existingPayment);
                response.setClientSecret(intent.getClientSecret());
                return response;
            }
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
        return paymentMapper.toResponse(existingPayment);
    }

    private void updatePaymentFromStripe(Payment payment, PaymentIntent intent) {
        switch (intent.getStatus()) {
            case "succeeded":
                payment.setStatus(Enums.PaymentStatus.SUCCEEDED);
                payment.setPaymentDate(LocalDateTime.now());
                updateOrderStatus(payment.getOrder(), Enums.OrderStatus.CONFIRMED);
                break;
            case "requires_payment_method":
            case "requires_confirmation":
                payment.setStatus(Enums.PaymentStatus.PROCESSING);
                break;
            case "requires_action":
                payment.setStatus(Enums.PaymentStatus.REQUIRES_ACTION);
                break;
            case "processing":
                payment.setStatus(Enums.PaymentStatus.PROCESSING);
                break;
            case "canceled":
                payment.setStatus(Enums.PaymentStatus.CANCELLED);
                break;
            default:
                payment.setStatus(Enums.PaymentStatus.FAILED);
        }
        paymentRepository.save(payment);
    }

    private void handlePaymentSucceeded(com.stripe.model.Event event){
        PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
        if(intent == null) return;

        Payment payment = paymentRepository.findByStripePaymentIntentId(intent.getId())
                .orElseThrow(()-> new ResourceNotFoundException("Payment not found for intent: " + intent.getId()));

        payment.setStatus(Enums.PaymentStatus.SUCCEEDED);
        payment.setPaymentDate(LocalDateTime.now());
        paymentRepository.save(payment);

        orderService.updateOrderStatus(payment.getId(), Enums.OrderStatus.CONFIRMED);
        log.info("Payment succeeded for intent: {}", intent.getId());
    }

    private void handlePaymentFailed(com.stripe.model.Event event){
        PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
        if(intent == null) return;

        Payment payment = paymentRepository.findByStripePaymentIntentId(intent.getId())
                .orElseThrow(()-> new ResourceNotFoundException("Payment not found for intent: {}" +intent.getId()));
        payment.setStatus(Enums.PaymentStatus.FAILED);
        paymentRepository.save(payment);

        log.warn("Payment failed for intent: {}", intent.getId());

    }

    private void handelPaymentRequiresAction(com.stripe.model.Event event){
        PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
        if(intent == null) return;

        Payment payment = paymentRepository.findByStripePaymentIntentId(intent.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for intent: {}" +intent.getId()));

        payment.setStatus(Enums.PaymentStatus.REQUIRES_ACTION);
        paymentRepository.save(payment);

        log.info("Payment requires action for intent: {}", intent.getId());
    }

    private void updateOrderStatus(Order order, Enums.OrderStatus status) {
        // You'll need to implement this in your OrderService
        // order.setStatus(status);
        // orderRepository.save(order);
        log.info("Order status updated to: {} for order: {}", status, order.getId());
    }
}
