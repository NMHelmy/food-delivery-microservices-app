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
@RequestMapping("/api/restaurants/{restaurantId}/menu")
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

    @PostMapping
    public ResponseEntity<MenuItemResponse> createMenuItem(
            @PathVariable Long restaurantId,
            @Valid @RequestBody MenuItemRequest request,
            HttpServletRequest httpRequest) {

        String ownerId = httpRequest.getHeader("X-User-Id");
        if (ownerId == null || ownerId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        MenuItemResponse item = menuItemService.createMenuItem(restaurantId, request, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<MenuItemResponse> updateMenuItem(
            @PathVariable Long restaurantId,
            @PathVariable Long itemId,
            @Valid @RequestBody MenuItemRequest request,
            HttpServletRequest httpRequest) {

        String ownerId = httpRequest.getHeader("X-User-Id");
        if (ownerId == null || ownerId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        MenuItemResponse item = menuItemService.updateMenuItem(restaurantId, itemId, request, ownerId);
        return ResponseEntity.ok(item);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Map<String, String>> deleteMenuItem(
            @PathVariable Long restaurantId,
            @PathVariable Long itemId,
            HttpServletRequest httpRequest) {

        String ownerId = httpRequest.getHeader("X-User-Id");
        if (ownerId == null || ownerId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        menuItemService.deleteMenuItem(restaurantId, itemId, ownerId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Menu item deleted successfully");
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{itemId}/availability")
    public ResponseEntity<MenuItemResponse> toggleAvailability(
            @PathVariable Long restaurantId,
            @PathVariable Long itemId,
            HttpServletRequest httpRequest) {

        String ownerId = httpRequest.getHeader("X-User-Id");
        if (ownerId == null || ownerId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        MenuItemResponse item = menuItemService.toggleAvailability(restaurantId, itemId, ownerId);
        return ResponseEntity.ok(item);
    }
}