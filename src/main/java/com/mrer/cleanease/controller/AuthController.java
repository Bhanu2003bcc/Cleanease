package com.mrer.cleanease.controller;

import com.mrer.cleanease.ExceptionHandler.UserAlreadyExistException;
import com.mrer.cleanease.dto.request.LoginRequest;
import com.mrer.cleanease.dto.request.RegisterUserRequest;
import com.mrer.cleanease.dto.response.ApiResponse;
import com.mrer.cleanease.dto.response.AuthResponse;
import com.mrer.cleanease.service.impl.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterUserRequest request){
        log.info("Registration request received for email: {}", request.getEmail());

        try{
            AuthResponse authResponse = authService.register(request);
            ApiResponse<AuthResponse> response = ApiResponse.<AuthResponse>builder()
                    .success(true)
                    .message("Registration Successful")
                    .data(authResponse)
                    .timestamp(LocalDateTime.now())
                    .build();

            return  ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (UserAlreadyExistException e) {
            log.warn("Registration failed for email: {}, {}", request.getEmail(), e.getMessage());

            ApiResponse<AuthResponse> errorResponse = ApiResponse.<AuthResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (IllegalArgumentException e) {
            log.warn("Registration failed for email {}: {}", request.getEmail(), e.getMessage());

            ApiResponse<AuthResponse> errorResponse = ApiResponse.<AuthResponse>builder()
                    .success(false)
                    .message("Validation failed: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("Unexpected error during registration for email {}: {}", request.getEmail(), e.getMessage(), e);

            ApiResponse<AuthResponse> errorResponse = ApiResponse.<AuthResponse>builder()
                    .success(false)
                    .message("Registration failed. Please try again.")
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for email: {}", request.getEmail());

        try {
            AuthResponse authResponse = authService.login(request);

            ApiResponse<AuthResponse> response = ApiResponse.<AuthResponse>builder()
                    .success(true)
                    .message("Login successful")
                    .data(authResponse)
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            log.warn("Login failed for email {}: Invalid credentials", request.getEmail());

            ApiResponse<AuthResponse> errorResponse = ApiResponse.<AuthResponse>builder()
                    .success(false)
                    .message("Invalid email or password")
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        } catch (Exception e) {
            log.error("Unexpected error during login for email {}: {}", request.getEmail(), e.getMessage(), e);

            ApiResponse<AuthResponse> errorResponse = ApiResponse.<AuthResponse>builder()
                    .success(false)
                    .message("Login failed. Please try again.")
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
