package com.fooddelivery.deliveryservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", path = "/users")
public interface UserServiceClient {

    @GetMapping("/driver/profile/{userId}")
    Object getDriverProfile(@PathVariable Long userId);

    @GetMapping("/customer/profile/{userId}")
    Object getCustomerProfile(@PathVariable Long userId);

    @GetMapping("/addresses/{addressId}")
    Object getAddress(@PathVariable Long addressId);
}
