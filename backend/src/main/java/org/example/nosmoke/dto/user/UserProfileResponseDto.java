package org.example.nosmoke.dto.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class UserProfileResponseDto {
    private final Long id;
    private final String name;
    private final String email;
    private final int point;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;
}
// 사용자 프로필 조회 시 사용하는 DTO