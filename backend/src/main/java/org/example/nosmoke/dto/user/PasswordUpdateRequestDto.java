package org.example.nosmoke.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PasswordUpdateRequestDto {

    @NotBlank(message = "현재 암호란은 비어있을 수 없습니다.")
    private final String currentPassword;


    @NotBlank(message = "새로운 암호란은 비어있을 수 없습니다.")
    private final String newPassword;

    @NotBlank(message = "새로운 암호 확인란은 비어있을 수 없습니다.")
    private final String newPasswordCheck;
}
