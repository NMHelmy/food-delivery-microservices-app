package com.fooddelivery.restaurantservice.repository;

import com.fooddelivery.restaurantservice.models.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    List<Restaurant> findByIsActiveTrue();

    List<Restaurant> findByCuisineAndIsActiveTrue(String cuisine);

    List<Restaurant> findByOwnerId(Long ownerId);

    List<Restaurant> findByNameContainingIgnoreCaseAndIsActiveTrue(String name);
}