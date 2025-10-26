package com.mrer.cleanease.service.impl;

import com.mrer.cleanease.dto.request.LoginRequest;
import com.mrer.cleanease.dto.request.RegisterUserRequest;
import com.mrer.cleanease.dto.response.AuthResponse;
import com.mrer.cleanease.dto.response.UserResponse;
import com.mrer.cleanease.entity.User;
import com.mrer.cleanease.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;


    public AuthResponse register(RegisterUserRequest request){
        log.info("Registration attempt for email: {}", request.getEmail());

        UserResponse registeredUser = userService.registerUser(request);
        User userEntity = userService.findUserEntityByEmail(registeredUser.getEmail());

        String jwtToken = jwtService.generateToken(userEntity);
//        String refreshToken = jwtService.generateRefreshToken(userEntity);

        log.info("User registered and authenticated successfully: {}", request.getEmail());

        return AuthResponse.builder()
                .token(jwtToken)
                .expiresIn(86400L)
                .user(registeredUser)
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        // Authenticate user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Get user details
        User user = userService.findUserEntityByEmail(request.getEmail());
        UserResponse userResponse = userService.getUserByEmail(request.getEmail());

        // Generate tokens
        String jwtToken = jwtService.generateToken(user);
//        String refreshToken = jwtService.generateRefreshToken(user);

        log.info("User logged in successfully: {}", request.getEmail());

        return AuthResponse.builder()
                .token(jwtToken)
                .expiresIn(86400L) // 24 hours in seconds
                .user(userResponse)
                .build();
    }
}
