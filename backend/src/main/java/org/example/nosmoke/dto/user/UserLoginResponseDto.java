package org.example.nosmoke.dto.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserLoginResponseDto {
    private final String accessToken;
    private final String refreshToken;
    private final Long id;
    private final String name;
    private final String email;
    private final int point;
    private final boolean hasSmokingInfo;
}
