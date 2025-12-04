package com.gamestore.authservice.controller;

import com.gamestore.authservice.model.Role;
import com.gamestore.authservice.model.User;
import com.gamestore.authservice.security.JwtService;
import com.gamestore.authservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired private UserService userService;
    @Autowired private JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        User user = userService.registerUser(email, password, Role.USER);
        String token = jwtService.generateToken(Map.of(
                "userId", user.getId(),
                "role", user.getRole().name()
        ));

        return ResponseEntity.ok(Map.of(
                "message", "User registered successfully",
                "token", token
        ));
    }

    @PostMapping("/register-admin")
    public ResponseEntity<?> registerAdmin(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        User user = userService.registerUser(email, password, Role.ADMIN);
        String token = jwtService.generateToken(Map.of(
                "userId", user.getId(),
                "role", user.getRole().name()
        ));

        return ResponseEntity.ok(Map.of(
                "message", "Admin registered successfully",
                "token", token
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        User user = userService.login(email, password);
        String token = jwtService.generateToken(Map.of(
                "userId", user.getId(),
                "role", user.getRole().name()
        ));

        return ResponseEntity.ok(Map.of(
                "message", "Login successful",
                "token", token
        ));
    }
}
