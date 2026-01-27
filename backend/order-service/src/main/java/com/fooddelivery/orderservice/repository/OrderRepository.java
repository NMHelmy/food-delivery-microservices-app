package com.fooddelivery.orderservice.repository;

import com.fooddelivery.orderservice.model.Order;
import com.fooddelivery.orderservice.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Find orders by customer
    List<Order> findByCustomerId(Long customerId);

    // Find orders by restaurant
    List<Order> findByRestaurantId(Long restaurantId);

    // Find orders by status
    List<Order> findByStatus(OrderStatus status);

    // Find orders by customer and status
    List<Order> findByCustomerIdAndStatus(Long customerId, OrderStatus status);

    // Find orders by restaurant and status
    List<Order> findByRestaurantIdAndStatus(Long restaurantId, OrderStatus status);
}