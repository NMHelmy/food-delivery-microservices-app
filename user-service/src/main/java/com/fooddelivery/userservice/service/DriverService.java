package com.fooddelivery.userservice.service;

import com.fooddelivery.userservice.dto.DriverProfileDTO;
import com.fooddelivery.userservice.model.DriverProfile;
import com.fooddelivery.userservice.repository.DriverProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fooddelivery.userservice.feign.AuthServiceClient;
import com.fooddelivery.userservice.model.DriverStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DriverService {

    @Autowired
    private DriverProfileRepository driverProfileRepository;

    @Autowired
    private AuthServiceClient authServiceClient;

    @Transactional
    public DriverProfile createDriverProfile(DriverProfileDTO dto) {
        try {
            Map<String, Object> userInfo = authServiceClient.getUserInfo(dto.getUserId());
            String role = (String) userInfo.get("role");

            if (!"DELIVERY_DRIVER".equals(role)) {
                throw new RuntimeException("Only users with DELIVERY_DRIVER role can create driver profiles");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to validate user role: " + e.getMessage());
        }

        if (driverProfileRepository.existsByUserId(dto.getUserId())) {
            throw new RuntimeException("Driver profile already exists for this user");
        }

        DriverProfile profile = new DriverProfile();
        profile.setUserId(dto.getUserId());
        profile.setVehicleType(dto.getVehicleType());
        profile.setVehicleNumber(dto.getVehicleNumber());
        profile.setLicenseNumber(dto.getLicenseNumber());

        return driverProfileRepository.save(profile);
    }

    public DriverProfile getDriverProfile(Long userId) {
        return driverProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Driver profile not found"));
    }

    @Transactional
    public DriverProfile updateDriverProfile(Long userId, DriverProfileDTO dto) {
        DriverProfile profile = getDriverProfile(userId);

        if (dto.getVehicleType() != null) profile.setVehicleType(dto.getVehicleType());
        if (dto.getVehicleNumber() != null) profile.setVehicleNumber(dto.getVehicleNumber());
        if (dto.getLicenseNumber() != null) profile.setLicenseNumber(dto.getLicenseNumber());

        return driverProfileRepository.save(profile);
    }

    @Transactional
    public DriverProfile updateDriverStatus(Long userId, String statusString) {
        DriverProfile profile = getDriverProfile(userId);

        try {
            DriverStatus status = DriverStatus.valueOf(statusString.toUpperCase());
            profile.setStatus(status);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status. Must be: AVAILABLE, BUSY, or OFFLINE");
        }

        return driverProfileRepository.save(profile);
    }

    @Transactional
    public DriverProfile updateDriverLocation(Long userId, Double latitude, Double longitude) {
        DriverProfile profile = getDriverProfile(userId);

        if (latitude == null || longitude == null) {
            throw new RuntimeException("Latitude and longitude are required");
        }

        if (latitude < -90 || latitude > 90) {
            throw new RuntimeException("Latitude must be between -90 and 90");
        }

        if (longitude < -180 || longitude > 180) {
            throw new RuntimeException("Longitude must be between -180 and 180");
        }

        profile.setCurrentLatitude(latitude);
        profile.setCurrentLongitude(longitude);
        return driverProfileRepository.save(profile);
    }

    public List<DriverProfile> getAvailableDrivers() {
        return driverProfileRepository.findByStatus(String.valueOf(DriverStatus.AVAILABLE));
    }
    public List<DriverProfile> getAllDrivers() {
        return driverProfileRepository.findAll();
    }
}