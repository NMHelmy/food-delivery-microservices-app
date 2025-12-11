package com.fooddelivery.restaurantservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Map;

@FeignClient(name = "user-service")
public interface UserServiceClient {
    @GetMapping("/users/owner/profile/{userId}")
    Map<String, Object> getOwnerProfile(@PathVariable("userId") Long userId);
}