package com.fooddelivery.restaurantservice.controller;

import com.fooddelivery.restaurantservice.dto.MenuItemRequest;
import com.fooddelivery.restaurantservice.dto.MenuItemResponse;
import com.fooddelivery.restaurantservice.service.MenuItemService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/restaurants/{restaurantId}/menu")
public class MenuItemController {

    private final MenuItemService menuItemService;

    public MenuItemController(MenuItemService menuItemService) {
        this.menuItemService = menuItemService;
    }

    @GetMapping
    public ResponseEntity<List<MenuItemResponse>> getMenuItems(
            @PathVariable Long restaurantId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "false") Boolean availableOnly) {

        List<MenuItemResponse> items;

        if (category != null && !category.isEmpty()) {
            items = menuItemService.getMenuItemsByCategory(restaurantId, category, availableOnly);
        } else {
            items = menuItemService.getMenuItemsByRestaurant(restaurantId, availableOnly);
        }

        return ResponseEntity.ok(items);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<MenuItemResponse> getMenuItemById(
            @PathVariable Long restaurantId,
            @PathVariable Long itemId) {
        MenuItemResponse item = menuItemService.getMenuItemById(restaurantId, itemId);
        return ResponseEntity.ok(item);
    }

    // Owner creates menu item (gateway ensures RESTAURANT_OWNER role)
    @PostMapping
    public ResponseEntity<?> createMenuItem(
            @PathVariable Long restaurantId,
            @Valid @RequestBody MenuItemRequest request,
            @RequestHeader("X-User-Id") String ownerId) {

        MenuItemResponse item = menuItemService.createMenuItem(restaurantId, request, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    // Owner updates menu item (gateway ensures RESTAURANT_OWNER role)
    @PutMapping("/{itemId}")
    public ResponseEntity<?> updateMenuItem(
            @PathVariable Long restaurantId,
            @PathVariable Long itemId,
            @Valid @RequestBody MenuItemRequest request,
            @RequestHeader("X-User-Id") String ownerId) {

        MenuItemResponse item = menuItemService.updateMenuItem(restaurantId, itemId, request, ownerId);
        return ResponseEntity.ok(item);
    }

    // Owner deletes menu item (gateway ensures RESTAURANT_OWNER role)
    @DeleteMapping("/{itemId}")
    public ResponseEntity<?> deleteMenuItem(
            @PathVariable Long restaurantId,
            @PathVariable Long itemId,
            @RequestHeader("X-User-Id") String ownerId) {

        menuItemService.deleteMenuItem(restaurantId, itemId, ownerId);
        return ResponseEntity.ok(Map.of("message", "Menu item deleted successfully"));
    }

    // Owner toggles availability
    @PatchMapping("/{itemId}/availability")
    public ResponseEntity<?> toggleAvailability(
            @PathVariable Long restaurantId,
            @PathVariable Long itemId,
            @RequestHeader("X-User-Id") String ownerId) {

        MenuItemResponse item = menuItemService.toggleAvailability(restaurantId, itemId, ownerId);
        return ResponseEntity.ok(item);
    }
}