package com.fooddelivery.userservice.service;

import com.fooddelivery.userservice.dto.DriverProfileDTO;
import com.fooddelivery.userservice.model.DriverProfile;
import com.fooddelivery.userservice.model.DriverStatus;
import com.fooddelivery.userservice.repository.DriverProfileRepository;
import com.fooddelivery.userservice.exception.BadRequestException;
import com.fooddelivery.userservice.exception.ResourceNotFoundException;
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
            throw new BadRequestException("Driver profile already exists for this user");
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
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found for user with id: " + userId));
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
            throw new BadRequestException("Invalid status. Must be: AVAILABLE, BUSY, or OFFLINE");
        }

        return driverProfileRepository.save(profile);
    }

    @Transactional
    public DriverProfile updateDriverLocation(Long userId, Double latitude, Double longitude) {
        DriverProfile profile = getDriverProfile(userId);

        if (latitude == null || longitude == null) {
            throw new BadRequestException("Latitude and longitude are required");
        }

        if (latitude < -90 || latitude > 90) {
            throw new BadRequestException("Latitude must be between -90 and 90");
        }

        if (longitude < -180 || longitude > 180) {
            throw new BadRequestException("Longitude must be between -180 and 180");
        }

        profile.setCurrentLatitude(latitude);
        profile.setCurrentLongitude(longitude);
        return driverProfileRepository.save(profile);
    }

    public List<DriverProfile> getAvailableDrivers() {
        return driverProfileRepository.findByStatus(DriverStatus.AVAILABLE);
    }

    public List<DriverProfile> getAllDrivers() {
        return driverProfileRepository.findAll();
    }

    public void deleteDriverProfile(Long userId) {
        driverProfileRepository.deleteByUserId(userId);
    }

}