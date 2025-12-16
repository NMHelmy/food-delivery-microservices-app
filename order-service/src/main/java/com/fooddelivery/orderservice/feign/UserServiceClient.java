package com.fooddelivery.orderservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

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
    @GetMapping("/users/customer/addresses/{addressId}")
    Object getCustomerAddresses(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role,
            @PathVariable("addressId") Long addressId
    );
}