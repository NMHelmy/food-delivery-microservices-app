package com.fooddelivery.orderservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "auth-service")
public interface UserServiceClient {

    // Get user profile (used to verify customer exists)
    @GetMapping("/auth/user/{userId}")
    Object getUserById(@PathVariable Long userId);

    // Get specific address (ownership enforced inside AddressService)
    @GetMapping("/addresses/internal/{addressId}")
    Object getAddressById(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long addressId
    );

    // Get all addresses for a user (admin/internal use if needed)
    @GetMapping("/addresses/user/{userId}")
    Object getUserAddresses(@PathVariable Long userId);
}
