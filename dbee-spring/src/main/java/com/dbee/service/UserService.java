package com.dbee.service;

import com.dbee.controller.AuthDtos;
import com.dbee.model.User;
import com.dbee.controller.ApiException;
import com.dbee.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(AuthDtos.RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "An account with that email already exists.");
        }
        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        return userRepository.save(user);
    }

    public User authenticate(AuthDtos.LoginRequest request) {
        User user = userRepository.findByEmail(normalizeEmail(request.email()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password."));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
        }
        return user;
    }

    public User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Authentication required."));
    }

    @Transactional
    public User updateProfile(Long userId, AuthDtos.ProfileRequest request) {
        User user = requireUser(userId);
        String email = normalizeEmail(request.email());
        userRepository.findByEmail(email).filter(existing -> !existing.getId().equals(userId))
                .ifPresent(existing -> { throw new ApiException(HttpStatus.CONFLICT, "That email is already in use."); });
        user.setName(request.name().trim());
        user.setEmail(email);
        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(Long userId, AuthDtos.PasswordRequest request) {
        User user = requireUser(userId);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Current password is incorrect.");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    public static String normalizeEmail(String email) { return email.trim().toLowerCase(Locale.ROOT); }

    public static AuthDtos.UserResponse toResponse(User user) {
        return new AuthDtos.UserResponse(user.getId(), user.getName(), user.getEmail(), user.getCreatedAt());
    }
}
