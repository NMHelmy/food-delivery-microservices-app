package com.fooddelivery.deliveryservice.feign;

import com.fooddelivery.deliveryservice.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "restaurant-service", path = "/restaurants", configuration = FeignConfig.class)
public interface RestaurantServiceClient {
    @GetMapping("/{restaurantId}")
    Object getRestaurant(@PathVariable Long restaurantId);

    @GetMapping("/owner/{ownerId}")
    Object getRestaurantsByOwnerId(@PathVariable Long ownerId);
}