package com.fooddelivery.authservice.service;

import com.fooddelivery.authservice.dto.RegisterRequest;
import com.fooddelivery.authservice.model.Role;
import com.fooddelivery.authservice.model.User;
import com.fooddelivery.authservice.model.DriverStatus;
import com.fooddelivery.authservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fooddelivery.authservice.exception.BadRequestException;
import com.fooddelivery.authservice.exception.ResourceNotFoundException;
import com.fooddelivery.authservice.exception.UnauthorizedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public User registerUser(RegisterRequest request) {
        // Validate email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        // Validate phone number uniqueness
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new BadRequestException("Phone number already exists");
        }

        // Create new user
        User user = new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getRole(),
                request.getFullName(),
                request.getPhoneNumber()
        );

        return userRepository.save(user);
    }

    public User login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.isActive()) {
            throw new UnauthorizedException("Account is deactivated");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        return user;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }

    @Transactional
    public User updateUser(Long userId, String fullName, String phoneNumber) {
        User user = getUserById(userId);

        if (fullName != null && !fullName.isEmpty()) {
            user.setFullName(fullName);
        }

        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            // Check if phone number is already taken by another user
            if (!phoneNumber.equals(user.getPhoneNumber()) &&
                    userRepository.existsByPhoneNumber(phoneNumber)) {
                throw new BadRequestException("Phone number already exists");
            }
            user.setPhoneNumber(phoneNumber);
        }

        return userRepository.save(user);
    }

    @Transactional
    public User updateDriverProfile(Long userId, String vehicleType, String vehicleNumber, String status) {
        User user = getUserById(userId);

        if (user.getRole() != Role.DELIVERY_DRIVER) {
            throw new BadRequestException("User is not a driver");
        }

        if (vehicleType != null && !vehicleType.isEmpty()) {
            user.setVehicleType(vehicleType);
        }

        if (vehicleNumber != null && !vehicleNumber.isEmpty()) {
            user.setVehicleNumber(vehicleNumber);
        }

        if (status != null && !status.isEmpty()) {
            try {
                user.setDriverStatus(DriverStatus.valueOf(status.toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid driver status: " + status);
            }
        }

        return userRepository.save(user);
    }

    @Transactional
    public User updateDriverStatus(Long userId, DriverStatus status) {
        User user = getUserById(userId);

        if (user.getRole() != Role.DELIVERY_DRIVER) {
            throw new BadRequestException("User is not a driver");
        }

        user.setDriverStatus(status);
        return userRepository.save(user);
    }

    public List<User> getAvailableDrivers() {
        return userRepository.findByRoleAndDriverStatus(Role.DELIVERY_DRIVER, DriverStatus.AVAILABLE);
    }

    // Password Management
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getUserById(userId);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadRequestException("Invalid old password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public String generatePasswordResetToken(String email) {
        User user = getUserByEmail(email);

        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1)); // Token valid for 1 hour

        userRepository.save(user);
        return resetToken;
    }

    @Transactional
    public void resetPassword(String resetToken, String newPassword) {
        User user = userRepository.findByResetToken(resetToken)
                .orElseThrow(() -> new BadRequestException("Invalid reset token"));

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Reset token has expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);

        userRepository.save(user);
    }

    // Admin Functions
    @Transactional
    public void deactivateUser(Long userId) {
        User user = getUserById(userId);
        user.setActive(false);
        userRepository.save(user);
    }

    @Transactional
    public void activateUser(Long userId) {
        User user = getUserById(userId);
        user.setActive(true);
        userRepository.save(user);
    }

    @Transactional
    public void verifyEmail(Long userId) {
        User user = getUserById(userId);
        user.setEmailVerified(true);
        userRepository.save(user);
    }
}