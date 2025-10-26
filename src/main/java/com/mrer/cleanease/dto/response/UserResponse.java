package com.mrer.cleanease.dto.response;

import com.mrer.cleanease.entity.Enums;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String firstname;
    private String lastname;
    private String email;
    private String address;
    private String phoneNumber;
    private Enums.Role role;
    private boolean active;
    private boolean emailVerified;
}
