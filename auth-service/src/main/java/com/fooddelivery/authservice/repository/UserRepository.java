package com.fooddelivery.authservice.repository;

import com.fooddelivery.authservice.model.Role;
import com.fooddelivery.authservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPhoneNumber(String phoneNumber);
    Optional<User> findByResetToken(String resetToken);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    List<User> findByRole(Role role);
    List<User> findByIsActive(boolean isActive);
}