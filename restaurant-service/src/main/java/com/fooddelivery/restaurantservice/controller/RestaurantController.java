package com.fooddelivery.restaurantservice.controller;

import com.fooddelivery.restaurantservice.dto.RestaurantRequest;
import com.fooddelivery.restaurantservice.dto.RestaurantResponse;
import com.fooddelivery.restaurantservice.exception.ResourceNotFoundException;
import com.fooddelivery.restaurantservice.models.Restaurant;
import com.fooddelivery.restaurantservice.service.RestaurantService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @GetMapping
    public ResponseEntity<List<RestaurantResponse>> getAllRestaurants(
            @RequestParam(required = false) String cuisine,
            @RequestParam(required = false) String search) {

        List<RestaurantResponse> restaurants;

        if (cuisine != null && !cuisine.isEmpty()) {
            restaurants = restaurantService.getRestaurantsByCuisine(cuisine);
        } else if (search != null && !search.isEmpty()) {
            restaurants = restaurantService.searchRestaurantsByName(search);
        } else {
            restaurants = restaurantService.getAllRestaurants();
        }

        return ResponseEntity.ok(restaurants);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponse> getRestaurantById(@PathVariable Long id) {
        RestaurantResponse restaurant = restaurantService.getRestaurantById(id);
        return ResponseEntity.ok(restaurant);
    }

    @GetMapping("/owner")
    public ResponseEntity<List<RestaurantResponse>> getMyRestaurants(
            @RequestHeader("X-User-Id") Long ownerId) {

        List<RestaurantResponse> restaurants = restaurantService.getRestaurantsByOwner(ownerId);
        return ResponseEntity.ok(restaurants);
    }

    // Admin: all restaurants for specific owner
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<?> getRestaurantsByOwnerId(@PathVariable Long ownerId) {
        try {
            List<Restaurant> restaurants = restaurantService.getRestaurantsByOwnerId(ownerId);
            return ResponseEntity.ok(restaurants);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Owner creates restaurant
    @PostMapping
    public ResponseEntity<?> createRestaurant(
            @Valid @RequestBody RestaurantRequest request,
            @RequestHeader("X-User-Id") Long ownerId) {

        RestaurantResponse restaurant = restaurantService.createRestaurant(request, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(restaurant);
    }

    // Owner updates own restaurant
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRestaurant(
            @PathVariable Long id,
            @Valid @RequestBody RestaurantRequest request,
            @RequestHeader("X-User-Id") Long ownerId) {

        RestaurantResponse restaurant = restaurantService.updateRestaurant(id, request, ownerId);
        return ResponseEntity.ok(restaurant);
    }

    // Owner deletes (deactivates) own restaurant
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRestaurant(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long ownerId) {

        restaurantService.deleteRestaurant(id, ownerId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Restaurant deactivated successfully");
        return ResponseEntity.ok(response);
    }
}