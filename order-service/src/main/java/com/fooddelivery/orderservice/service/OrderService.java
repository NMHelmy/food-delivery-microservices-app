package com.fooddelivery.orderservice.service;

import com.fooddelivery.orderservice.dto.*;
import com.fooddelivery.orderservice.exception.BadRequestException;
import com.fooddelivery.orderservice.exception.ResourceNotFoundException;
import com.fooddelivery.orderservice.exception.UnauthorizedException;
import com.fooddelivery.orderservice.feign.RestaurantServiceClient;
import com.fooddelivery.orderservice.feign.UserServiceClient;
import com.fooddelivery.orderservice.model.Order;
import com.fooddelivery.orderservice.model.OrderItem;
import com.fooddelivery.orderservice.model.OrderStatus;
import com.fooddelivery.orderservice.model.PaymentStatus;
import com.fooddelivery.orderservice.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private RestaurantServiceClient restaurantServiceClient;

    @Transactional
    public OrderResponseDTO createOrder(CreateOrderDTO dto, Long customerId) {
        // Controller already validated user is CUSTOMER
        // Just verify customer profile exists
        validateCustomerExists(customerId);

        validateRestaurantExists(dto.getRestaurantId());
        validateDeliveryAddress(customerId, dto.getDeliveryAddressId());
        validateMenuItems(dto.getRestaurantId(), dto.getItems());

        Order order = new Order();
        order.setCustomerId(customerId);
        order.setRestaurantId(dto.getRestaurantId());
        order.setDeliveryAddressId(dto.getDeliveryAddressId());
        order.setDeliveryFee(dto.getDeliveryFee());
        order.setTax(dto.getTax());
        order.setSpecialInstructions(dto.getSpecialInstructions());
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.PENDING);

        for (OrderItemDTO itemDTO : dto.getItems()) {
            OrderItem item = new OrderItem();
            item.setMenuItemId(itemDTO.getMenuItemId());
            item.setItemName(itemDTO.getItemName());
            item.setQuantity(itemDTO.getQuantity());
            item.setPrice(itemDTO.getPrice());
            item.setCustomizations(itemDTO.getCustomizations());
            order.addItem(item);
        }

        order.calculateTotal();
        order.setEstimatedDeliveryTime(LocalDateTime.now().plusMinutes(45));
        Order savedOrder = orderRepository.save(order);

        return convertToResponseDTO(savedOrder);
    }

    public OrderResponseDTO getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return convertToResponseDTO(order);
    }

    public List<OrderResponseDTO> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<OrderResponseDTO> getOrdersByCustomerId(Long customerId) {
        List<Order> orders = orderRepository.findByCustomerId(customerId);
        return orders.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<OrderResponseDTO> getOrdersByRestaurantId(Long restaurantId) {
        List<Order> orders = orderRepository.findByRestaurantId(restaurantId);
        return orders.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<OrderResponseDTO> getOrdersByDriverId(Long driverId) {
        List<Order> orders = orderRepository.findByDriverId(driverId);
        return orders.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<OrderResponseDTO> getOrdersByStatus(OrderStatus status) {
        List<Order> orders = orderRepository.findByStatus(status);
        return orders.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<OrderResponseDTO> getActiveOrdersForDriver(Long driverId) {
        List<OrderStatus> activeStatuses = List.of(
                OrderStatus.READY_FOR_PICKUP,
                OrderStatus.PICKED_UP,
                OrderStatus.ON_THE_WAY
        );
        List<Order> orders = orderRepository.findByDriverIdAndStatusIn(driverId, activeStatuses);
        return orders.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponseDTO updateOrderStatus(Long orderId, UpdateOrderStatusDTO dto) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // Validate status transition
        validateStatusTransition(order.getStatus(), dto.getStatus());

        order.setStatus(dto.getStatus());

        // If delivered, set actual delivery time
        if (dto.getStatus() == OrderStatus.DELIVERED) {
            order.setActualDeliveryTime(LocalDateTime.now());
        }

        Order updatedOrder = orderRepository.save(order);
        return convertToResponseDTO(updatedOrder);
    }

    @Transactional
    public OrderResponseDTO assignDriver(Long orderId, AssignDriverDTO dto) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // Validate order is ready for pickup
        if (order.getStatus() != OrderStatus.READY_FOR_PICKUP) {
            throw new BadRequestException("Order must be in READY_FOR_PICKUP status to assign a driver");
        }

        // Controller already validated user role
        // Just verify driver profile exists
        validateDriverExists(dto.getDriverId());

        order.setDriverId(dto.getDriverId());
        order.setStatus(OrderStatus.PICKED_UP);

        Order updatedOrder = orderRepository.save(order);
        return convertToResponseDTO(updatedOrder);
    }

    @Transactional
    public OrderResponseDTO updatePaymentStatus(Long orderId, UpdatePaymentStatusDTO dto) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        order.setPaymentStatus(dto.getPaymentStatus());

        Order updatedOrder = orderRepository.save(order);
        return convertToResponseDTO(updatedOrder);
    }

    @Transactional
    public OrderResponseDTO cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // Controller already validated authorization
        // Only verify order can be cancelled
        if (!order.getCustomerId().equals(userId)) {
            throw new UnauthorizedException("Only the customer who placed the order can cancel it");
        }

        // Can only cancel if not already delivered or cancelled
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new BadRequestException("Cannot cancel a delivered order");
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Order is already cancelled");
        }

        order.setStatus(OrderStatus.CANCELLED);

        // If payment was made, set to refunded
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            order.setPaymentStatus(PaymentStatus.REFUNDED);
        }

        Order updatedOrder = orderRepository.save(order);
        return convertToResponseDTO(updatedOrder);
    }

    private void validateCustomerExists(Long customerId) {
        try {
            // Call User Service to verify customer profile exists
            userServiceClient.getCustomerProfile(customerId);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Customer profile not found for user id: " + customerId);
        }
    }

    private void validateRestaurantExists(Long restaurantId) {
        try {
            restaurantServiceClient.getRestaurant(restaurantId);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Restaurant not found with id: " + restaurantId);
        }
    }

    private void validateDriverExists(Long driverId) {
        try {
            // Call User Service to verify driver profile exists
            userServiceClient.getDriverProfile(driverId);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Driver profile not found for user id: " + driverId);
        }
    }

    private void validateDeliveryAddress(Long customerId, Long addressId) {
        try {
            // Get customer's addresses and verify the address exists
            Object addresses = userServiceClient.getCustomerAddresses(customerId);
            // In a real implementation, you'd parse the response and check if addressId exists
            // For now, we'll trust that if the call succeeds, the address is valid
        } catch (Exception e) {
            throw new BadRequestException("Invalid delivery address for customer " + customerId);
        }
    }

    private void validateMenuItems(Long restaurantId, List<OrderItemDTO> items) {
        for (OrderItemDTO item : items) {
            try {
                restaurantServiceClient.getMenuItem(restaurantId, item.getMenuItemId());
            } catch (Exception e) {
                throw new BadRequestException(
                        "Menu item with id " + item.getMenuItemId() +
                                " not found in restaurant " + restaurantId
                );
            }
        }
    }

    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        // Define valid transitions
        boolean isValid = switch (currentStatus) {
            case PENDING -> newStatus == OrderStatus.CONFIRMED ||
                    newStatus == OrderStatus.REJECTED ||
                    newStatus == OrderStatus.CANCELLED;
            case CONFIRMED -> newStatus == OrderStatus.PREPARING ||
                    newStatus == OrderStatus.CANCELLED;
            case PREPARING -> newStatus == OrderStatus.READY_FOR_PICKUP ||
                    newStatus == OrderStatus.CANCELLED;
            case READY_FOR_PICKUP -> newStatus == OrderStatus.PICKED_UP ||
                    newStatus == OrderStatus.CANCELLED;
            case PICKED_UP -> newStatus == OrderStatus.ON_THE_WAY;
            case ON_THE_WAY -> newStatus == OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED, REJECTED -> false; // Terminal states
        };

        if (!isValid) {
            throw new BadRequestException(
                    "Invalid status transition from " + currentStatus + " to " + newStatus
            );
        }
    }

    private OrderResponseDTO convertToResponseDTO(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(order.getId());
        dto.setCustomerId(order.getCustomerId());
        dto.setRestaurantId(order.getRestaurantId());
        dto.setDriverId(order.getDriverId());
        dto.setDeliveryAddressId(order.getDeliveryAddressId());
        dto.setSubtotal(order.getSubtotal());
        dto.setDeliveryFee(order.getDeliveryFee());
        dto.setTax(order.getTax());
        dto.setTotal(order.getTotal());
        dto.setStatus(order.getStatus());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setSpecialInstructions(order.getSpecialInstructions());
        dto.setEstimatedDeliveryTime(order.getEstimatedDeliveryTime());
        dto.setActualDeliveryTime(order.getActualDeliveryTime());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());

        // Convert order items
        List<OrderItemResponseDTO> itemDTOs = order.getItems().stream()
                .map(this::convertToItemResponseDTO)
                .collect(Collectors.toList());
        dto.setItems(itemDTOs);

        return dto;
    }

    private OrderItemResponseDTO convertToItemResponseDTO(OrderItem item) {
        OrderItemResponseDTO dto = new OrderItemResponseDTO();
        dto.setId(item.getId());
        dto.setMenuItemId(item.getMenuItemId());
        dto.setItemName(item.getItemName());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        dto.setCustomizations(item.getCustomizations());
        return dto;
    }
}