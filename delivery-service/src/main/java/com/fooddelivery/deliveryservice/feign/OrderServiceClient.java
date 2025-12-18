package com.fooddelivery.deliveryservice.feign;

import com.fooddelivery.deliveryservice.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "order-service", path = "/orders", configuration = FeignConfig.class)
public interface OrderServiceClient {

    @GetMapping("/{orderId}")
    Object getOrder(@PathVariable Long orderId);
}
