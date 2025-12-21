package com.fooddelivery.deliveryservice.feign;

import com.fooddelivery.deliveryservice.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "order-service", path = "/orders", configuration = FeignConfig.class)
public interface OrderServiceClient {

    @GetMapping("/{orderId}")
    Object getOrder(@PathVariable Long orderId);


    @PutMapping("/{orderId}/status")
    Object updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody java.util.Map<String, String> statusUpdate,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role
    );
}
