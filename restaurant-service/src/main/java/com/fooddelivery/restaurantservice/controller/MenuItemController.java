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

    // Owner or Admin creates menu item
    @PostMapping
    public ResponseEntity<?> createMenuItem(
            @PathVariable Long restaurantId,
            @Valid @RequestBody MenuItemRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String userRole
    ) {

        MenuItemResponse item = menuItemService.createMenuItem(restaurantId, request, userId, userRole);
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    // Owner or Admin updates menu item
    @PutMapping("/{itemId}")
    public ResponseEntity<?> updateMenuItem(
            @PathVariable Long restaurantId,
            @PathVariable Long itemId,
            @Valid @RequestBody MenuItemRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String userRole
    ) {

        MenuItemResponse item = menuItemService.updateMenuItem(restaurantId, itemId, request, userId, userRole);
        return ResponseEntity.ok(item);
    }

    // Owner or Admin deletes menu item
    @DeleteMapping("/{itemId}")
    public ResponseEntity<?> deleteMenuItem(
            @PathVariable Long restaurantId,
            @PathVariable Long itemId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String userRole
    ) {

        menuItemService.deleteMenuItem(restaurantId, itemId, userId, userRole);
        return ResponseEntity.ok(Map.of("message", "Menu item deleted successfully"));
    }

    // Owner or Admin toggles availability
    @PatchMapping("/{itemId}/availability")
    public ResponseEntity<?> toggleAvailability(
            @PathVariable Long restaurantId,
            @PathVariable Long itemId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String userRole
    ) {

        MenuItemResponse item = menuItemService.toggleAvailability(restaurantId, itemId, userId, userRole);
        return ResponseEntity.ok(item);
    }
}