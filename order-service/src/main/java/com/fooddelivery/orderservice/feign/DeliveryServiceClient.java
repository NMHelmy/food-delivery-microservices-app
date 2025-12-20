package com.fooddelivery.orderservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Map;

@FeignClient(name = "delivery-service")
public interface DeliveryServiceClient {

    @PostMapping("/deliveries")
    Map<String, Object> createDelivery(@RequestBody Map<String, Object> request);
}
