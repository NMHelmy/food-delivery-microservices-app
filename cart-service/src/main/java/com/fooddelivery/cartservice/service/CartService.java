package com.fooddelivery.cartservice.service;

import com.fooddelivery.cartservice.dto.*;
import com.fooddelivery.cartservice.exception.BadRequestException;
import com.fooddelivery.cartservice.exception.ForbiddenOperationException;
import com.fooddelivery.cartservice.exception.ResourceNotFoundException;
import com.fooddelivery.cartservice.feign.AuthServiceClient;
import com.fooddelivery.cartservice.feign.OrderServiceClient;
import com.fooddelivery.cartservice.feign.RestaurantServiceClient;
import com.fooddelivery.cartservice.model.Cart;
import com.fooddelivery.cartservice.model.CartItem;
import com.fooddelivery.cartservice.repository.CartRepository;
import com.fooddelivery.cartservice.repository.CartItemRepository;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private RestaurantServiceClient restaurantServiceClient;

    @Autowired
    private OrderServiceClient orderServiceClient;

    @Autowired
    private AuthServiceClient authServiceClient;

    private static final int CART_TTL_HOURS = 24;

    @Transactional(readOnly = true)
    public CartResponseDTO getCartByCustomerId(Long customerId) {
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart not found for customer: " + customerId));

        // Check if cart has expired
        if (cart.getExpiresAt() != null &&
                LocalDateTime.now().isAfter(cart.getExpiresAt())) {
            // Clear expired cart
            clearCart(customerId);
            throw new ResourceNotFoundException("Cart has expired and been cleared");
        }

        return convertToResponseDTO(cart);
    }

    @Transactional
    public CartResponseDTO addItemToCart(AddCartItemDTO dto, Long customerId) {
        // Validate menu item exists and is available
        Map<String, Object> menuItem = fetchMenuItem(dto.getRestaurantId(), dto.getMenuItemId());
        validateMenuItemAvailability(menuItem);

        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElse(createNewCart(customerId, dto.getRestaurantId()));

        // Enforce single-restaurant rule
        if (!cart.getRestaurantId().equals(dto.getRestaurantId())) {
            throw new BadRequestException(
                    "Cannot add items from different restaurants. " +
                            "Please clear your cart first or complete your current order."
            );
        }

        // Check if item already exists in cart
        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getMenuItemId().equals(dto.getMenuItemId()) &&
                        isSameCustomization(item.getCustomizations(), dto.getCustomizations()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            // Update quantity if item exists
            existingItem.setQuantity(existingItem.getQuantity() + dto.getQuantity());
        } else {
            // Add new item
            CartItem newItem = new CartItem();
            newItem.setMenuItemId(dto.getMenuItemId());
            newItem.setItemName((String) menuItem.get("name"));
            newItem.setQuantity(dto.getQuantity());
            newItem.setPrice(extractPrice(menuItem.get("price")));
            newItem.setCustomizations(dto.getCustomizations());
            cart.addItem(newItem);
        }

        // Update expiry time
        cart.setExpiresAt(LocalDateTime.now().plusHours(CART_TTL_HOURS));

        Cart savedCart = cartRepository.save(cart);
        return convertToResponseDTO(savedCart);
    }

    @Transactional
    public CartResponseDTO updateCartItem(Long itemId, UpdateCartItemDTO dto, Long customerId) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart item not found: " + itemId));

        // Verify ownership
        if (!item.getCart().getCustomerId().equals(customerId)) {
            throw new ForbiddenOperationException("You can only update items in your own cart");
        }

        item.setQuantity(dto.getQuantity());
        if (dto.getCustomizations() != null) {
            item.setCustomizations(dto.getCustomizations());
        }

        cartItemRepository.save(item);

        Cart cart = item.getCart();
        cart.setExpiresAt(LocalDateTime.now().plusHours(CART_TTL_HOURS));
        cartRepository.save(cart);

        return convertToResponseDTO(cart);
    }

    @Transactional
    public CartResponseDTO removeItemFromCart(Long itemId, Long customerId) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart item not found: " + itemId));

        // Verify ownership
        if (!item.getCart().getCustomerId().equals(customerId)) {
            throw new ForbiddenOperationException("You can only remove items from your own cart");
        }

        Cart cart = item.getCart();

        // Update the relationship first
        cart.removeItem(item);

        // Delete the item row
        cartItemRepository.delete(item);

        // If cart is empty, delete cart and return null (NO EXCEPTION!)
        if (cart.getItems().isEmpty()) {
            cartRepository.delete(cart);
            return null;
        }

        cart.setExpiresAt(LocalDateTime.now().plusHours(CART_TTL_HOURS));
        Cart saved = cartRepository.save(cart);

        return convertToResponseDTO(saved);
    }

    @Transactional
    public void clearCart(Long customerId) {
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart not found for customer: " + customerId));

        cartRepository.delete(cart);
    }

    @Transactional
    public OrderResponseDTO checkout(CheckoutRequestDTO dto, Long customerId) {
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart is empty. Cannot checkout."));

        // Check if cart has expired
        if (cart.getExpiresAt() != null &&
                LocalDateTime.now().isAfter(cart.getExpiresAt())) {
            cartRepository.delete(cart);
            throw new BadRequestException("Cart has expired. Please add items again.");
        }

        // Address Validation
        validateAddressOwnership(dto.getDeliveryAddressId(), customerId);

        // Verify all items are still available
        for (CartItem item : cart.getItems()) {
            Map<String, Object> menuItem = fetchMenuItem(
                    cart.getRestaurantId(),
                    item.getMenuItemId());
            validateMenuItemAvailability(menuItem);
        }

        // Build order creation DTO
        CreateOrderFromCartDTO orderDTO = new CreateOrderFromCartDTO();
        orderDTO.setCustomerId(customerId);
        orderDTO.setRestaurantId(cart.getRestaurantId());
        orderDTO.setDeliveryAddressId(dto.getDeliveryAddressId());
        orderDTO.setSpecialInstructions(dto.getSpecialInstructions());
        orderDTO.setItems(cart.getItems().stream()
                .map(item -> new CartItemForOrderDTO(
                        item.getMenuItemId(),
                        item.getQuantity(),
                        item.getCustomizations()))
                .collect(Collectors.toList()));

        // Call order-service internal endpoint
        OrderResponseDTO order = orderServiceClient.createOrderFromCart(orderDTO, "true");

        // Clear cart after successful order creation
        cartRepository.delete(cart);

        return order;
    }

    // User address Validation
    private void validateAddressOwnership(Long addressId, Long userId) {
        try {
            Boolean isOwner = authServiceClient.verifyAddressOwnership(
                    addressId,
                    userId,
                    "true"
            );

            if (isOwner == null || !isOwner) {
                throw new ForbiddenOperationException(
                        "The delivery address does not belong to you");
            }
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException(
                    "Delivery address not found: " + addressId);
        } catch (FeignException e) {
            throw new BadRequestException(
                    "Unable to verify address ownership. Please try again.");
        }
    }

    private Cart createNewCart(Long customerId, Long restaurantId) {
        Cart cart = new Cart();
        cart.setCustomerId(customerId);
        cart.setRestaurantId(restaurantId);
        cart.setExpiresAt(LocalDateTime.now().plusHours(CART_TTL_HOURS));
        return cart;
    }

    private Map<String, Object> fetchMenuItem(Long restaurantId, Long menuItemId) {
        try {
            return restaurantServiceClient.getMenuItem(restaurantId, menuItemId);
        } catch (Exception e) {
            throw new BadRequestException(
                    "Menu item " + menuItemId + " not found in restaurant " + restaurantId);
        }
    }

    private void validateMenuItemAvailability(Map<String, Object> menuItem) {
        Boolean isAvailable = (Boolean) menuItem.get("isAvailable");
        if (isAvailable == null || !isAvailable) {
            throw new BadRequestException(
                    "Menu item " + menuItem.get("name") + " is currently unavailable");
        }
    }

    private BigDecimal extractPrice(Object priceObj) {
        if (priceObj instanceof Double) {
            return BigDecimal.valueOf((Double) priceObj);
        } else if (priceObj instanceof Integer) {
            return BigDecimal.valueOf((Integer) priceObj);
        } else if (priceObj instanceof BigDecimal) {
            return (BigDecimal) priceObj;
        }
        throw new BadRequestException("Invalid price format");
    }

    private boolean isSameCustomization(String custom1, String custom2) {
        if (custom1 == null && custom2 == null) return true;
        if (custom1 == null || custom2 == null) return false;
        return custom1.equals(custom2);
    }

    private CartResponseDTO convertToResponseDTO(Cart cart) {
        CartResponseDTO dto = new CartResponseDTO();
        dto.setId(cart.getId());
        dto.setCustomerId(cart.getCustomerId());
        dto.setRestaurantId(cart.getRestaurantId());

        // Fetch restaurant name
        try {
            Map<String, Object> restaurant = restaurantServiceClient.getRestaurant(cart.getRestaurantId());
            dto.setRestaurantName((String) restaurant.get("name"));
        } catch (Exception e) {
            dto.setRestaurantName("Unknown Restaurant");
        }

        dto.setItems(cart.getItems().stream()
                .map(this::convertToItemResponseDTO)
                .collect(Collectors.toList()));

        dto.setSubtotal(calculateSubtotal(cart));
        dto.setTotalItems(cart.getItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum());
        dto.setCreatedAt(cart.getCreatedAt());
        dto.setUpdatedAt(cart.getUpdatedAt());
        dto.setExpiresAt(cart.getExpiresAt());

        return dto;
    }

    private CartItemResponseDTO convertToItemResponseDTO(CartItem item) {
        CartItemResponseDTO dto = new CartItemResponseDTO();
        dto.setId(item.getId());
        dto.setMenuItemId(item.getMenuItemId());
        dto.setItemName(item.getItemName());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        dto.setItemTotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        dto.setCustomizations(item.getCustomizations());
        return dto;
    }

    private BigDecimal calculateSubtotal(Cart cart) {
        return cart.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
