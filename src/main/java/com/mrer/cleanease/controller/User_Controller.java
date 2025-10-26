package com.mrer.cleanease.controller;

import com.mrer.cleanease.ExceptionHandler.UserAlreadyExistException;
import com.mrer.cleanease.ExceptionHandler.UserNotFoundException;
import com.mrer.cleanease.dto.request.UpdateRoleRequest;
import com.mrer.cleanease.service.UserService;
import com.mrer.cleanease.dto.request.RegisterUserRequest;
import com.mrer.cleanease.dto.response.ApiResponse;
import com.mrer.cleanease.dto.response.UserResponse;
import com.mrer.cleanease.entity.Enums;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;



@RestController
@Slf4j
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
public class User_Controller {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterUserRequest request){
        log.info("Received the user registration request for email: {}", request.getEmail());
        try{
            UserResponse userResponse = userService.registerUser(request);

            ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                    .success(true)
                    .message("User registered successfully")
                    .data(userResponse)
                    .timestamp(LocalDateTime.now())
                    .build();

            log.info("User registered successfully with ID: {}", userResponse.getId());
            URI location = URI.create("/api/v1/users"+ userResponse.getId());
            return ResponseEntity.created(location).body(response);
        } catch (UserAlreadyExistException e) {
            log.warn("User registration failed - user already exists: {}", e.getMessage());

            ApiResponse<UserResponse> errorResponse = ApiResponse.<UserResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }catch (IllegalArgumentException e) {
            log.warn("User registration failed - validation error: {}", e.getMessage());

            ApiResponse<UserResponse> errorResponse = ApiResponse.<UserResponse>builder()
                    .success(false)
                    .message("Validation failed: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            log.error("Unexpected error during user registration: {}", e.getMessage(), e);

            ApiResponse<UserResponse> errorResponse = ApiResponse.<UserResponse>builder()
                    .success(false)
                    .message("Registration failed. Please try again later.")
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }

    }
    @GetMapping("/id/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('STAFF')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id){
        log.debug("Received request to get user by id: {}", id);
        try {
            UserResponse userResponse = userService.getUserById(id);
            ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                    .success(true)
                    .message("User retrieved successfully")
                    .data(userResponse)
                    .timestamp(LocalDateTime.now())
                    .build();

            log.debug("User retrieved successfully: {}", id);
            return ResponseEntity.ok(response);
        }  catch (UserNotFoundException e) {
            log.warn("User not found with ID: {}", id);

            ApiResponse<UserResponse> errorResponse = ApiResponse.<UserResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        }
    }
    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('STAFF')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(@PathVariable String email){
        log.debug("Received request to get user by email: {}", email);

        try{
            UserResponse userResponse = userService.getUserByEmail(email);

            ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                    .success(true)
                    .message("User retrieved successfully")
                    .data(userResponse)
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.ok(response);

        } catch (UserNotFoundException e) {
            ApiResponse<UserResponse> errorResponse = ApiResponse.<UserResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByRole(
            @PathVariable Enums.Role role) {

        log.debug("Received request to get users by role: {}", role);

        try {
            List<UserResponse> users = userService.getUserByRole(role);

            ApiResponse<List<UserResponse>> response = ApiResponse.<List<UserResponse>>builder()
                    .success(true)
                    .message("Users retrieved successfully by role: " + role)
                    .data(users)
                    .timestamp(LocalDateTime.now())
                    .metadata(Map.of("role", role.toString(), "count", users.size()))
                    .build();

            log.info("Retrieved {} users with role: {}", users.size(), role);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            ApiResponse<List<UserResponse>> errorResponse = ApiResponse.<List<UserResponse>>builder()
                    .success(false)
                    .message("Invalid role: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllActiveUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "firstname") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir){
        log.debug("Received request to get all active users - page: {}, size: {}", page, size);
        try{
            List<UserResponse> users = userService.getAllActiveUser();
            Sort sort = sortDir.equalsIgnoreCase("desc") ?
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

            ApiResponse<List<UserResponse>> response = ApiResponse.<List<UserResponse>>builder()
                    .success(true)
                    .message("Active users retrieved successfully")
                    .data(users)
                    .timestamp(LocalDateTime.now())
                    .metadata(Map.of(
                            "totalElements", users.size(),
                            "page", page,
                            "size", size,
                            "sortBy", sortBy,
                            "sortDir", sortDir
                    ))
                    .build();

            log.info("Retrieved {} active users", users.size());
            return ResponseEntity.ok(response);
        }  catch (Exception e) {
            log.error("Error retrieving active users: {}", e.getMessage(), e);

            ApiResponse<List<UserResponse>> errorResponse = ApiResponse.<List<UserResponse>>builder()
                    .success(false)
                    .message("Failed to retrieve users")
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and #id == authentication.principal.id)")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable  Long id,
            @Valid @RequestBody RegisterUserRequest request, Authentication authentication) {

        log.info("Received request to update user with ID: {}", id);


        try {
            UserResponse updatedUser = userService.updateUser(id, request);

            ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                    .success(true)
                    .message("User updated successfully")
                    .data(updatedUser)
                    .timestamp(LocalDateTime.now())
                    .build();

            log.info("User updated successfully: {}", id);
            return ResponseEntity.ok(response);

        } catch (UserNotFoundException e) {
            ApiResponse<UserResponse> errorResponse = ApiResponse.<UserResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (UserAlreadyExistException e) {
            ApiResponse<UserResponse> errorResponse = ApiResponse.<UserResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);

        } catch (IllegalArgumentException e) {
            ApiResponse<UserResponse> errorResponse = ApiResponse.<UserResponse>builder()
                    .success(false)
                    .message("Validation failed: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PatchMapping("/update-role/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request) {

        log.info("Received request to update role for user with ID: {}", id);

        UserResponse updatedUser = userService.updateUserRole(id, request);

        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .success(true)
                .message("User role updated successfully")
                .data(updatedUser)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id){
        log.info("Deactivate user with Id: {}", id);
        try{
            userService.deactivateUser(id);

            ApiResponse<Void> response = ApiResponse.<Void>builder()
                    .success(true)
                    .message("Account is deactivated")
                    .timestamp(LocalDateTime.now())
                    .build();
            log.info("User deactivate successfully: {}", id);
            return ResponseEntity.ok(response);
        } catch (UserNotFoundException e) {
            ApiResponse<Void> errorResponse = ApiResponse.<Void>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activate(@PathVariable Long id){
        log.info("Received request to activate user with Id: {}", id);
        try {
            userService.activateUser(id);

            ApiResponse<Void> response = ApiResponse.<Void>builder()
                    .success(true)
                    .message("User activated successfully")
                    .timestamp(LocalDateTime.now())
                    .build();

            log.info("User activated successfully: {}", id);
            return ResponseEntity.ok(response);

        } catch (UserNotFoundException e) {
            ApiResponse<Void> errorResponse = ApiResponse.<Void>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck(){

        log.info("Api run");
        ApiResponse<String> response = ApiResponse.<String>builder()
                .success(true)
                .message("User service is is healthy")
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    private boolean hasRole(Authentication authentication, Enums.Role role) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }
}
