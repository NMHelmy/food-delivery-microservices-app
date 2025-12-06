package com.fooddelivery.userservice.service;

import com.fooddelivery.userservice.dto.AddressDTO;
import com.fooddelivery.userservice.dto.CustomerProfileDTO;
import com.fooddelivery.userservice.model.Address;
import com.fooddelivery.userservice.model.CustomerProfile;
import com.fooddelivery.userservice.repository.AddressRepository;
import com.fooddelivery.userservice.repository.CustomerProfileRepository;
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
        if (customerProfileRepository.existsByUserId(dto.getUserId())) {
            throw new RuntimeException("Customer profile already exists for this user");
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
                .orElseThrow(() -> new RuntimeException("Customer profile not found"));
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
    public Address addAddress(AddressDTO dto) {
        // If this is the first address or marked as default, set it as default
        List<Address> existingAddresses = addressRepository.findByUserId(dto.getUserId());

        if (dto.isDefault() || existingAddresses.isEmpty()) {
            // Unset any existing default address
            existingAddresses.stream()
                    .filter(Address::isDefault)
                    .forEach(addr -> {
                        addr.setDefault(false);
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
        address.setDefault(dto.isDefault() || existingAddresses.isEmpty());

        return addressRepository.save(address);
    }

    public List<Address> getAddressesByUserId(Long userId) {
        return addressRepository.findByUserId(userId);
    }

    public Address getAddressById(Long addressId) {
        return addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));
    }

    @Transactional
    public Address updateAddress(Long addressId, AddressDTO dto) {
        Address address = getAddressById(addressId);

        if (dto.getLabel() != null) address.setLabel(dto.getLabel());
        if (dto.getStreetAddress() != null) address.setStreetAddress(dto.getStreetAddress());
        if (dto.getCity() != null) address.setCity(dto.getCity());
        if (dto.getState() != null) address.setState(dto.getState());
        if (dto.getZipCode() != null) address.setZipCode(dto.getZipCode());
        if (dto.getLandmark() != null) address.setLandmark(dto.getLandmark());

        // If setting this as default, unset other defaults
        if (dto.isDefault() && !address.isDefault()) {
            addressRepository.findByUserId(address.getUserId()).stream()
                    .filter(Address::isDefault)
                    .forEach(addr -> {
                        addr.setDefault(false);
                        addressRepository.save(addr);
                    });
            address.setDefault(true);
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
                .orElseThrow(() -> new RuntimeException("No default address found"));
    }
}