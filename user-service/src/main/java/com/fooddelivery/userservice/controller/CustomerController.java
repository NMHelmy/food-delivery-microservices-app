package com.fooddelivery.userservice.controller;

import com.fooddelivery.userservice.dto.AddressDTO;
import com.fooddelivery.userservice.dto.CustomerProfileDTO;
import com.fooddelivery.userservice.feign.AuthServiceClient;
import com.fooddelivery.userservice.model.Address;
import com.fooddelivery.userservice.model.CustomerProfile;
import com.fooddelivery.userservice.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private AuthServiceClient authServiceClient;

    // Helper method to validate token
    private Long validateTokenAndGetUserId(String token) {
        Map<String, Object> response = authServiceClient.validateToken(Map.of("token", token));

        if (!(Boolean) response.get("valid")) {
            throw new RuntimeException("Invalid or expired token");
        }

        return ((Number) response.get("userId")).longValue();
    }

    @PostMapping("/profile")
    public ResponseEntity<?> createProfile(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody CustomerProfileDTO dto) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = validateTokenAndGetUserId(token);

            // Ensure the userId in DTO matches the token
            dto.setUserId(userId);

            CustomerProfile profile = customerService.createCustomerProfile(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getProfile(@PathVariable Long userId) {
        try {
            CustomerProfile profile = customerService.getCustomerProfile(userId);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/profile/{userId}")
    public ResponseEntity<?> updateProfile(
            @PathVariable Long userId,
            @Valid @RequestBody CustomerProfileDTO dto) {
        try {
            CustomerProfile profile = customerService.updateCustomerProfile(userId, dto);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Address endpoints
    @PostMapping("/address")
    public ResponseEntity<?> addAddress(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody AddressDTO dto) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = validateTokenAndGetUserId(token);

            dto.setUserId(userId);

            Address address = customerService.addAddress(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(address);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{userId}/addresses")
    public ResponseEntity<?> getAddresses(@PathVariable Long userId) {
        try {
            List<Address> addresses = customerService.getAddressesByUserId(userId);
            return ResponseEntity.ok(addresses);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{userId}/address/default")
    public ResponseEntity<?> getDefaultAddress(@PathVariable Long userId) {
        try {
            Address address = customerService.getDefaultAddress(userId);
            return ResponseEntity.ok(address);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/address/{addressId}")
    public ResponseEntity<?> updateAddress(
            @PathVariable Long addressId,
            @Valid @RequestBody AddressDTO dto) {
        try {
            Address address = customerService.updateAddress(addressId, dto);
            return ResponseEntity.ok(address);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/address/{addressId}")
    public ResponseEntity<?> deleteAddress(@PathVariable Long addressId) {
        try {
            customerService.deleteAddress(addressId);
            return ResponseEntity.ok(Map.of("message", "Address deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}