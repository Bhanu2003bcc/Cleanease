package com.mrer.cleanease.dto.response;

import com.mrer.cleanease.entity.Enums;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessageResponse {
    private Long id;
    private String content;
    private Enums.SenderType senderType;
    private LocalDateTime timestamp;
}
