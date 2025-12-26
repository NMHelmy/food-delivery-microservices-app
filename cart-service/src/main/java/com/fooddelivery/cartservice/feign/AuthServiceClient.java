package com.fooddelivery.cartservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "auth-service")
public interface AuthServiceClient {

    @GetMapping("/addresses/internal/{addressId}/verify-owner/{userId}")
    Boolean verifyAddressOwnership(
            @PathVariable Long addressId,
            @PathVariable Long userId,
            @RequestHeader("X-Internal-Request") String internalHeader
    );
}