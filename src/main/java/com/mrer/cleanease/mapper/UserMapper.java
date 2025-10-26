package com.mrer.cleanease.mapper;

import com.mrer.cleanease.dto.request.RegisterUserRequest;
import com.mrer.cleanease.dto.response.UserResponse;
import com.mrer.cleanease.entity.Enums;
import com.mrer.cleanease.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(RegisterUserRequest dto){
        if(dto == null) return null;

        User user = new User();
        user.setFirstname(dto.getFirstname());
        user.setLastname(dto.getLastname());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setAddress(dto.getAddress());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setRole(dto.getRole() != null ? dto.getRole() : Enums.Role.CUSTOMER);
        return user;
    }

    public UserResponse toResponse(User user){
        if (user == null) return null;
        return UserResponse.builder()
                .id(user.getId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .role(user.getRole())
                .emailVerified(user.isEmailVerified())
                .active(user.isActive())
                .build();
    }

    public User toEntity(UserResponse dto) {
        if (dto == null) return null;

        User user = new User();
        user.setId(dto.getId());
        user.setFirstname(dto.getFirstname());
        user.setLastname(dto.getLastname());
        user.setEmail(dto.getEmail());
        user.setAddress(dto.getAddress());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setRole(dto.getRole());
        user.setEmailVerified(dto.isEmailVerified());
        user.setActive(dto.isActive());
        return user;
    }
}
