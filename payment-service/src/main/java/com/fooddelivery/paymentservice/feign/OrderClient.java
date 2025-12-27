package com.fooddelivery.paymentservice.feign;

import java.math.BigDecimal;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "order-service")
public interface OrderClient {

    /**
     * Fetch order summary (SOURCE OF TRUTH).
     */
    @GetMapping("/orders/{orderId}")
    OrderSummaryResponse getOrderById(
            @PathVariable Long orderId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role
    );

    /**
     * Mark order as paid AFTER payment confirmation.
     */
    @PostMapping("/orders/{orderId}/paid")
    void markOrderAsPaid(
            @PathVariable Long orderId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role
    );

    /**
     * Minimal DTO — aligned with OrderResponseDTO.
     */
    class OrderSummaryResponse {

        private Long id;
        private Long customerId;
        private Long restaurantId;
        private BigDecimal total;
        private String status;

        public OrderSummaryResponse() {}

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getCustomerId() {
            return customerId;
        }

        public void setCustomerId(Long customerId) {
            this.customerId = customerId;
        }

        public Long getRestaurantId() {
            return restaurantId;
        }

        public void setRestaurantId(Long restaurantId) {
            this.restaurantId = restaurantId;
        }

        public BigDecimal getTotalAmount() {
            return total;
        }

        public void setTotal(BigDecimal total) {
            this.total = total;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
