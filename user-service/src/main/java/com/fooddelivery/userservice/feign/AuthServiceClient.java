package com.fooddelivery.userservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "auth-service")
public interface AuthServiceClient {

    @PostMapping("/auth/validate")
    Map<String, Object> validateToken(@RequestBody Map<String, String> request);

    @GetMapping("/auth/user/{userId}")
    Map<String, Object> getUserInfo(@PathVariable("userId") Long userId);
}