package com.mrer.cleanease.dto.request;

import com.mrer.cleanease.entity.Enums;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageRequest {

    @NotBlank
    private String content;

    @NotNull
    private Long userId;

    private LocalDateTime timeStamp;

    private Enums.SenderType senderType;
}
