package com.fooddelivery.paymentservice.service;

import org.springframework.stereotype.Service;

import com.fooddelivery.paymentservice.dto.PaymentRequest;
import com.fooddelivery.paymentservice.dto.PaymentResponse;
import com.fooddelivery.paymentservice.exception.PaymentNotFoundException;
import com.fooddelivery.paymentservice.models.Payment;
import com.fooddelivery.paymentservice.models.PaymentStatus;
import com.fooddelivery.paymentservice.repository.PaymentRepository;

@Service
public class PaymentService {

    private final PaymentRepository repository;

    public PaymentService(PaymentRepository repository) {
        this.repository = repository;
    }

    public PaymentResponse createPayment(PaymentRequest request) {

        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setAmount(request.getAmount());
        payment.setStatus(PaymentStatus.PENDING);

        repository.save(payment);

        return new PaymentResponse(
                payment.getId(),
                payment.getStatus().name(),
                "Payment created"
        );
    }

    public PaymentResponse confirmPayment(Long id) {

        Payment payment = repository.findById(id)
        .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));

        payment.setStatus(PaymentStatus.SUCCESS);
        repository.save(payment);

        return new PaymentResponse(
                payment.getId(),
                payment.getStatus().name(),
                "Payment successful"
        );
    }
}
