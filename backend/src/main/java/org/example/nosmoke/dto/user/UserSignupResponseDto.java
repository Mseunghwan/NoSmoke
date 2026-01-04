package org.example.nosmoke.dto.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class UserSignupResponseDto {
    private final Long id;
    private final String name;
    private final String email;
    private final int point;
    private final LocalDateTime createdAt;
}
