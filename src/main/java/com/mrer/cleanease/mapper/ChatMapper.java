package com.mrer.cleanease.mapper;

import com.mrer.cleanease.dto.request.ChatMessageRequest;
import com.mrer.cleanease.dto.response.ChatMessageResponse;
import com.mrer.cleanease.entity.ChatMessage;
import com.mrer.cleanease.entity.User;
import org.springframework.stereotype.Component;

@Component
public class ChatMapper {
    public ChatMessage toEntity(ChatMessageRequest dto, User user){
        if (dto == null) return null;

        ChatMessage msg = new ChatMessage();
        msg.setSenderType(dto.getSenderType());
        msg.setUser(user);
        msg.setContent(dto.getContent());
        msg.setTimestamp(dto.getTimeStamp());
        return msg;
    }

    public ChatMessageResponse toResponse(ChatMessage entity){
        if (entity == null) return null;
        return ChatMessageResponse.builder()
                .id(entity.getId())
                .senderType(entity.getSenderType())
                .content(entity.getContent())
                .timestamp(entity.getTimestamp())
                .build();
    }
}
