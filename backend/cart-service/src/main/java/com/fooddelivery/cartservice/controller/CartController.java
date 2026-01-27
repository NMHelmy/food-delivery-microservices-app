package com.fooddelivery.cartservice.controller;

import com.fooddelivery.cartservice.dto.*;

import com.fooddelivery.cartservice.service.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    // GET /cart — Get customer's cart
    @GetMapping
    public ResponseEntity<CartResponseDTO> getCart(
            @RequestHeader("X-User-Id") Long customerId) {
        CartResponseDTO cart = cartService.getCartByCustomerId(customerId);
        return ResponseEntity.ok(cart);
    }

    // POST /cart/items — Add item to cart
    @PostMapping("/items")
    public ResponseEntity<CartResponseDTO> addItemToCart(
            @Valid @RequestBody AddCartItemDTO dto,
            @RequestHeader("X-User-Id") Long customerId) {
        CartResponseDTO cart = cartService.addItemToCart(dto, customerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(cart);
    }

    // PUT /cart/items/{itemId} — Update cart item
    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponseDTO> updateCartItem(
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemDTO dto,
            @RequestHeader("X-User-Id") Long customerId) {
        CartResponseDTO cart = cartService.updateCartItem(itemId, dto, customerId);
        return ResponseEntity.ok(cart);
    }

    // DELETE /cart/items/{itemId} — Remove item from cart
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponseDTO> removeItemFromCart(
            @PathVariable Long itemId,
            @RequestHeader("X-User-Id") Long customerId) {
        CartResponseDTO cart = cartService.removeItemFromCart(itemId, customerId);

        // cart deleted because it became empty
        if (cart == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(cart);
    }

    // DELETE /cart — Clear entire cart
    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            @RequestHeader("X-User-Id") Long customerId) {
        cartService.clearCart(customerId);
        return ResponseEntity.noContent().build();
    }

    // POST /cart/checkout — Checkout and create order
    @PostMapping("/checkout")
    public ResponseEntity<OrderResponseDTO> checkout(
            @Valid @RequestBody CheckoutRequestDTO dto,
            @RequestHeader("X-User-Id") Long customerId) {
        OrderResponseDTO order = cartService.checkout(dto, customerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }
}
