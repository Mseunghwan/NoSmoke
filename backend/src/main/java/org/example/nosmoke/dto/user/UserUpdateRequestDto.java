package org.example.nosmoke.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserUpdateRequestDto {
    @NotBlank(message = "이름은 비어있을 수 없습니다.")
    @Size(max = 100, message = "이름은 최대 100자 이내여야 합니다.")
    private final String name;

    @NotBlank(message = "이메일은 비어있을 수 없습니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private final String email;

    // 프로필 수정 시에는 비밀번호 변경 제외 (별도 API로 분리)
}
