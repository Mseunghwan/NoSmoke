package org.example.nosmoke.dto.monkey;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.nosmoke.entity.SmokingInfo;
import org.example.nosmoke.entity.User;

@Getter
@AllArgsConstructor
public class MonkeyChatContextDto {
    private final User user;
    private final SmokingInfo smokingInfo;
}
