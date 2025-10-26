package com.mrer.cleanease.dto.request;

import com.mrer.cleanease.entity.Enums;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateRoleRequest {
    @NotNull(message = "Role must not be null")
    private Enums.Role role;
}
