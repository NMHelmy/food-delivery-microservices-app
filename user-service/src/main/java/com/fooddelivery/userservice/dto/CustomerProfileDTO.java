package com.fooddelivery.userservice.dto;

import jakarta.validation.constraints.NotNull;

public class CustomerProfileDTO {

    private Long id;

    private Long userId;

    private String dietaryPreferences;
    private String favoriteRestaurants;
    private String allergies;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getDietaryPreferences() { return dietaryPreferences; }
    public void setDietaryPreferences(String dietaryPreferences) { this.dietaryPreferences = dietaryPreferences; }

    public String getFavoriteRestaurants() { return favoriteRestaurants; }
    public void setFavoriteRestaurants(String favoriteRestaurants) { this.favoriteRestaurants = favoriteRestaurants; }

    public String getAllergies() { return allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }
}