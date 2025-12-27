package com.fooddelivery.paymentservice.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
/**
 * Payment Entity
 * --------------
 * This entity represents a PAYMENT record in the payment-service database.
 *
 * One Payment = One Order
 *
 * This table is the SOURCE OF TRUTH for:
 * - payment amount
 * - payment status
 * - payment ownership

 */
@Entity
/**
 * Enforces a critical business rule at the DATABASE level:
 *
 * An order can only have ONE payment.
 *
 * This prevents:
 * - double payment attempts
 * - race conditions
 * - duplicate rows from bugs
 */
@Table(
        name = "payments",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "orderId")
        }
)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long restaurantId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false)
    private String paymentMethod;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /* ===================== */
    /* Lifecycle Hooks       */
    /* ===================== */

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /* ===================== */
    /* Constructors          */
    /* ===================== */

    public Payment() {
    }

    /* ===================== */
    /* Getters               */
    /* ===================== */

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /* ===================== */
    /* Setters               */
    /* ===================== */

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
