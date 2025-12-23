package com.fooddelivery.orderservice.service;

import com.fooddelivery.orderservice.dto.*;
import com.fooddelivery.orderservice.event.*;
import com.fooddelivery.orderservice.exception.BadRequestException;
import com.fooddelivery.orderservice.exception.ForbiddenOperationException;
import com.fooddelivery.orderservice.exception.ResourceNotFoundException;
import com.fooddelivery.orderservice.exception.UnauthorizedException;
import com.fooddelivery.orderservice.feign.RestaurantServiceClient;
import com.fooddelivery.orderservice.feign.UserServiceClient;
import com.fooddelivery.orderservice.feign.DeliveryServiceClient;
import com.fooddelivery.orderservice.model.Order;
import com.fooddelivery.orderservice.model.OrderItem;
import com.fooddelivery.orderservice.model.OrderStatus;
import com.fooddelivery.orderservice.model.PaymentStatus;
import com.fooddelivery.orderservice.repository.OrderRepository;
import com.fooddelivery.cartservice.dto.*;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private RestaurantServiceClient restaurantServiceClient;

    @Autowired
    private DeliveryServiceClient deliveryServiceClient;

    @Autowired
    private OrderEventPublisher eventPublisher; //added for rabbitmq integration

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
        order.setSpecialInstructions(dto.getSpecialInstructions());
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.PENDING);

        for (OrderItemDTO itemDTO : dto.getItems()) {
            // Fetch menu item details to get the ACTUAL price
            Map<String, Object> menuItem = fetchMenuItem(dto.getRestaurantId(), itemDTO.getMenuItemId());

            // Verify menu item is available
            Boolean isAvailable = (Boolean) menuItem.get("isAvailable");
            if (isAvailable == null || !isAvailable) {
                throw new BadRequestException(
                        "Menu item " + menuItem.get("name") + " is currently unavailable"
                );
            }

            OrderItem item = new OrderItem();
            item.setMenuItemId(itemDTO.getMenuItemId());

            item.setItemName((String) menuItem.get("name"));

            item.setQuantity(itemDTO.getQuantity());

            Object priceObj = menuItem.get("price");
            BigDecimal actualPrice;
            if (priceObj instanceof Double) {
                actualPrice = BigDecimal.valueOf((Double) priceObj);
            } else if (priceObj instanceof Integer) {
                actualPrice = BigDecimal.valueOf((Integer) priceObj);
            } else if (priceObj instanceof BigDecimal) {
                actualPrice = (BigDecimal) priceObj;
            } else {
                throw new BadRequestException("Invalid price format for menu item " + itemDTO.getMenuItemId());
            }
            item.setPrice(actualPrice);

            item.setCustomizations(itemDTO.getCustomizations());
            order.addItem(item);
        }

        order.setSubtotal(calculateSubtotal(order));
        order.setTax(calculateTax(order));
        order.setDeliveryFee(calculateDeliveryFee(dto.getRestaurantId(), dto.getDeliveryAddressId()));
        order.setTotal(order.getSubtotal().add(order.getDeliveryFee()).add(order.getTax()));
        order.setEstimatedDeliveryTime(LocalDateTime.now().plusMinutes(45));
        Order savedOrder = orderRepository.save(order);

        // Added - Publish ORDER_CREATED event
        try {
            String restaurantName = getRestaurantName(dto.getRestaurantId());
            Long restaurantOwnerId = getRestaurantOwnerId(dto.getRestaurantId());
            eventPublisher.publishOrderCreated(new OrderCreatedEvent(
                    savedOrder.getId(),
                    customerId,
                    dto.getRestaurantId(),
                    restaurantOwnerId,
                    restaurantName
            ));
        } catch (Exception e) {
            // Log but don't fail the order creation if event publishing fails
            System.err.println("Failed to publish ORDER_CREATED event: " + e.getMessage());
        }

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

    public List<OrderResponseDTO> getOrdersByStatus(OrderStatus status) {
        List<Order> orders = orderRepository.findByStatus(status);
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

        // ADDED - Automatically create delivery when order is ready for pickup
        if (dto.getStatus() == OrderStatus.READY_FOR_PICKUP) {
            try {
                autoCreateDelivery(updatedOrder);
            } catch (Exception e) {
                System.err.println("Failed to auto-create delivery: " + e.getMessage());
                // Don't fail the status update if delivery creation fails
            }
        }

        try {
            String restaurantName = getRestaurantName(order.getRestaurantId());

            switch (dto.getStatus()) {
                case CONFIRMED:
                    eventPublisher.publishOrderConfirmed(new OrderConfirmedEvent(
                            orderId,
                            order.getCustomerId(),
                            restaurantName
                    ));
                    break;

                case READY_FOR_PICKUP:
                    eventPublisher.publishOrderReady(new OrderReadyEvent(
                            orderId,
                            order.getCustomerId(),
                            restaurantName
                    ));
                    break;

                default:
                    // No notification for other statuses
                    break;
            }
        } catch (Exception e) {
            // Log but don't fail the status update if event publishing fails
            System.err.println("Failed to publish order status event: " + e.getMessage());
        }

        return convertToResponseDTO(updatedOrder);
    }

    private void autoCreateDelivery(Order order) {
        try {
            // Fetch restaurant address for delivery tracking
            String restaurantAddress = fetchRestaurantAddress(order.getRestaurantId());

            // Fetch customer's delivery address
            String deliveryAddress = fetchDeliveryAddress(order.getCustomerId(), order.getDeliveryAddressId());

            // Build the delivery creation request
            Map<String, Object> deliveryRequest = new HashMap<>();
            deliveryRequest.put("orderId", order.getId());
            deliveryRequest.put("customerId", order.getCustomerId());
            deliveryRequest.put("restaurantId", order.getRestaurantId());
            deliveryRequest.put("deliveryAddressId", order.getDeliveryAddressId());
            deliveryRequest.put("restaurantAddress", restaurantAddress);
            deliveryRequest.put("deliveryAddress", deliveryAddress);
            deliveryRequest.put("estimatedDeliveryTime", order.getEstimatedDeliveryTime().toString());

            // Add special instructions as delivery notes if present
            if (order.getSpecialInstructions() != null && !order.getSpecialInstructions().isEmpty()) {
                deliveryRequest.put("deliveryNotes", order.getSpecialInstructions());
            }

            Map<String, Object> response = deliveryServiceClient.createDelivery(
                    deliveryRequest,
                    "true"
            );

            System.out.println("Auto-created delivery for order: " + order.getId() +
                    " | Delivery ID: " + response.get("id"));
        } catch (Exception e) {
            System.err.println("Failed to auto-create delivery for order " + order.getId() + ": " + e.getMessage());
        }
    }

    private String fetchRestaurantAddress(Long restaurantId) {
        try {
            Map<String, Object> restaurant = restaurantServiceClient.getRestaurant(restaurantId);

            StringBuilder address = new StringBuilder();

            if (restaurant.get("address") != null) {
                address.append(restaurant.get("address"));
            }

            if (restaurant.get("district") != null) {
                if (address.length() > 0) address.append(", ");
                address.append(restaurant.get("district"));
            }

            if (restaurant.get("city") != null) {
                if (address.length() > 0) address.append(", ");
                address.append(restaurant.get("city"));
            }

            return address.length() > 0 ? address.toString() : "Restaurant Address Not Available";

        } catch (Exception e) {
            return "Restaurant Address Not Available";
        }
    }

    private String fetchDeliveryAddress(Long customerId, Long addressId) {
        try {
            // Call User Service to get the specific address
            Object addressResponse = userServiceClient.getAddressById(
                    customerId,
                    addressId
            );

            // Parse the address response
            @SuppressWarnings("unchecked")
            Map<String, Object> addressMap = (Map<String, Object>) addressResponse;

            StringBuilder address = new StringBuilder();

            if (addressMap.get("street") != null) {
                address.append(addressMap.get("street"));
            }

            if (addressMap.get("district") != null) {
                if (address.length() > 0) address.append(", ");
                address.append(addressMap.get("district"));
            }

            if (addressMap.get("city") != null) {
                if (address.length() > 0) address.append(", ");
                address.append(addressMap.get("city"));
            }

            if (addressMap.get("building") != null) {
                if (address.length() > 0) address.append(", ");
                address.append("Building: ").append(addressMap.get("building"));
            }

            if (addressMap.get("floor") != null) {
                address.append(", Floor: ").append(addressMap.get("floor"));
            }

            if (addressMap.get("apartment") != null) {
                address.append(", Apt: ").append(addressMap.get("apartment"));
            }

            return address.length() > 0 ? address.toString() : "Delivery Address Not Available";

        } catch (Exception e) {
            return "Delivery Address Not Available";
        }
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
            throw new ForbiddenOperationException(
                    "You can only cancel your own order"
            );
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
        // ADDED - Publish ORDER_CANCELLED event
        try {
            Long restaurantOwnerId = getRestaurantOwnerId(order.getRestaurantId());
            eventPublisher.publishOrderCancelled(new OrderCancelledEvent(
                    orderId,
                    userId,
                    restaurantOwnerId,
                    "Cancelled by customer"
            ));
        } catch (Exception e) {
            // Log but don't fail the cancellation if event publishing fails
            System.err.println("Failed to publish ORDER_CANCELLED event: " + e.getMessage());
        }

        return convertToResponseDTO(updatedOrder);
    }

    @Transactional
    public void markOrderAsPaid(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        order.setPaymentStatus(PaymentStatus.PAID);
        orderRepository.save(order);
    }

    private void validateCustomerExists(Long customerId) {
        try {
            // Call User Service to verify customer profile exists
            userServiceClient.getUserById(customerId);
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

    private void validateDeliveryAddress(Long customerId, Long addressId) {
        try {
            // Get customer's addresses and verify the address exists
            userServiceClient.getAddressById(customerId, addressId);
            // In a real implementation, you'd parse the response and check if addressId exists
            // For now, we'll trust that if the call succeeds, the address is valid
        } catch (Exception e) {
            throw new BadRequestException("Invalid delivery address for customer " + customerId);
        }
    }

    public boolean verifyRestaurantOwnership(Long restaurantId, Long userId) {
        try {
            // Call Restaurant Service to verify ownership
            Map<String, Object> restaurant = restaurantServiceClient.getRestaurant(restaurantId);

            // Extract ownerId from the restaurant response
            Object ownerIdObj = restaurant.get("ownerId");
            Long ownerId;

            if (ownerIdObj instanceof Integer) {
                ownerId = ((Integer) ownerIdObj).longValue();
            } else if (ownerIdObj instanceof Long) {
                ownerId = (Long) ownerIdObj;
            } else {
                return false;
            }

            return ownerId.equals(userId);
        } catch (Exception e) {
            // Log the error
            // Fail closed - deny access on error
            return false;
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

    private Map<String, Object> fetchMenuItem(Long restaurantId, Long menuItemId) {
        try {
            return restaurantServiceClient.getMenuItem(restaurantId, menuItemId);
        } catch (Exception e) {
            throw new BadRequestException(
                    "Menu item with id " + menuItemId +
                            " not found in restaurant " + restaurantId
            );
        }
    }
    // ADDED - Helper method to get restaurant name
    private String getRestaurantName(Long restaurantId) {
        try {
            Map<String, Object> restaurant = restaurantServiceClient.getRestaurant(restaurantId);
            Object nameObj = restaurant.get("name");
            return nameObj != null ? nameObj.toString() : "Restaurant";
        } catch (Exception e) {
            return "Restaurant"; // Fallback if we can't get the name
        }
    }
    //ADDED - Helper method to get restaurant owner id
    private Long getRestaurantOwnerId(Long restaurantId) {
        try {
            Map<String, Object> restaurant = restaurantServiceClient.getRestaurant(restaurantId);
            Object ownerIdObj = restaurant.get("ownerId");

            if (ownerIdObj instanceof Integer) {
                return ((Integer) ownerIdObj).longValue();
            } else if (ownerIdObj instanceof Long) {
                return (Long) ownerIdObj;
            }
            throw new RuntimeException("Could not get owner ID for restaurant " + restaurantId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get restaurant owner ID: " + e.getMessage());
        }
    }

    @Transactional
    public OrderResponseDTO createOrderFromCart(CreateOrderFromCartDTO cartDto) {
        // Convert cart DTO to regular CreateOrderDTO
        CreateOrderDTO dto = new CreateOrderDTO();
        dto.setRestaurantId(cartDto.getRestaurantId());
        dto.setDeliveryAddressId(cartDto.getDeliveryAddressId());
        dto.setSpecialInstructions(cartDto.getSpecialInstructions());

        List<OrderItemDTO> orderItems = cartDto.getItems().stream()
                .map(cartItem -> {
                    OrderItemDTO orderItem = new OrderItemDTO();
                    orderItem.setMenuItemId(cartItem.getMenuItemId());
                    orderItem.setQuantity(cartItem.getQuantity());
                    orderItem.setCustomizations(cartItem.getCustomizations());
                    return orderItem;
                })
                .collect(Collectors.toList());

        dto.setItems(orderItems);

        // Reuse existing createOrder logic
        return createOrder(dto, cartDto.getCustomerId());
    }



    private BigDecimal calculateDeliveryFee(Long restaurantId, Long deliveryAddressId) {
        // Base fee for Egyptian market: 15 EGP
        BigDecimal baseFee = new BigDecimal("15.00");

        // TODO: Future enhancements:
        // - Calculate actual distance
        // - Add peak hour surcharges
        // - Apply demand-based pricing
        // - Check promotional discounts

        return baseFee;
    }

    private BigDecimal calculateTax(Order order) {
        // Egypt VAT: 14%
        BigDecimal taxRate = new BigDecimal("0.14");
        BigDecimal subtotal = order.getSubtotal();
        BigDecimal tax = subtotal.multiply(taxRate);

        return tax.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private BigDecimal calculateSubtotal(Order order) {
        return order.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
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
            case PICKED_UP -> newStatus == OrderStatus.DELIVERED ||
                    newStatus == OrderStatus.CANCELLED;
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