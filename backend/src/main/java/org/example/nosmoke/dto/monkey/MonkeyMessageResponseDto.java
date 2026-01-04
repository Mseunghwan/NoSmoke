package org.example.nosmoke.dto.monkey;

import lombok.Getter;
import org.example.nosmoke.entity.MonkeyMessage;

import java.time.LocalDateTime;

@Getter
public class MonkeyMessageResponseDto {

    private final Long messageId;
    private final String content;
    private final String messageType;
    private final LocalDateTime createdAt;

    public MonkeyMessageResponseDto(MonkeyMessage monkeyMessage) {
        this.messageId = monkeyMessage.getId();
        this.content = monkeyMessage.getContent();
        this.messageType = monkeyMessage.getMessageType().name();
        this.createdAt = monkeyMessage.getCreatedAt();
    }
}
