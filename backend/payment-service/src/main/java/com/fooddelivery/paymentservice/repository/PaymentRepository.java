package com.fooddelivery.paymentservice.repository;

import com.fooddelivery.paymentservice.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * There can be ONLY ONE payment per order.
     */
    Optional<Payment> findByOrderId(Long orderId);

    /**
     * Check if a payment already exists for an order.
     * Used to enforce idempotency.
     */
    boolean existsByOrderId(Long orderId);

    /**
     * Payments belonging to a specific user.
     */
    List<Payment> findByUserId(Long userId);

    /**
     * Payments belonging to a restaurant.
     */
    List<Payment> findByRestaurantId(Long restaurantId);
}
