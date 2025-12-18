package com.fooddelivery.paymentservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "order-service")
public interface OrderClient {

    @PostMapping("/orders/{orderId}/paid")
    void markOrderAsPaid(@PathVariable Long orderId);
}
