package com.fooddelivery.userservice.service;

import com.fooddelivery.userservice.dto.OwnerProfileDTO;
import com.fooddelivery.userservice.model.OwnerProfile;
import com.fooddelivery.userservice.repository.OwnerProfileRepository;
import com.fooddelivery.userservice.exception.BadRequestException;
import com.fooddelivery.userservice.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OwnerService {

    @Autowired
    private OwnerProfileRepository ownerProfileRepository;

    @Transactional
    public OwnerProfile createOwnerProfile(OwnerProfileDTO dto) {
        // Controller already validated user has RESTAURANT_OWNER role via X-User-Role header
        // Just verify profile doesn't already exist

        if (ownerProfileRepository.existsByUserId(dto.getUserId())) {
            throw new BadRequestException("Owner profile already exists for this user");
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
                .orElseThrow(() -> new ResourceNotFoundException("Owner profile not found for user with id: " + userId));
    }

    @Transactional
    public OwnerProfile updateOwnerProfile(Long userId, OwnerProfileDTO dto) {
        OwnerProfile profile = getOwnerProfile(userId);

        if (dto.getBusinessName() != null) profile.setBusinessName(dto.getBusinessName());
        if (dto.getBusinessAddress() != null) profile.setBusinessAddress(dto.getBusinessAddress());
        if (dto.getTaxId() != null) profile.setTaxId(dto.getTaxId());

        return ownerProfileRepository.save(profile);
    }

    public void deleteOwnerProfile(Long userId) {
        ownerProfileRepository.deleteByUserId(userId);
    }
}