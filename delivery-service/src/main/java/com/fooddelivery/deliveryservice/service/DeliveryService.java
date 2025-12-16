package com.fooddelivery.deliveryservice.service;

import com.fooddelivery.deliveryservice.dto.*;
import com.fooddelivery.deliveryservice.exception.BadRequestException;
import com.fooddelivery.deliveryservice.exception.ResourceNotFoundException;
import com.fooddelivery.deliveryservice.feign.OrderServiceClient;
import com.fooddelivery.deliveryservice.feign.UserServiceClient;
import com.fooddelivery.deliveryservice.model.Delivery;
import com.fooddelivery.deliveryservice.model.DeliveryStatus;
import com.fooddelivery.deliveryservice.repository.DeliveryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeliveryService {

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private OrderServiceClient orderServiceClient;

    @Autowired
    private UserServiceClient userServiceClient;

    @Transactional
    public DeliveryResponseDTO createDelivery(CreateDeliveryDTO dto) {
        // Validate order exists
        validateOrderExists(dto.getOrderId());

        // Check if delivery already exists for this order
        if (deliveryRepository.findByOrderId(dto.getOrderId()).isPresent()) {
            throw new BadRequestException("Delivery already exists for order ID: " + dto.getOrderId());
        }

        Delivery delivery = new Delivery();
        delivery.setOrderId(dto.getOrderId());
        delivery.setCustomerId(dto.getCustomerId());
        delivery.setRestaurantId(dto.getRestaurantId());
        delivery.setDeliveryAddressId(dto.getDeliveryAddressId());
        delivery.setRestaurantAddress(dto.getRestaurantAddress());
        delivery.setDeliveryAddress(dto.getDeliveryAddress());
        delivery.setEstimatedDeliveryTime(dto.getEstimatedDeliveryTime());
        delivery.setDeliveryNotes(dto.getDeliveryNotes());
        delivery.setStatus(DeliveryStatus.PENDING);

        Delivery savedDelivery = deliveryRepository.save(delivery);
        return convertToResponseDTO(savedDelivery);
    }

    public DeliveryResponseDTO getDeliveryById(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found with id: " + deliveryId));
        return convertToResponseDTO(delivery);
    }

    public DeliveryResponseDTO getDeliveryByOrderId(Long orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found for order ID: " + orderId));
        return convertToResponseDTO(delivery);
    }

    public List<DeliveryResponseDTO> getDeliveriesByCustomerId(Long customerId) {
        List<Delivery> deliveries = deliveryRepository.findByCustomerId(customerId);
        return deliveries.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<DeliveryResponseDTO> getDeliveriesByDriverId(Long driverId) {
        List<Delivery> deliveries = deliveryRepository.findByDriverId(driverId);
        return deliveries.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<DeliveryResponseDTO> getDeliveriesByRestaurantId(Long restaurantId) {
        List<Delivery> deliveries = deliveryRepository.findByRestaurantId(restaurantId);
        return deliveries.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public DeliveryResponseDTO updateDeliveryStatus(Long deliveryId, UpdateDeliveryStatusDTO dto) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found with id: " + deliveryId));

        // Validate status transition
        validateStatusTransition(delivery.getStatus(), dto.getStatus());

        delivery.setStatus(dto.getStatus());

        // Set timestamps based on status
        if (dto.getStatus() == DeliveryStatus.PICKED_UP && delivery.getPickupTime() == null) {
            delivery.setPickupTime(LocalDateTime.now());
        } else if (dto.getStatus() == DeliveryStatus.DELIVERED && delivery.getDeliveryTime() == null) {
            delivery.setDeliveryTime(LocalDateTime.now());
        }

        Delivery updatedDelivery = deliveryRepository.save(delivery);
        return convertToResponseDTO(updatedDelivery);
    }

    @Transactional
    public DeliveryResponseDTO assignDriver(Long deliveryId, AssignDriverDTO dto) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found with id: " + deliveryId));

        // Validate driver exists
        validateDriverExists(dto.getDriverId());

        // Check if delivery can be assigned
        if (delivery.getStatus() != DeliveryStatus.PENDING) {
            throw new BadRequestException("Can only assign driver to deliveries in PENDING status");
        }

        delivery.setDriverId(dto.getDriverId());
        delivery.setStatus(DeliveryStatus.ASSIGNED);

        Delivery updatedDelivery = deliveryRepository.save(delivery);
        return convertToResponseDTO(updatedDelivery);
    }

    @Transactional
    public DeliveryResponseDTO updateDriverLocation(Long deliveryId, UpdateDriverLocationDTO dto, Long driverId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found with id: " + deliveryId));

        // Verify driver is assigned to this delivery
        if (!driverId.equals(delivery.getDriverId())) {
            throw new BadRequestException("You are not assigned to this delivery");
        }

        delivery.setDriverLatitude(dto.getLatitude());
        delivery.setDriverLongitude(dto.getLongitude());
        delivery.setLastLocationUpdate(LocalDateTime.now());

        Delivery updatedDelivery = deliveryRepository.save(delivery);
        return convertToResponseDTO(updatedDelivery);
    }

    @Transactional
    public DeliveryResponseDTO confirmPickup(Long deliveryId, Long driverId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found with id: " + deliveryId));

        // Verify driver is assigned to this delivery
        if (!driverId.equals(delivery.getDriverId())) {
            throw new BadRequestException("You are not assigned to this delivery");
        }

        if (delivery.getStatus() != DeliveryStatus.ASSIGNED) {
            throw new BadRequestException("Delivery must be in ASSIGNED status to confirm pickup");
        }

        delivery.setStatus(DeliveryStatus.PICKED_UP);
        delivery.setPickupTime(LocalDateTime.now());

        Delivery updatedDelivery = deliveryRepository.save(delivery);
        return convertToResponseDTO(updatedDelivery);
    }

    @Transactional
    public DeliveryResponseDTO confirmDelivery(Long deliveryId, Long driverId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found with id: " + deliveryId));

        // Verify driver is assigned to this delivery
        if (!driverId.equals(delivery.getDriverId())) {
            throw new BadRequestException("You are not assigned to this delivery");
        }

        if (delivery.getStatus() != DeliveryStatus.IN_TRANSIT) {
            throw new BadRequestException("Delivery must be in IN_TRANSIT status to confirm delivery");
        }

        delivery.setStatus(DeliveryStatus.DELIVERED);
        delivery.setDeliveryTime(LocalDateTime.now());

        Delivery updatedDelivery = deliveryRepository.save(delivery);
        return convertToResponseDTO(updatedDelivery);
    }

    private void validateOrderExists(Long orderId) {
        try {
            orderServiceClient.getOrder(orderId);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Order not found with id: " + orderId);
        }
    }

    private void validateDriverExists(Long driverId) {
        try {
            userServiceClient.getDriverProfile(driverId);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Driver profile not found for user id: " + driverId);
        }
    }

    private void validateStatusTransition(DeliveryStatus currentStatus, DeliveryStatus newStatus) {
        boolean isValid = switch (currentStatus) {
            case PENDING -> newStatus == DeliveryStatus.ASSIGNED ||
                    newStatus == DeliveryStatus.CANCELLED;
            case ASSIGNED -> newStatus == DeliveryStatus.PICKED_UP ||
                    newStatus == DeliveryStatus.CANCELLED;
            case PICKED_UP -> newStatus == DeliveryStatus.IN_TRANSIT ||
                    newStatus == DeliveryStatus.CANCELLED;
            case IN_TRANSIT -> newStatus == DeliveryStatus.DELIVERED ||
                    newStatus == DeliveryStatus.CANCELLED;
            case DELIVERED, CANCELLED -> false; // Terminal states
        };

        if (!isValid) {
            throw new BadRequestException(
                    "Invalid status transition from " + currentStatus + " to " + newStatus
            );
        }
    }

    private DeliveryResponseDTO convertToResponseDTO(Delivery delivery) {
        DeliveryResponseDTO dto = new DeliveryResponseDTO();
        dto.setId(delivery.getId());
        dto.setOrderId(delivery.getOrderId());
        dto.setCustomerId(delivery.getCustomerId());
        dto.setRestaurantId(delivery.getRestaurantId());
        dto.setDriverId(delivery.getDriverId());
        dto.setDeliveryAddressId(delivery.getDeliveryAddressId());
        dto.setStatus(delivery.getStatus());
        dto.setRestaurantAddress(delivery.getRestaurantAddress());
        dto.setDeliveryAddress(delivery.getDeliveryAddress());
        dto.setDriverLatitude(delivery.getDriverLatitude());
        dto.setDriverLongitude(delivery.getDriverLongitude());
        dto.setLastLocationUpdate(delivery.getLastLocationUpdate());
        dto.setPickupTime(delivery.getPickupTime());
        dto.setDeliveryTime(delivery.getDeliveryTime());
        dto.setEstimatedDeliveryTime(delivery.getEstimatedDeliveryTime());
        dto.setDeliveryNotes(delivery.getDeliveryNotes());
        dto.setCreatedAt(delivery.getCreatedAt());
        dto.setUpdatedAt(delivery.getUpdatedAt());
        return dto;
    }
}
