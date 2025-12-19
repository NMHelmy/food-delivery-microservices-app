package com.fooddelivery.restaurantservice.service;

import com.fooddelivery.restaurantservice.dto.RestaurantRequest;
import com.fooddelivery.restaurantservice.dto.RestaurantResponse;
import com.fooddelivery.restaurantservice.models.Restaurant;
import com.fooddelivery.restaurantservice.exception.ResourceNotFoundException;
import com.fooddelivery.restaurantservice.exception.UnauthorizedException;
import com.fooddelivery.restaurantservice.repository.RestaurantRepository;
import com.fooddelivery.restaurantservice.feign.UserServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final UserServiceClient userServiceClient;

    @Autowired
    public RestaurantService(RestaurantRepository restaurantRepository, UserServiceClient userServiceClient) {
        this.restaurantRepository = restaurantRepository;
        this.userServiceClient = userServiceClient;
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> getAllRestaurants() {
        return restaurantRepository.findByIsActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RestaurantResponse getRestaurantById(Long id) {
        Restaurant restaurant = restaurantRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + id));
        return mapToResponse(restaurant);
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> getRestaurantsByCuisine(String cuisine) {
        return restaurantRepository.findByCuisineAndIsActiveTrue(cuisine)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<Restaurant> getRestaurantsByOwnerId(Long ownerId) {
        List<Restaurant> restaurants = restaurantRepository.findByOwnerId(ownerId);

        if (restaurants.isEmpty()) {
            throw new ResourceNotFoundException("No restaurants found for owner id: " + ownerId);
        }

        return restaurants;
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> searchRestaurantsByName(String name) {
        return restaurantRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(name)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> getRestaurantsByOwner(Long ownerId) {
        return restaurantRepository.findByOwnerIdAndIsActiveTrue(ownerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public RestaurantResponse createRestaurant(RestaurantRequest request, Long ownerId) {
        try {
            userServiceClient.getOwnerProfile(ownerId);
        } catch (Exception e) {
            throw new RuntimeException("Invalid owner. User must have a restaurant owner profile.");
        }
        Restaurant restaurant = new Restaurant();
        restaurant.setName(request.getName());
        restaurant.setAddress(request.getAddress());
        restaurant.setPhone(request.getPhone());
        restaurant.setCuisine(request.getCuisine());
        restaurant.setDescription(request.getDescription());
        restaurant.setImageUrl(request.getImageUrl());
        restaurant.setOwnerId(ownerId);
        restaurant.setRating(0.0);
        restaurant.setIsActive(true);

        Restaurant saved = restaurantRepository.save(restaurant);
        return mapToResponse(saved);
    }

    @Transactional
    public RestaurantResponse updateRestaurant(Long id, RestaurantRequest request, Long ownerId) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + id));

        if (!restaurant.getOwnerId().equals(ownerId)) {
            throw new UnauthorizedException("You are not authorized to update this restaurant");
        }

        restaurant.setName(request.getName());
        restaurant.setAddress(request.getAddress());
        restaurant.setPhone(request.getPhone());
        restaurant.setCuisine(request.getCuisine());
        restaurant.setDescription(request.getDescription());
        restaurant.setImageUrl(request.getImageUrl());

        Restaurant updated = restaurantRepository.save(restaurant);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteRestaurant(Long id, Long ownerId) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + id));

        if (!restaurant.getOwnerId().equals(ownerId)) {
            throw new UnauthorizedException("You are not authorized to delete this restaurant");
        }

        restaurant.setIsActive(false);
        restaurantRepository.save(restaurant);
    }

    @Transactional
    public void updateRestaurantRating(Long id, Double rating) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + id));

        restaurant.setRating(rating);
        restaurantRepository.save(restaurant);
    }

    private RestaurantResponse mapToResponse(Restaurant restaurant) {
        return new RestaurantResponse(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getAddress(),
                restaurant.getPhone(),
                restaurant.getCuisine(),
                restaurant.getRating(),
                restaurant.getIsActive(),
                restaurant.getOwnerId(),
                restaurant.getDescription(),
                restaurant.getImageUrl(),
                restaurant.getCreatedAt(),
                restaurant.getUpdatedAt()
        );
    }
}