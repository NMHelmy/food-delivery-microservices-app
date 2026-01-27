package com.fooddelivery.paymentservice.models;

/**
 * Payment lifecycle states.
 *
 * Valid transitions:
 *
 * PENDING   -> CONFIRMED
 * PENDING   -> CANCELLED
 * PENDING   -> FAILED
 *
 * CONFIRMED -> REFUNDED
 *
 * Terminal states:
 * - CANCELLED
 * - FAILED
 * - REFUNDED
 *
 * Once a payment reaches a terminal state,
 * it MUST NOT transition again.
 */
public enum PaymentStatus {

    PENDING,

    CONFIRMED,

    FAILED,

    CANCELLED,

    REFUNDED
}
