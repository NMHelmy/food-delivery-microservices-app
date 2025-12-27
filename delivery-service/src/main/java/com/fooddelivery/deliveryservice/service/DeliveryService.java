package com.fooddelivery.deliveryservice.service;


import com.fooddelivery.deliveryservice.dto.*;
import com.fooddelivery.deliveryservice.event.*; //rabbitmq
import com.fooddelivery.deliveryservice.exception.BadRequestException;
import com.fooddelivery.deliveryservice.exception.ResourceNotFoundException;
import com.fooddelivery.deliveryservice.feign.OrderServiceClient;
import com.fooddelivery.deliveryservice.feign.UserServiceClient;
import com.fooddelivery.deliveryservice.model.Delivery;
import com.fooddelivery.deliveryservice.model.DeliveryStatus;
import com.fooddelivery.deliveryservice.repository.DeliveryRepository;
import com.fooddelivery.deliveryservice.feign.RestaurantServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Slf4j
@Service
public class DeliveryService {

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private OrderServiceClient orderServiceClient;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private RestaurantServiceClient restaurantServiceClient;

    @Autowired
    private DeliveryEventPublisher eventPublisher;

    private Long getLongValue(Object value) {
        if (value == null) return null;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof Long) return (Long) value;
        throw new BadRequestException("Invalid numeric value: " + value);
    }

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


        // Simple validation - just check if ID is valid
        if (dto.getDriverId() == null || dto.getDriverId() <= 0) {
            throw new BadRequestException("Invalid driver ID");
        }

        // Check if delivery can be assigned
        if (delivery.getStatus() != DeliveryStatus.PENDING) {
            throw new BadRequestException("Can only assign driver to deliveries in PENDING status");
        }

        delivery.setDriverId(dto.getDriverId());
        delivery.setStatus(DeliveryStatus.ASSIGNED);
        Delivery updatedDelivery = deliveryRepository.save(delivery);

        // PUBLISH EVENT
        try {
            String driverName = "Driver #" + dto.getDriverId();
            eventPublisher.publishDeliveryAssigned(new DeliveryAssignedEvent(
                    updatedDelivery.getId(),
                    updatedDelivery.getOrderId(),
                    updatedDelivery.getCustomerId(),
                    dto.getDriverId(),
                    driverName
            ));
        } catch (Exception e) {
            log.error("Failed to publish DELIVERY_ASSIGNED event: {}", e.getMessage());
        }

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

        // UPDATE ORDER STATUS TO PICKED_UP
        try {
            Map<String, String> orderStatusUpdate = new HashMap<>();
            orderStatusUpdate.put("status", "PICKED_UP");
            orderServiceClient.updateOrderStatus(
                    delivery.getOrderId(),
                    orderStatusUpdate,
                    "1", // System user ID
                    "ADMIN" // System role to bypass authorization
            );
            log.info("Updated order {} status to PICKED_UP", delivery.getOrderId());
        } catch (Exception e) {
            log.error("Failed to update order status: {}", e.getMessage());
            // Don't fail the delivery confirmation if order update fails
        }

        // PUBLISH EVENT
        try {
            String driverName = "Driver #" + driverId;

            eventPublisher.publishDeliveryPickedUp(new DeliveryPickedUpEvent(
                    updatedDelivery.getId(),
                    updatedDelivery.getOrderId(),
                    updatedDelivery.getCustomerId(),
                    driverName
            ));
        } catch (Exception e) {
            log.error("Failed to publish DELIVERY_PICKED_UP event: {}", e.getMessage());
        }
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

        if (delivery.getStatus() != DeliveryStatus.PICKED_UP) {
            throw new BadRequestException("Delivery must be in PICKED_UP status to confirm delivery");
        }

        delivery.setStatus(DeliveryStatus.DELIVERED);
        delivery.setDeliveryTime(LocalDateTime.now());

        Delivery updatedDelivery = deliveryRepository.save(delivery);

        // UPDATE ORDER STATUS TO DELIVERED
        try {
            Map<String, String> orderStatusUpdate = new HashMap<>();
            orderStatusUpdate.put("status", "DELIVERED");
            orderServiceClient.updateOrderStatus(
                    delivery.getOrderId(),
                    orderStatusUpdate,
                    "1", // System user ID
                    "ADMIN" // System role to bypass authorization
            );
            log.info("Updated order {} status to DELIVERED", delivery.getOrderId());
        } catch (Exception e) {
            log.error("Failed to update order status: {}", e.getMessage());
            // Don't fail the delivery confirmation if order update fails
        }

        // EVENT PUBLISHING
        try {
            eventPublisher.publishDeliveryDelivered(new DeliveryDeliveredEvent(
                    updatedDelivery.getId(),
                    updatedDelivery.getOrderId(),
                    updatedDelivery.getCustomerId()
            ));
        } catch (Exception e) {
            log.error("Failed to publish DELIVERY_DELIVERED event: {}", e.getMessage());
        }

        return convertToResponseDTO(updatedDelivery);
    }

    private void validateOrderExists(Long orderId) {
        try {
            orderServiceClient.getOrder(
                    orderId,
                    "1",      // System user ID
                    "ADMIN"   // System role to bypass authorization
            );
        } catch (Exception e) {
            throw new ResourceNotFoundException("Order not found with id: " + orderId);
        }
    }

    private String fetchRestaurantAddress(Long restaurantId) {
        try {
            Object restaurantResponse = restaurantServiceClient.getRestaurant(restaurantId);
            @SuppressWarnings("unchecked")
            Map<String, Object> restaurantMap = (Map<String, Object>) restaurantResponse;

            // Build formatted address from restaurant details
            StringBuilder address = new StringBuilder();

            if (restaurantMap.get("address") != null) {
                address.append(restaurantMap.get("address"));
            }

            if (restaurantMap.get("city") != null) {
                if (address.length() > 0) address.append(", ");
                address.append(restaurantMap.get("city"));
            }

            if (restaurantMap.get("district") != null) {
                if (address.length() > 0) address.append(", ");
                address.append(restaurantMap.get("district"));
            }

            if (address.length() == 0) {
                return "Restaurant Address Not Available";
            }

            return address.toString();

        } catch (Exception e) {
            throw new ResourceNotFoundException("Restaurant not found with id: " + restaurantId);
        }
    }

    public List<DeliveryResponseDTO> getDeliveriesByRestaurantOwnerId(Long restaurantOwnerId) {
        try {
            // Fetch ALL restaurants for this owner from Restaurant Service
            Object restaurantsResponse = restaurantServiceClient.getRestaurantsByOwnerId(restaurantOwnerId);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> restaurantsList = (List<Map<String, Object>>) restaurantsResponse;

            if (restaurantsList.isEmpty()) {
                throw new ResourceNotFoundException("No restaurants found for owner ID: " + restaurantOwnerId);
            }

            // Get deliveries for ALL restaurants owned by this owner
            List<DeliveryResponseDTO> allDeliveries = new ArrayList<>();

            for (Map<String, Object> restaurantMap : restaurantsList) {
                Long restaurantId = getLongValue(restaurantMap.get("id"));
                List<DeliveryResponseDTO> restaurantDeliveries = getDeliveriesByRestaurantId(restaurantId);
                allDeliveries.addAll(restaurantDeliveries);
            }

            return allDeliveries;

        } catch (Exception e) {
            throw new ResourceNotFoundException("Restaurant not found for owner id: " + restaurantOwnerId);
        }
    }

    /*private Map<String, Object> validateDriverExists(Long driverId) {
        try {
            // Call user-service to get driver info and cast the response
            Object response = userServiceClient.getDriverById(driverId);

            @SuppressWarnings("unchecked")
            Map<String, Object> driverData = (Map<String, Object>) response;

            // Validate that data was returned
            if (driverData == null || driverData.isEmpty()) {
                throw new ResourceNotFoundException("Driver not found for user id: " + driverId);
            }

            // Optional: Validate driver status if needed
            Object driverStatus = driverData.get("driverStatus");
            if (driverStatus != null && "UNAVAILABLE".equals(driverStatus.toString())) {
                log.warn("Driver {} is currently unavailable but assignment allowed", driverId);
            }

            return driverData;

        } catch (feign.FeignException.NotFound e) {
            throw new ResourceNotFoundException("Driver not found for user id: " + driverId);
        } catch (Exception e) {
            log.error("Error validating driver {}: {}", driverId, e.getMessage());
            throw new ResourceNotFoundException("Driver profile not found for user id: " + driverId);
        }
    } */


    private void validateStatusTransition(DeliveryStatus currentStatus, DeliveryStatus newStatus) {
        boolean isValid = switch (currentStatus) {
            case PENDING -> newStatus == DeliveryStatus.ASSIGNED ||
                    newStatus == DeliveryStatus.CANCELLED;
            case ASSIGNED -> newStatus == DeliveryStatus.PICKED_UP ||
                    newStatus == DeliveryStatus.CANCELLED;
            case PICKED_UP -> newStatus == DeliveryStatus.DELIVERED ||  // ← CHANGED: Direct to DELIVERED
                    newStatus == DeliveryStatus.CANCELLED;
            case DELIVERED, CANCELLED -> false; // Terminal states
        };

        if (!isValid) {
            throw new BadRequestException(
                    "Invalid status transition from " + currentStatus + " to " + newStatus
            );
        }
    }

    public List<DeliveryResponseDTO> getActiveDeliveriesByDriverId(Long driverId) {
        List<DeliveryStatus> activeStatuses = List.of(
                DeliveryStatus.ASSIGNED,
                DeliveryStatus.PICKED_UP
        );
        List<Delivery> deliveries = deliveryRepository.findByDriverIdAndStatusIn(driverId, activeStatuses);
        return deliveries.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }


    /*private String getDriverName(Map<String, Object> driverData) {
        // Try to get full name first
        Object fullName = driverData.get("fullName");
        if (fullName != null && !fullName.toString().isEmpty()) {
            return fullName.toString();
        }

        // Fallback to email if name not available
        Object email = driverData.get("email");
        if (email != null && !email.toString().isEmpty()) {
            return email.toString().split("@")[0]; // Use part before @
        }

        // Final fallback to driver ID
        Object userId = driverData.get("userId");
        if (userId != null) {
            return "Driver #" + userId;
        }

        return "Driver (Unknown)";
    } */


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
        dto.setPickupTime(delivery.getPickupTime());
        dto.setDeliveryTime(delivery.getDeliveryTime());
        dto.setEstimatedDeliveryTime(delivery.getEstimatedDeliveryTime());
        dto.setDeliveryNotes(delivery.getDeliveryNotes());
        dto.setCreatedAt(delivery.getCreatedAt());
        dto.setUpdatedAt(delivery.getUpdatedAt());
        return dto;
    }


}