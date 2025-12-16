package com.fooddelivery.userservice.controller;

import com.fooddelivery.userservice.dto.AddressDTO;
import com.fooddelivery.userservice.dto.CustomerProfileDTO;
import com.fooddelivery.userservice.model.Address;
import com.fooddelivery.userservice.model.CustomerProfile;
import com.fooddelivery.userservice.service.CustomerService;
import com.fooddelivery.userservice.exception.UnauthorizedException;
import com.fooddelivery.userservice.exception.BadRequestException;
import jakarta.servlet.http.HttpServletRequest;
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

    private Long getUserIdFromHeader(HttpServletRequest request) {
        String userIdHeader = request.getHeader("X-User-Id");
        if (userIdHeader == null || userIdHeader.isEmpty()) {
            throw new UnauthorizedException("User ID not found in request headers");
        }
        try {
            return Long.parseLong(userIdHeader);
        } catch (NumberFormatException e) {
            throw new UnauthorizedException("Invalid User ID format");
        }
    }

    private String getUserRoleFromHeader(HttpServletRequest request) {
        String roleHeader = request.getHeader("X-User-Role");
        if (roleHeader == null || roleHeader.isEmpty()) {
            throw new UnauthorizedException("User role not found in request headers");
        }
        return roleHeader;
    }

    private void validateCustomerRole(HttpServletRequest request) {
        String role = getUserRoleFromHeader(request);
        if (!"CUSTOMER".equals(role)) {
            throw new UnauthorizedException("Only CUSTOMER role can access this endpoint");
        }
    }

    @PostMapping("/profile")
    public ResponseEntity<CustomerProfile> createProfile(
            @Valid @RequestBody CustomerProfileDTO dto,
            HttpServletRequest request) {

        Long userId = getUserIdFromHeader(request);
        validateCustomerRole(request);

        if (dto.getUserId() != null && !dto.getUserId().equals(userId)) {
            throw new UnauthorizedException("You can only create profiles for yourself");
        }

        dto.setUserId(userId);
        CustomerProfile profile = customerService.createCustomerProfile(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(profile);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromHeader(request);
            validateCustomerRole(request);

            CustomerProfile profile = customerService.getCustomerProfile(userId);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @Valid @RequestBody CustomerProfileDTO dto,
            HttpServletRequest request) {
        try {
            Long userId = getUserIdFromHeader(request);
            validateCustomerRole(request);

            CustomerProfile profile = customerService.updateCustomerProfile(userId, dto);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/addresses")
    public ResponseEntity<?> createAddress(
            @Valid @RequestBody AddressDTO dto,
            HttpServletRequest request) {
        try {
            Long userId = getUserIdFromHeader(request);
            validateCustomerRole(request);

            dto.setUserId(userId);

            Address address = customerService.createAddress(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(address);
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create address: " + e.getMessage()));
        }
    }

    @GetMapping("/addresses")
    public ResponseEntity<?> getAddresses(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromHeader(request);
            validateCustomerRole(request);

            List<Address> addresses = customerService.getAddressesByUserId(userId);
            return ResponseEntity.ok(addresses);
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/addresses/default")
    public ResponseEntity<?> getDefaultAddress(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromHeader(request);
            validateCustomerRole(request);

            Address address = customerService.getDefaultAddress(userId);
            return ResponseEntity.ok(address);
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/addresses/{addressId}")
    public ResponseEntity<?> getAddress(
            @PathVariable Long addressId,
            HttpServletRequest request) {
        try {
            Long userId = getUserIdFromHeader(request);
            validateCustomerRole(request);

            Address address = customerService.getAddressById(addressId);

            if (!address.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You can only view your own addresses"));
            }

            return ResponseEntity.ok(address);
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<?> updateAddress(
            @PathVariable Long addressId,
            @Valid @RequestBody AddressDTO dto,
            HttpServletRequest request) {
        try {
            Long userId = getUserIdFromHeader(request);
            validateCustomerRole(request);

            Address existingAddress = customerService.getAddressById(addressId);
            if (!existingAddress.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You can only update your own addresses"));
            }

            Address address = customerService.updateAddress(addressId, dto);
            return ResponseEntity.ok(address);
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<?> deleteAddress(
            @PathVariable Long addressId,
            HttpServletRequest request) {
        try {
            Long userId = getUserIdFromHeader(request);
            validateCustomerRole(request);

            Address address = customerService.getAddressById(addressId);
            if (!address.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You can only delete your own addresses"));
            }

            customerService.deleteAddress(addressId);
            return ResponseEntity.ok(Map.of("message", "Address deleted successfully"));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

     // This endpoint is for internal service-to-service communication.
     // It does NOT require authentication headers.
     // WARNING: Should NOT be exposed through API Gateway!
    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getProfileByUserId(@PathVariable Long userId) {
        try {
            CustomerProfile profile = customerService.getCustomerProfile(userId);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}