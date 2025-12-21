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

    @PostMapping
    public ResponseEntity<?> createAddress(
            @RequestHeader("X-User-Id") String userIdHeader,
            @Valid @RequestBody AddressDTO addressDTO) {

        Long userId = Long.parseLong(userIdHeader);
        Address address = addressService.createAddress(userId, addressDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(address);
    }

    @GetMapping("/my-addresses")
    public ResponseEntity<?> getMyAddresses(@RequestHeader("X-User-Id") String userIdHeader) {
        Long userId = Long.parseLong(userIdHeader);
        List<Address> addresses = addressService.getUserAddresses(userId);
        return ResponseEntity.ok(addresses);
    }

    @GetMapping("/{addressId}")
    public ResponseEntity<?> getAddress(
            @RequestHeader("X-User-Id") String userIdHeader,
            @PathVariable Long addressId) {

        Long userId = Long.parseLong(userIdHeader);
        Address address = addressService.getAddressByIdAndUserId(addressId, userId);
        return ResponseEntity.ok(address);
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<?> updateAddress(
            @RequestHeader("X-User-Id") String userIdHeader,
            @PathVariable Long addressId,
            @Valid @RequestBody AddressDTO addressDTO) {

        Long userId = Long.parseLong(userIdHeader);
        Address address = addressService.updateAddress(userId, addressId, addressDTO);
        return ResponseEntity.ok(address);
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<?> deleteAddress(
            @RequestHeader("X-User-Id") String userIdHeader,
            @PathVariable Long addressId) {

        Long userId = Long.parseLong(userIdHeader);
        addressService.deleteAddress(userId, addressId);
        return ResponseEntity.ok().body("Address deleted successfully");
    }

    @PutMapping("/{addressId}/set-default")
    public ResponseEntity<?> setDefaultAddress(
            @RequestHeader("X-User-Id") String userIdHeader,
            @PathVariable Long addressId) {

        Long userId = Long.parseLong(userIdHeader);
        Address address = addressService.setDefaultAddress(userId, addressId);
        return ResponseEntity.ok(address);
    }
}