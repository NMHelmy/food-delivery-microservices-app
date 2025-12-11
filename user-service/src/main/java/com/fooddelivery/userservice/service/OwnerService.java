package com.fooddelivery.userservice.service;

import com.fooddelivery.userservice.dto.OwnerProfileDTO;
import com.fooddelivery.userservice.model.OwnerProfile;
import com.fooddelivery.userservice.repository.OwnerProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fooddelivery.userservice.feign.AuthServiceClient;

import java.util.Map;

@Service
public class OwnerService {

    @Autowired
    private OwnerProfileRepository ownerProfileRepository;

    @Autowired
    private AuthServiceClient authServiceClient;

    @Transactional
    public OwnerProfile createOwnerProfile(OwnerProfileDTO dto) {
        try {
            Map<String, Object> userInfo = authServiceClient.getUserInfo(dto.getUserId());
            String role = (String) userInfo.get("role");

            if (!"RESTAURANT_OWNER".equals(role)) {
                throw new RuntimeException("Only users with RESTAURANT_OWNER role can create owner profiles");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to validate user role: " + e.getMessage());
        }

        if (ownerProfileRepository.existsByUserId(dto.getUserId())) {
            throw new RuntimeException("Owner profile already exists for this user");
        }

        OwnerProfile profile = new OwnerProfile();
        profile.setUserId(dto.getUserId());
        profile.setBusinessName(dto.getBusinessName());
        profile.setBusinessAddress(dto.getBusinessAddress());
        profile.setTaxId(dto.getTaxId());

        return ownerProfileRepository.save(profile);
    }

    public OwnerProfile getOwnerProfile(Long userId) {
        return ownerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Owner profile not found"));
    }

    @Transactional
    public OwnerProfile updateOwnerProfile(Long userId, OwnerProfileDTO dto) {
        OwnerProfile profile = getOwnerProfile(userId);

        if (dto.getBusinessName() != null) profile.setBusinessName(dto.getBusinessName());
        if (dto.getBusinessAddress() != null) profile.setBusinessAddress(dto.getBusinessAddress());
        if (dto.getTaxId() != null) profile.setTaxId(dto.getTaxId());

        return ownerProfileRepository.save(profile);
    }
}
