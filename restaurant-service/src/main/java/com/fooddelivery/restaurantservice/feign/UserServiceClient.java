package com.fooddelivery.restaurantservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Map;

@FeignClient(name = "auth-service")
public interface UserServiceClient {
    @GetMapping("/user/{userId}")
    Object getUserById(@PathVariable Long userId);
}