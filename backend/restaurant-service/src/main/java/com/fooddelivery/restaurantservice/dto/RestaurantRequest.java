package com.fooddelivery.restaurantservice.dto;

import jakarta.validation.constraints.*;

public class RestaurantRequest {

    @NotBlank(message = "Restaurant name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Invalid phone number")
    private String phone;

    @NotBlank(message = "Cuisine type is required")
    private String cuisine;

    private String description;

    private String imageUrl;

    // Constructors
    public RestaurantRequest() {
    }

    public RestaurantRequest(String name, String address, String phone, String cuisine,
                             String description, String imageUrl) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.cuisine = cuisine;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCuisine() {
        return cuisine;
    }

    public void setCuisine(String cuisine) {
        this.cuisine = cuisine;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}