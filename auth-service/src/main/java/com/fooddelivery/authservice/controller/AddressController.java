package com.fooddelivery.authservice.controller;

import com.fooddelivery.authservice.dto.AddressDTO;
import com.fooddelivery.authservice.model.Address;
import com.fooddelivery.authservice.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/addresses")
public class AddressController {

    @Autowired
    private AddressService addressService;

    // AUTHENTICATED USERS

    @PostMapping
    public ResponseEntity<Address> createAddress(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody AddressDTO addressDTO) {

        Address address =
                addressService.createAddress(userId, addressDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(address);
    }

    @GetMapping("/my-addresses")
    public ResponseEntity<List<Address>> getMyAddresses(
            @RequestHeader("X-User-Id") Long userId) {

        return ResponseEntity.ok(
                addressService.getUserAddresses(userId)
        );
    }

    @GetMapping("/default")
    public ResponseEntity<Address> getDefaultAddress(
            @RequestHeader("X-User-Id") Long userId) {

        Address address = addressService.getDefaultAddress(userId);
        return ResponseEntity.ok(address);
    }

    @GetMapping("/{addressId}")
    public ResponseEntity<Address> getAddress(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long addressId) {

        Address address =
                addressService.getAddressByIdAndUserId(addressId, userId);

        return ResponseEntity.ok(address);
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<Address> updateAddress(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long addressId,
            @Valid @RequestBody AddressDTO addressDTO) {

        Address address =
                addressService.updateAddress(userId, addressId, addressDTO);

        return ResponseEntity.ok(address);
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<String> deleteAddress(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long addressId) {

        addressService.deleteAddress(userId, addressId);
        return ResponseEntity.ok("Address deleted successfully");
    }

    @PutMapping("/{addressId}/set-default")
    public ResponseEntity<Address> setDefaultAddress(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long addressId) {

        Address address =
                addressService.setDefaultAddress(userId, addressId);

        return ResponseEntity.ok(address);
    }

    // ADMIN

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Address>> getUserAddresses(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                addressService.getUserAddresses(userId)
        );
    }

    // INTERNAL

    @GetMapping("/internal/{addressId}")
    public ResponseEntity<Address> getAddressById(
            @PathVariable Long addressId) {

        Address address =
                addressService.getAddressById(addressId);

        return ResponseEntity.ok(address);
    }

    // INTERNAL - Verify address ownership
    @GetMapping("/internal/{addressId}/verify-owner/{userId}")
    public ResponseEntity<Boolean> verifyAddressOwnership(
            @PathVariable Long addressId,
            @PathVariable Long userId,
            @RequestHeader(value = "X-Internal-Request", required = false) String internalHeader) {

        // Security check: only allow internal service calls
        if (!"true".equals(internalHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        boolean isOwner = addressService.isAddressOwnedByUser(addressId, userId);
        return ResponseEntity.ok(isOwner);
    }
}
