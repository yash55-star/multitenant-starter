package com.example.authservice.service;

import com.example.authservice.dto.AuthRequest;
import com.example.authservice.dto.AuthValidationResponse;
import com.example.authservice.entity.AppUser;
import com.example.authservice.repository.AppUserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AppUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public AuthValidationResponse register(AuthRequest request) {
        validateInput(request);
        if (appUserRepository.existsByUsername(request.getUsername())) {
            return new AuthValidationResponse(request.getUsername(), false, "Username already exists");
        }

        AppUser user = new AppUser();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        appUserRepository.save(user);
        return new AuthValidationResponse(request.getUsername(), true, "User registered successfully");
    }

    @Transactional(readOnly = true)
    public AuthValidationResponse validateLogin(AuthRequest request) {
        validateInput(request);
        return appUserRepository.findByUsername(request.getUsername())
                .map(user -> passwordEncoder.matches(request.getPassword(), user.getPasswordHash())
                        ? new AuthValidationResponse(user.getUsername(), true, "Credentials are valid")
                        : new AuthValidationResponse(request.getUsername(), false, "Invalid username or password"))
                .orElseGet(() -> new AuthValidationResponse(request.getUsername(), false, "Invalid username or password"));
    }

    private void validateInput(AuthRequest request) {
        if (request == null || !StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
            throw new IllegalArgumentException("Username and password are required");
        }
    }
}
