package com.mrer.cleanease.service.impl;

import com.mrer.cleanease.ExceptionHandler.UserAlreadyExistException;
import com.mrer.cleanease.ExceptionHandler.UserNotFoundException;
import com.mrer.cleanease.dto.request.UpdateRoleRequest;
import com.mrer.cleanease.service.UserService;
import com.mrer.cleanease.dto.request.RegisterUserRequest;
import com.mrer.cleanease.dto.response.UserResponse;
import com.mrer.cleanease.entity.Enums;
import com.mrer.cleanease.entity.User;
import com.mrer.cleanease.mapper.UserMapper;
import com.mrer.cleanease.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.channels.AcceptPendingException;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {


    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JWTService jwtService;
    private final BCryptPasswordEncoder passwordEncoder;
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[\\+]?[1-9]?[0-9]{7,15}$");

    @Override
    public UserResponse registerUser(RegisterUserRequest request) {
        log.info("Attempting to register new user with email: {}", request.getEmail());
        try{
            validateUserRegistrationRequest(request);
            validateUserUniqueness(request.getEmail(), request.getPhoneNumber());

            User user = userMapper.toEntity(request);
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            if(user.getRole() == null){
                user.setRole(Enums.Role.CUSTOMER);
            }
            User savedUser = userRepository.save(user);
            log.info("User registered successfully with ID: {} and email: {}", savedUser.getId(), savedUser.getEmail());
            return userMapper.toResponse(savedUser);
        } catch (UserAlreadyExistException | IllegalArgumentException e){
            log.warn("User registration failed for email {}: {}", request.getEmail(), e.getMessage(), e);
            throw e;
        } catch (Exception e){
            log.error("Unexpected error during user registration for email {}: {}",request.getEmail(),e.getMessage(), e);
            throw new RuntimeException("Failed to register user, Please try again.", e);

        }
    }

    @Override
   @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.debug("Fetching user by ID: {}", id);
        if (id == null || id <= 0){
            throw new IllegalArgumentException("User ID must be a positive number");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", id);
                    return new UserNotFoundException("User not found with id: " + id);
                });
        log.debug("Successfully retrieved user: {} {}", user.getFirstname(), user.getLastname());
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        log.debug("Fetching user by email: {}", email);

        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }

        User user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", email);
                    return new UserNotFoundException("User not found with email: " + email);
                });

        log.debug("Successfully retrieved user by email: {}", email);
        return userMapper.toResponse(user);
    }

    @Override
    public List<UserResponse> getUserByRole(Enums.Role role) {
        log.debug("Fetching users by roles: {}", role);
        if (role == null) throw new IllegalArgumentException("Role cannot be null");
        List<User> users = userRepository.findByRole(role);
        log.info("Found {} with role: {}", users.size(), role);
        return users.stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    public List<UserResponse> getAllActiveUser() {
        log.debug("Fetching all active users");

        List<User> activeUsers = userRepository.findByActiveTrue();

        log.info("Found {} active users", activeUsers.size());

        return activeUsers.stream()
                .map(userMapper::toResponse)
                .toList();

    }

    @Override
    public UserResponse updateUser(Long id, RegisterUserRequest request) {
        log.info("Updating user with Id: {}", id);
        if(id == null || id <= 0)
            throw new IllegalArgumentException("User Id must be a positive Number");
        try{
            User existing = userRepository.findById(id)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: {}" + id));
            validateUserUpdateRequest(id, request);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentEmail = authentication.getName();
            log.info("Update under process");
            User currentUser = userRepository.findByEmail(currentEmail)
                    .orElseThrow(()  -> new UserNotFoundException("Current logged-in user not found"));


            if(request.getRole() != null && request.getRole() != existing.getRole()){
                throw new AccessDeniedException("Role cannot be changed here. Use admin update API.");
            }

            // update allowed fields like : Firstname, lastname, email, phoneNumber, password
            updateUserFields(existing, request);

            User updatedUser = userRepository.save(existing);
            log.info("User updated successfully: ID={}, Email={}",
                    updatedUser.getId(), updatedUser.getEmail());

            return userMapper.toResponse(updatedUser);

        } catch (UserNotFoundException | UserAlreadyExistException | IllegalArgumentException e) {
            log.warn("User update failed for ID {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during user update for ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to update user. Please try again.", e);
        }

    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateUserRole(Long id, UpdateRoleRequest request) {
        log.info("Admin requested to update role for User ID {}: NewRole={}", id, request);

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("User Id must be a positive number");
        }
        try {
            User existingUser = userRepository.findById(id)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

            // Update role
            existingUser.setRole(request.getRole());

            User updatedUser = userRepository.save(existingUser);
            log.info("User role updated successfully: ID={}, NewRole={}",
                    updatedUser.getId(), updatedUser.getRole());

            return userMapper.toResponse(updatedUser);

        } catch (UserNotFoundException e) {
            log.warn("User role update failed for ID {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during role update for ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to update user role. Please try again.", e);
        }
    }

    @Override
    public void deactivateUser(Long id) {
        log.info("Deactivating user with ID: {}", id);

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("User ID must be a positive number");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        if (!user.isActive()) {
            log.warn("User with ID {} is already deactivated", id);
            return;
        }

        user.setActive(false);
        userRepository.save(user);

        log.info("User deactivated successfully: ID={}, Email={}", id, user.getEmail());
    }

    @Override
    public void activateUser(Long id) {
        log.info("Activating user with ID: {}", id);

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("User ID must be a positive number");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: {}" + id));
        if (user.isActive()){
            log.warn("User with Id {} is already active", id);
            return;
        }
        user.setActive(true);
        userRepository.save(user);
        log.info("User activated successfully: ID={}, Email={}", id, user.getEmail());
    }

    @Override
    public boolean existsByEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }
        return userRepository.existsByEmail(email.toLowerCase().trim());
    }

    @Override
    public boolean existsByPhoneNumber(String phoneNumber) {
        if (!StringUtils.hasText(phoneNumber)) {
            return false;
        }
        return userRepository.existsByPhoneNumber(phoneNumber.trim());
    }

    @Override
    @Transactional
    public User findUserEntityByEmail(String email) {
        log.debug("Finding user entity by email: {}", email);
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }

        return userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> {
                    log.warn("User entity not found with email: {}", email);
                    return new UserNotFoundException("User not found with email: " + email);
                });
    }

    //Helper Methods
    private void validateUserRegistrationRequest(RegisterUserRequest request) {
        if (request == null){
            throw new IllegalArgumentException("Registration can not be null");
        }
        validateRequiredFields(request); // basic nullable checks
        validatePasswords(request.getPassword(), request.getConfirmPassword());
        validateEmailFormat(request.getEmail());
        validatePhoneNumber(request.getPhoneNumber());
        validateNames(request.getFirstname(), request.getLastname());

    }
    private  void validateRequiredFields(RegisterUserRequest request){
        if(!StringUtils.hasText(request.getFirstname())){
            throw new IllegalArgumentException("first name is required");
        }
        if (!StringUtils.hasText(request.getLastname())){
            throw new IllegalArgumentException("Last name is required");
        }
        if (!StringUtils.hasText(request.getEmail())) {
            throw new IllegalArgumentException("Email is required");
        }
        if (!StringUtils.hasText(request.getPassword())) {
            throw new IllegalArgumentException("Password is required");
        }
        if (!StringUtils.hasText(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Password confirmation is required");
        }
        if (!StringUtils.hasText(request.getAddress())) {
            throw new IllegalArgumentException("Address is required");
        }
        if (!StringUtils.hasText(request.getPhoneNumber())) {
            throw new IllegalArgumentException("Phone number is required");
        }
    }

    private void validatePasswords(String password, String confirmPassword){
        if(!password.equals(confirmPassword)){
            throw new IllegalArgumentException("Passwords do not match");
        }
        if (password.length() < 8){
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()){
            throw new IllegalArgumentException( "Password must contain at least one uppercase letter, one lowercase letter, " +
                    "one digit, and one special character (@$!%*?&)");
        }
    }

    private void validateEmailFormat(String email){
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        if (!Pattern.compile(emailRegex).matcher(email).matches()){
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    private void validatePhoneNumber(String phoneNumber){
        String cleanPhone = phoneNumber.replaceAll("[\\s()-]","");
        if (!PHONE_PATTERN.matcher(phoneNumber).matches()){
            throw new IllegalArgumentException("Invalid phone Number format");
        }
    }

    private void validateNames(String firstname, String lastname){
        if (firstname.length() < 2 || firstname.length() > 50) {
            throw new IllegalArgumentException("First name must be between 2 and 50 characters");
        }
        if (lastname.length() < 2 || lastname.length() > 50) {
            throw new IllegalArgumentException("Last name must be between 2 and 50 characters");
        }
        String namePattern = "^[a-zA-Z\\s'-]+$";
        if (!Pattern.compile(namePattern).matcher(firstname).matches()) {
            throw new IllegalArgumentException("First name contains invalid characters");
        }
        if (!Pattern.compile(namePattern).matcher(lastname).matches()) {
            throw new IllegalArgumentException("Last name contains invalid characters");
        }
    }

    private void validateUserUniqueness(String email, String phoneNumber) {
        if (userRepository.existsByEmail(email.toLowerCase().trim())) {
            throw new UserAlreadyExistException("User already exists with email: " + email);
        }

        if (userRepository.existsByPhoneNumber(phoneNumber.trim())) {
            throw new UserAlreadyExistException("Phone number already in use: " + phoneNumber);
        }
    }

    private void validateUserUpdateRequest(Long userId, RegisterUserRequest request) {
        validateRequiredFields(request);
        validateNames(request.getFirstname(), request.getLastname());
        validateEmailFormat(request.getEmail());
        validatePhoneNumber(request.getPhoneNumber());
        // Check for conflicts with other users
        validateUpdateConflicts(userId, request.getEmail(), request.getPhoneNumber());
    }

    private void validateUpdateConflicts(Long userId, String email, String phoneNumber) {
        // Check email conflict
        Optional<User> userWithEmail = userRepository.findByEmail(email.toLowerCase().trim());
        if (userWithEmail.isPresent() && !userWithEmail.get().getId().equals(userId)) {
            throw new UserAlreadyExistException("Email already in use by another user: " + email);
        }

        // Check phone conflict
        userRepository.findAll().stream()
                .filter(user -> user.getPhoneNumber().equals(phoneNumber.trim()))
                .filter(user -> !user.getId().equals(userId))
                .findFirst()
                .ifPresent(user -> {
                    throw new UserAlreadyExistException("Phone number already in use by another user: " + phoneNumber);
                });
    }

    private void updateUserFields(User existingUser, RegisterUserRequest request) {
        existingUser.setFirstname(request.getFirstname().trim());
        existingUser.setLastname(request.getLastname().trim());
        existingUser.setAddress(request.getAddress().trim());
        existingUser.setPhoneNumber(request.getPhoneNumber().trim());
        // Update email if changed
        String newEmail = request.getEmail().toLowerCase().trim();
        if (!existingUser.getEmail().equals(newEmail)) {
            existingUser.setEmail(newEmail);
            existingUser.setEmailVerified(false); // Reset email verification
        }
        if (StringUtils.hasText(request.getPassword())) {
            validatePasswords(request.getPassword(), request.getConfirmPassword());
            existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
        }
//        if (request.getRole() != null && !request.getRole().equals(existingUser.getRole())) {
//            existingUser.setRole(request.getRole());
//        }
    }
}
