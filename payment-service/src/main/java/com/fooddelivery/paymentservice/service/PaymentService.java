package com.fooddelivery.paymentservice.service;

import java.util.List;

import com.fooddelivery.paymentservice.dto.PaymentRequest;
import com.fooddelivery.paymentservice.dto.PaymentResponse;

public interface PaymentService {

    PaymentResponse createPayment(PaymentRequest request, Long userId);

    PaymentResponse confirmPayment(Long paymentId, Long userId);

    PaymentResponse cancelPayment(Long paymentId, Long userId);

    PaymentResponse refundPayment(Long paymentId);

    PaymentResponse getPaymentById(Long paymentId, Long userId, String role);

    PaymentResponse getPaymentByOrderId(Long orderId, Long userId, String role);

    List<PaymentResponse> getPaymentsByUser(Long userId);

    List<PaymentResponse> getAllPayments();

    List<PaymentResponse> getPaymentsByRestaurant(
            Long restaurantId,
            Long userId,
            String role
    );
}
