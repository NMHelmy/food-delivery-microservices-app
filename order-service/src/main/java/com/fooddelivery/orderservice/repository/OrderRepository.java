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

    // Find orders by driver
    List<Order> findByDriverId(Long driverId);

    // Find orders by status
    List<Order> findByStatus(OrderStatus status);

    // Find orders by customer and status
    List<Order> findByCustomerIdAndStatus(Long customerId, OrderStatus status);

    // Find orders by restaurant and status
    List<Order> findByRestaurantIdAndStatus(Long restaurantId, OrderStatus status);

    // Find orders by driver and status
    List<Order> findByDriverIdAndStatus(Long driverId, OrderStatus status);

    // Find active orders for driver (orders that are assigned but not delivered/cancelled)
    List<Order> findByDriverIdAndStatusIn(Long driverId, List<OrderStatus> statuses);
}