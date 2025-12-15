package com.fooddelivery.userservice.service;

import com.fooddelivery.userservice.dto.AddressDTO;
import com.fooddelivery.userservice.dto.CustomerProfileDTO;
import com.fooddelivery.userservice.model.Address;
import com.fooddelivery.userservice.model.CustomerProfile;
import com.fooddelivery.userservice.repository.AddressRepository;
import com.fooddelivery.userservice.repository.CustomerProfileRepository;
import com.fooddelivery.userservice.exception.BadRequestException;
import com.fooddelivery.userservice.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {

    @Autowired
    private CustomerProfileRepository customerProfileRepository;

    @Autowired
    private AddressRepository addressRepository;

    // Customer Profile Methods
    @Transactional
    public CustomerProfile createCustomerProfile(CustomerProfileDTO dto) {
        // Controller already validated user has CUSTOMER role via X-User-Role header
        // Just verify profile doesn't already exist

        if (customerProfileRepository.existsByUserId(dto.getUserId())) {
            throw new BadRequestException("Customer profile already exists for this user");
        }

        CustomerProfile profile = new CustomerProfile();
        profile.setUserId(dto.getUserId());
        profile.setDietaryPreferences(dto.getDietaryPreferences());
        profile.setFavoriteRestaurants(dto.getFavoriteRestaurants());
        profile.setAllergies(dto.getAllergies());

        return customerProfileRepository.save(profile);
    }

    public CustomerProfile getCustomerProfile(Long userId) {
        return customerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found for user with id: " + userId));
    }

    @Transactional
    public CustomerProfile updateCustomerProfile(Long userId, CustomerProfileDTO dto) {
        CustomerProfile profile = getCustomerProfile(userId);

        if (dto.getDietaryPreferences() != null) {
            profile.setDietaryPreferences(dto.getDietaryPreferences());
        }
        if (dto.getFavoriteRestaurants() != null) {
            profile.setFavoriteRestaurants(dto.getFavoriteRestaurants());
        }
        if (dto.getAllergies() != null) {
            profile.setAllergies(dto.getAllergies());
        }

        return customerProfileRepository.save(profile);
    }

    // Address Methods
    @Transactional
    public Address createAddress(AddressDTO dto) {
        // If this is the first address or marked as default, set it as default
        List<Address> existingAddresses = addressRepository.findByUserId(dto.getUserId());

        if (dto.getIsDefault() || existingAddresses.isEmpty()) {
            // Unset any existing default address
            existingAddresses.stream()
                    .filter(Address::getIsDefault)
                    .forEach(addr -> {
                        addr.setIsDefault(false);
                        addressRepository.save(addr);
                    });
        }

        Address address = new Address();
        address.setUserId(dto.getUserId());
        address.setLabel(dto.getLabel());
        address.setStreetAddress(dto.getStreetAddress());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setZipCode(dto.getZipCode());
        address.setLandmark(dto.getLandmark());
        address.setIsDefault(dto.getIsDefault() || existingAddresses.isEmpty());

        return addressRepository.save(address);
    }

    public List<Address> getAddressesByUserId(Long userId) {
        return addressRepository.findByUserId(userId);
    }

    public Address getAddressById(Long addressId) {
        return addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));
    }

    @Transactional
    public Address updateAddress(Long addressId, AddressDTO dto) {
        Address address = getAddressById(addressId);

        if (dto.getLabel() != null) {
            address.setLabel(dto.getLabel());
        }
        if (dto.getStreetAddress() != null) {
            address.setStreetAddress(dto.getStreetAddress());
        }
        if (dto.getCity() != null) {
            address.setCity(dto.getCity());
        }
        if (dto.getState() != null) {
            address.setState(dto.getState());
        }
        if (dto.getZipCode() != null) {
            address.setZipCode(dto.getZipCode());
        }
        if (dto.getLandmark() != null) {
            address.setLandmark(dto.getLandmark());
        }

        // If setting this as default, unset other defaults
        if (dto.getIsDefault() && !address.getIsDefault()) {
            addressRepository.findByUserId(address.getUserId()).stream()
                    .filter(Address::getIsDefault)
                    .forEach(addr -> {
                        addr.setIsDefault(false);
                        addressRepository.save(addr);
                    });
            address.setIsDefault(true);
        }

        return addressRepository.save(address);
    }

    @Transactional
    public void deleteAddress(Long addressId) {
        Address address = getAddressById(addressId);
        addressRepository.delete(address);
    }

    public Address getDefaultAddress(Long userId) {
        return addressRepository.findByUserIdAndIsDefault(userId, true)
                .orElseThrow(() -> new ResourceNotFoundException("No default address found for user with id: " + userId));
    }

    public void deleteCustomerProfile(Long userId) {
        customerProfileRepository.deleteByUserId(userId);
    }
}