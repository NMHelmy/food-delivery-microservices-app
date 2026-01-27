package com.fooddelivery.deliveryservice.repository;

import com.fooddelivery.deliveryservice.model.Delivery;
import com.fooddelivery.deliveryservice.model.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    Optional<Delivery> findByOrderId(Long orderId);

    List<Delivery> findByCustomerId(Long customerId);

    List<Delivery> findByDriverId(Long driverId);

    List<Delivery> findByRestaurantId(Long restaurantId);

    List<Delivery> findByStatus(DeliveryStatus status);

    List<Delivery> findByDriverIdAndStatusIn(Long driverId, List<DeliveryStatus> statuses);

}
