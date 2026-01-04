package org.example.nosmoke.dto.user;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
// NoArgsConstructor = 매개변수 없는 기본 생성자 생성
// RequiredArgsConstructor = final 필드나, @NonNull 필드만을 매개변수로 받는 생성자 생성
public class UserSignupRequestDto {
    @NotBlank(message = "이름은 비어있을 수 없습니다.")
    @Size(max=100, message = "이름은 최대 100자 이내여야 합니다.")
    private final String name;

    @NotBlank(message = "이메일은 비어있을 수 없습니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private final String email;

    @NotBlank(message = "비밀번호는 비어있을 수 없습니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하여야 합니다.")
    private final String password;

    // 위와 같은 validation 들은 controller에서 매개변수를 받아올 때 @Valid 어노테이션을 붙여줘야 사용가능하다

}
