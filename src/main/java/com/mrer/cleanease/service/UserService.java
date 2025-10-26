package com.mrer.cleanease.service;

import com.mrer.cleanease.dto.request.RegisterUserRequest;
import com.mrer.cleanease.dto.request.UpdateRoleRequest;
import com.mrer.cleanease.dto.response.UserResponse;
import com.mrer.cleanease.entity.Enums;
import com.mrer.cleanease.entity.User;

import java.util.List;

public interface UserService {
    UserResponse registerUser(RegisterUserRequest request);
    UserResponse getUserById(Long id);
    UserResponse getUserByEmail(String email);
    List<UserResponse> getUserByRole(Enums.Role role);
    List<UserResponse> getAllActiveUser();
    UserResponse updateUser(Long id, RegisterUserRequest request);
    UserResponse updateUserRole(Long id, UpdateRoleRequest request);
    void deactivateUser(Long id);
    void activateUser(Long id);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    User findUserEntityByEmail(String email);
}
