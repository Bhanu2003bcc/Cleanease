package com.mrer.cleanease.dto.request;

import com.mrer.cleanease.entity.Enums;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterUserRequest {

    @NotBlank
    private String firstname;
    @NotBlank
    private String lastname;
    @Email(message = "Invalid email format")
    @NotBlank
    private String email;
    @NotBlank
    private String password;
    @NotBlank
    private String confirmPassword;
    @NotBlank
    private String address;
    @NotBlank
    private String phoneNumber;

    private Enums.Role role;
}
