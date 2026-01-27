package com.fooddelivery.orderservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "delivery-service")
public interface DeliveryServiceClient {

    @PostMapping("/deliveries")
    Map<String, Object> createDelivery(
            @RequestBody Map<String, Object> createDeliveryRequest,
            @RequestHeader("X-Internal-Call") String internalCall
    );
}
