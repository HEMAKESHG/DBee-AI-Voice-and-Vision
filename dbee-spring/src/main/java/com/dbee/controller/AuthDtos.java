package com.dbee.controller;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public final class AuthDtos {
    private AuthDtos() {}

    public record RegisterRequest(
            @NotBlank @Size(min = 2, max = 100) String name,
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank @Size(min = 8, max = 128) String password) {}

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password) {}

    public record ProfileRequest(
            @NotBlank @Size(min = 2, max = 100) String name,
            @NotBlank @Email @Size(max = 254) String email) {}

    public record PasswordRequest(
            @NotBlank @Size(min = 8, max = 128) String currentPassword,
            @NotBlank @Size(min = 8, max = 128) String newPassword) {}

    public record UserResponse(Long id, String name, String email, Instant createdAt) {}
    public record UserEnvelope(UserResponse user, String token) {
        public UserEnvelope(UserResponse user) {
            this(user, null);
        }
    }
}
