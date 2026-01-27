package com.fooddelivery.deliveryservice.controller;

import com.fooddelivery.deliveryservice.dto.*;
import com.fooddelivery.deliveryservice.service.DeliveryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/deliveries")
public class DeliveryController {

    @Autowired
    private DeliveryService deliveryService;

    // ADMIN / RESTAURANT OWNER

    @PostMapping
    public ResponseEntity<DeliveryResponseDTO> createDelivery(
            @Valid @RequestBody CreateDeliveryDTO dto) {

        DeliveryResponseDTO delivery = deliveryService.createDelivery(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(delivery);
    }

    @PutMapping("/{deliveryId}/status")
    public ResponseEntity<DeliveryResponseDTO> updateDeliveryStatus(
            @PathVariable Long deliveryId,
            @Valid @RequestBody UpdateDeliveryStatusDTO dto) {

        DeliveryResponseDTO delivery =
                deliveryService.updateDeliveryStatus(deliveryId, dto);

        return ResponseEntity.ok(delivery);
    }

    @PostMapping("/{deliveryId}/assign-driver")
    public ResponseEntity<DeliveryResponseDTO> assignDriver(
            @PathVariable Long deliveryId,
            @Valid @RequestBody AssignDriverDTO dto) {

        DeliveryResponseDTO delivery =
                deliveryService.assignDriver(deliveryId, dto);

        return ResponseEntity.ok(delivery);
    }

    // CUSTOMER

    @GetMapping("/my-deliveries")
    public ResponseEntity<List<DeliveryResponseDTO>> getMyDeliveries(
            @RequestHeader("X-User-Id") Long customerId) {

        return ResponseEntity.ok(
                deliveryService.getDeliveriesByCustomerId(customerId)
        );
    }

    @GetMapping("/my-order/{orderId}")
    public ResponseEntity<DeliveryResponseDTO> getMyDeliveryByOrderId(
            @PathVariable Long orderId,
            @RequestHeader("X-User-Id") Long customerId) {

        DeliveryResponseDTO delivery =
                deliveryService.getDeliveryByOrderId(orderId);

        return ResponseEntity.ok(delivery);
    }

    // ADMIN

    @GetMapping("/admin/all")
    public ResponseEntity<List<DeliveryResponseDTO>> getAllDeliveries() {
        return ResponseEntity.ok(
                deliveryService.getAllDeliveries()
        );
    }

    @GetMapping("/{deliveryId}")
    public ResponseEntity<DeliveryResponseDTO> getDeliveryById(
            @PathVariable Long deliveryId) {

        return ResponseEntity.ok(
                deliveryService.getDeliveryById(deliveryId)
        );
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<DeliveryResponseDTO>> getDeliveriesByCustomerId(
            @PathVariable Long customerId) {

        return ResponseEntity.ok(
                deliveryService.getDeliveriesByCustomerId(customerId)
        );
    }

    @GetMapping("/admin/driver/{driverId}")
    public ResponseEntity<List<DeliveryResponseDTO>> getDeliveriesByDriverId(
            @PathVariable Long driverId) {

        return ResponseEntity.ok(
                deliveryService.getDeliveriesByDriverId(driverId)
        );
    }

    @GetMapping("/admin/restaurant/{restaurantId}")
    public ResponseEntity<List<DeliveryResponseDTO>> getDeliveriesByRestaurantId(
            @PathVariable Long restaurantId) {

        return ResponseEntity.ok(
                deliveryService.getDeliveriesByRestaurantId(restaurantId)
        );
    }

    // RESTAURANT OWNER

    @GetMapping("/my-restaurant-deliveries")
    public ResponseEntity<List<DeliveryResponseDTO>> getMyRestaurantDeliveries(
            @RequestHeader("X-User-Id") Long restaurantOwnerId) {

        return ResponseEntity.ok(
                deliveryService.getDeliveriesByRestaurantOwnerId(restaurantOwnerId)
        );
    }

    // DELIVERY DRIVER

    @GetMapping("/my-driver-deliveries")
    public ResponseEntity<List<DeliveryResponseDTO>> getMyDriverDeliveries(
            @RequestHeader("X-User-Id") Long driverId) {

        return ResponseEntity.ok(
                deliveryService.getDeliveriesByDriverId(driverId)
        );
    }

    @GetMapping("/driver/active")
    public ResponseEntity<List<DeliveryResponseDTO>> getMyActiveDeliveries(
            @RequestHeader("X-User-Id") Long driverId) {

        return ResponseEntity.ok(
                deliveryService.getActiveDeliveriesByDriverId(driverId)
        );
    }

    @PutMapping("/{deliveryId}/pickup-confirmation")
    public ResponseEntity<DeliveryResponseDTO> confirmPickup(
            @PathVariable Long deliveryId,
            @RequestHeader("X-User-Id") Long driverId) {

        DeliveryResponseDTO delivery =
                deliveryService.confirmPickup(deliveryId, driverId);

        return ResponseEntity.ok(delivery);
    }

    @PutMapping("/{deliveryId}/delivery-confirmation")
    public ResponseEntity<DeliveryResponseDTO> confirmDelivery(
            @PathVariable Long deliveryId,
            @RequestHeader("X-User-Id") Long driverId) {

        DeliveryResponseDTO delivery =
                deliveryService.confirmDelivery(deliveryId, driverId);

        return ResponseEntity.ok(delivery);
    }
}
