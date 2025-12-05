package com.fooddelivery.restaurantservice.controller;

import com.fooddelivery.restaurantservice.dto.RestaurantRequest;
import com.fooddelivery.restaurantservice.dto.RestaurantResponse;
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
@RequestMapping("/api/restaurants")
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
    public ResponseEntity<List<RestaurantResponse>> getMyRestaurants(HttpServletRequest request) {
        String ownerId = request.getHeader("X-User-Id");
        if (ownerId == null || ownerId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<RestaurantResponse> restaurants = restaurantService.getRestaurantsByOwner(ownerId);
        return ResponseEntity.ok(restaurants);
    }

    @PostMapping
    public ResponseEntity<RestaurantResponse> createRestaurant(
            @Valid @RequestBody RestaurantRequest request,
            HttpServletRequest httpRequest) {

        String ownerId = httpRequest.getHeader("X-User-Id");
        if (ownerId == null || ownerId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        RestaurantResponse restaurant = restaurantService.createRestaurant(request, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(restaurant);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantResponse> updateRestaurant(
            @PathVariable Long id,
            @Valid @RequestBody RestaurantRequest request,
            HttpServletRequest httpRequest) {

        String ownerId = httpRequest.getHeader("X-User-Id");
        if (ownerId == null || ownerId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        RestaurantResponse restaurant = restaurantService.updateRestaurant(id, request, ownerId);
        return ResponseEntity.ok(restaurant);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteRestaurant(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {

        String ownerId = httpRequest.getHeader("X-User-Id");
        if (ownerId == null || ownerId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        restaurantService.deleteRestaurant(id, ownerId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Restaurant deactivated successfully");
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/rating")
    public ResponseEntity<Map<String, String>> updateRating(
            @PathVariable Long id,
            @RequestBody Map<String, Double> ratingRequest) {

        Double rating = ratingRequest.get("rating");
        if (rating == null || rating < 0 || rating > 5) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Rating must be between 0 and 5");
            return ResponseEntity.badRequest().body(error);
        }

        restaurantService.updateRestaurantRating(id, rating);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Rating updated successfully");
        return ResponseEntity.ok(response);
    }
}