package com.fooddelivery.orderservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "restaurant-service")
public interface RestaurantServiceClient {

    // Get restaurant details
    @GetMapping("/restaurants/{restaurantId}")
    Map<String, Object> getRestaurant(@PathVariable("restaurantId") Long restaurantId);

    // Get menu item details
    @GetMapping("/restaurants/{restaurantId}/menu/{menuItemId}")
    Map<String, Object> getMenuItem(
            @PathVariable("restaurantId") Long restaurantId,
            @PathVariable("menuItemId") Long menuItemId
    );
}