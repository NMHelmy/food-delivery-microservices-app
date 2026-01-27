package com.fooddelivery.paymentservice.service;

import java.util.List;

import com.fooddelivery.paymentservice.dto.PaymentResponse;

public interface PaymentService {

    PaymentResponse createPayment(Long orderId, String paymentMethod, Long userId);

    PaymentResponse confirmPayment(Long paymentId, Long userId);

    PaymentResponse cancelPayment(Long paymentId, Long userId);

    PaymentResponse refundPayment(Long paymentId);

    PaymentResponse getPaymentById(Long paymentId, Long userId);

    PaymentResponse getPaymentByOrderId(Long orderId, Long userId);

    List<PaymentResponse> getPaymentsByUser(Long userId);

    List<PaymentResponse> getAllPayments();

    List<PaymentResponse> getPaymentsByRestaurant(Long restaurantId);
}
