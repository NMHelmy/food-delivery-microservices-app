package com.fooddelivery.orderservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    // Get customer profile
    @GetMapping("/users/customer/profile/{userId}")
    Map<String, Object> getCustomerProfile(@PathVariable("userId") Long userId);

    // Get driver profile
    @GetMapping("/users/driver/profile/{userId}")
    Map<String, Object> getDriverProfile(@PathVariable("userId") Long userId);

    // Verify address exists and belongs to customer
    @GetMapping("/users/customer/{userId}/addresses")
    Object getCustomerAddresses(@PathVariable("userId") Long userId);
}