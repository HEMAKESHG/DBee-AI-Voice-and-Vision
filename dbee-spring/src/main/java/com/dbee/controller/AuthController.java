package com.dbee.controller;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dbee.config.AppProperties;
import com.dbee.model.User;
import com.dbee.config.JwtService;
import com.dbee.service.UserService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final JwtService jwtService;
    private final AppProperties properties;

    public AuthController(UserService userService, JwtService jwtService, AppProperties properties) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.properties = properties;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthDtos.UserEnvelope> register(@Valid @RequestBody AuthDtos.RegisterRequest request, HttpServletResponse response) {
        User user = userService.register(request);
        writeCookie(response, jwtService.createToken(user.getId()), Duration.ofMillis(properties.jwtExpirationMs()));
        return ResponseEntity.status(201).body(new AuthDtos.UserEnvelope(UserService.toResponse(user)));
    }

    @PostMapping("/login")
    public AuthDtos.UserEnvelope login(@Valid @RequestBody AuthDtos.LoginRequest request, HttpServletResponse response) {
        User user = userService.authenticate(request);
        writeCookie(response, jwtService.createToken(user.getId()), Duration.ofMillis(properties.jwtExpirationMs()));
        return new AuthDtos.UserEnvelope(UserService.toResponse(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        writeCookie(response, "", Duration.ZERO);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public AuthDtos.UserEnvelope me(@AuthenticationPrincipal Long userId) {
        return new AuthDtos.UserEnvelope(UserService.toResponse(userService.requireUser(userId)));
    }

    @PatchMapping("/profile")
    public AuthDtos.UserEnvelope updateProfile(@AuthenticationPrincipal Long userId, @Valid @RequestBody AuthDtos.ProfileRequest request) {
        return new AuthDtos.UserEnvelope(UserService.toResponse(userService.updateProfile(userId, request)));
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal Long userId, @Valid @RequestBody AuthDtos.PasswordRequest request) {
        userService.changePassword(userId, request);
        return ResponseEntity.noContent().build();
    }

    private void writeCookie(HttpServletResponse response, String value, Duration maxAge) {
        ResponseCookie cookie = ResponseCookie.from(properties.jwtCookieName(), value)
                .httpOnly(true)
                .secure(properties.cookieSecure())
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAge)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
