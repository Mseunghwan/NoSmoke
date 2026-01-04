package org.example.nosmoke.dto.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserLoginResponseDto {
    private final String token;
    private final Long id;
    private final String name;
    private final String email;
    private final int point;
}
