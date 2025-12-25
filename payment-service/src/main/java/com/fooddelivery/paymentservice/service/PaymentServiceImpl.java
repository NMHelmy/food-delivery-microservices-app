package com.fooddelivery.paymentservice.service;

import com.fooddelivery.paymentservice.dto.PaymentRequest;
import com.fooddelivery.paymentservice.dto.PaymentResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Override
    public PaymentResponse createPayment(PaymentRequest request, Long userId) {
        return null;
    }

    @Override
    public PaymentResponse confirmPayment(Long paymentId, Long userId) {
        return null;
    }

    @Override
    public PaymentResponse cancelPayment(Long paymentId, Long userId) {
        return null;
    }

    @Override
    public PaymentResponse refundPayment(Long paymentId) {
        return null;
    }

    @Override
    public PaymentResponse getPaymentById(Long paymentId, Long userId, String role) {
        return null;
    }

    @Override
    public PaymentResponse getPaymentByOrderId(Long orderId, Long userId, String role) {
        return null;
    }

    @Override
    public List<PaymentResponse> getPaymentsByUser(Long userId) {
        return List.of();
    }

    @Override
    public List<PaymentResponse> getAllPayments() {
        return List.of();
    }

    @Override
    public List<PaymentResponse> getPaymentsByRestaurant(
            Long restaurantId,
            Long userId,
            String role
    ) {
        return List.of();
    }
}
