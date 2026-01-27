package com.fooddelivery.cartservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "RESTAURANT-SERVICE")
public interface RestaurantServiceClient {

    @GetMapping("/restaurants/{restaurantId}")
    Map<String, Object> getRestaurant(@PathVariable Long restaurantId);

    @GetMapping("/restaurants/{restaurantId}/menu/{menuItemId}")
    Map<String, Object> getMenuItem(
            @PathVariable Long restaurantId,
            @PathVariable Long menuItemId);
}
