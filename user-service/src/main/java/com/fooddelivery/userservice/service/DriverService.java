package com.fooddelivery.userservice.service;

import com.fooddelivery.userservice.dto.DriverProfileDTO;
import com.fooddelivery.userservice.model.DriverProfile;
import com.fooddelivery.userservice.repository.DriverProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DriverService {

    @Autowired
    private DriverProfileRepository driverProfileRepository;

    @Transactional
    public DriverProfile createDriverProfile(DriverProfileDTO dto) {
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
    public DriverProfile updateDriverStatus(Long userId, String status) {
        DriverProfile profile = getDriverProfile(userId);
        profile.setStatus(status);
        return driverProfileRepository.save(profile);
    }

    @Transactional
    public DriverProfile updateDriverLocation(Long userId, Double latitude, Double longitude) {
        DriverProfile profile = getDriverProfile(userId);
        profile.setCurrentLatitude(latitude);
        profile.setCurrentLongitude(longitude);
        return driverProfileRepository.save(profile);
    }

    public List<DriverProfile> getAvailableDrivers() {
        return driverProfileRepository.findByStatus("AVAILABLE");
    }

    public List<DriverProfile> getAllDrivers() {
        return driverProfileRepository.findAll();
    }
}