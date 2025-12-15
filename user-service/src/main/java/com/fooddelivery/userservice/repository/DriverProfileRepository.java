package com.fooddelivery.userservice.repository;

import com.fooddelivery.userservice.model.DriverProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.fooddelivery.userservice.model.DriverStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverProfileRepository extends JpaRepository<DriverProfile, Long> {
    Optional<DriverProfile> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
    List<DriverProfile> findByStatus(DriverStatus status);
    void deleteByUserId(Long userId);
}