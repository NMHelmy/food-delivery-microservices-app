package com.fooddelivery.authservice.service;

import com.fooddelivery.authservice.dto.AddressDTO;
import com.fooddelivery.authservice.exception.ResourceNotFoundException;
import com.fooddelivery.authservice.model.Address;
import com.fooddelivery.authservice.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Transactional
    public Address createAddress(Long userId, AddressDTO dto) {
        Address address = new Address();
        address.setUserId(userId);
        address.setLabel(dto.getLabel());
        address.setStreetAddress(dto.getStreetAddress());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setZipCode(dto.getZipCode());
        address.setIsDefault(dto.getIsDefault());

        // If this is set as default, unset other defaults
        if (dto.getIsDefault()) {
            addressRepository.findByUserIdAndIsDefaultTrue(userId)
                    .ifPresent(existingDefault -> {
                        existingDefault.setIsDefault(false);
                        addressRepository.save(existingDefault);
                    });
        }

        return addressRepository.save(address);
    }

    public List<Address> getUserAddresses(Long userId) {
        return addressRepository.findByUserId(userId);
    }

    public Address getAddressById(Long addressId) {
        return addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
    }

    public Address getAddressByIdAndUserId(Long addressId, Long userId) {
        return addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
    }

    @Transactional
    public Address updateAddress(Long userId, Long addressId, AddressDTO dto) {
        Address address = getAddressByIdAndUserId(addressId, userId);

        address.setLabel(dto.getLabel());
        address.setStreetAddress(dto.getStreetAddress());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setZipCode(dto.getZipCode());

        if (dto.getIsDefault() && !address.getIsDefault()) {
            // Unset other defaults
            addressRepository.findByUserIdAndIsDefaultTrue(userId)
                    .ifPresent(existingDefault -> {
                        if (!existingDefault.getId().equals(addressId)) {
                            existingDefault.setIsDefault(false);
                            addressRepository.save(existingDefault);
                        }
                    });
            address.setIsDefault(true);
        }

        return addressRepository.save(address);
    }

    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        Address address = getAddressByIdAndUserId(addressId, userId);
        addressRepository.delete(address);
    }

    @Transactional
    public Address setDefaultAddress(Long userId, Long addressId) {
        Address address = getAddressByIdAndUserId(addressId, userId);

        // Unset other defaults
        addressRepository.findByUserIdAndIsDefaultTrue(userId)
                .ifPresent(existingDefault -> {
                    if (!existingDefault.getId().equals(addressId)) {
                        existingDefault.setIsDefault(false);
                        addressRepository.save(existingDefault);
                    }
                });

        address.setIsDefault(true);
        return addressRepository.save(address);
    }
}