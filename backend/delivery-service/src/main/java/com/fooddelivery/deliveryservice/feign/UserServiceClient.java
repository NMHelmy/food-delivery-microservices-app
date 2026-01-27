package com.fooddelivery.deliveryservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service")
public interface UserServiceClient {
    @GetMapping("/drivers/{driverId}")
    Object getDriverById(@PathVariable Long driverId);

    @GetMapping("/auth/user/{userId}")
    Object getUserById(@PathVariable Long userId);
}