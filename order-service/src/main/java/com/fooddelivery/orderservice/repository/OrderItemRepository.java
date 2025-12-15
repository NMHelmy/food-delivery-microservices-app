package com.fooddelivery.orderservice.repository;

import com.fooddelivery.orderservice.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // Find all items for an order
    List<OrderItem> findByOrderId(Long orderId);
}