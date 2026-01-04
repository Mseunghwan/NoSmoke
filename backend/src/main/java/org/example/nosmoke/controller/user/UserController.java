package org.example.nosmoke.controller.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.nosmoke.dto.ApiResponse;
import org.example.nosmoke.dto.user.*;
import org.example.nosmoke.service.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserSignupResponseDto>> signup(
            @Valid @RequestBody UserSignupRequestDto requestDto
    ) {

        try {
            // 회원가입 시도
            UserSignupResponseDto responseDto = userService.signup(requestDto);

            // 예외 반환 안되면 아래처럼 ApiResponse 생성되어 ResponseEntity에 얹혀 리턴될 것
            ApiResponse<UserSignupResponseDto> response = ApiResponse.success(
                    "회원가입이 완료되었습니다.",
                    responseDto
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            // 예외 발생 시
            ApiResponse<UserSignupResponseDto> response = ApiResponse.error(
                    "SIGNUP_ERROR",
                    e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserLoginResponseDto>> login(
            @Valid @RequestBody UserLoginRequestDto requestDto) {

        try{
            UserLoginResponseDto responseDto = userService.login(requestDto);
            ApiResponse<UserLoginResponseDto> response = ApiResponse.success(
                    "로그인이 완료되었습니다",
                    responseDto
            );

            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (IllegalArgumentException e ){
            ApiResponse<UserLoginResponseDto> response = ApiResponse.error(
                    "LOGIN_ERROR",
                    e.getMessage()
            );

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    // 사용자 프로필 조회
    @GetMapping("/profile/{userId}")
    public ResponseEntity<ApiResponse<UserProfileResponseDto>> getProfile(
            @PathVariable Long userId
    ){

        try{
            UserProfileResponseDto responseDto = userService.getProfile(userId);
            ApiResponse<UserProfileResponseDto> response = ApiResponse.success(
                    "사용자 정보 조회 완료",
                    responseDto
            );
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (IllegalArgumentException e) {
            ApiResponse<UserProfileResponseDto> response = ApiResponse.error(
                    "USER_NOT_FOUND",
                    e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    // 사용자 정보 수정
    @PutMapping("/profile/{userId}")
    public ResponseEntity<ApiResponse<UserUpdateResponseDto>> updateProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequestDto requestDto) {

        try{
            UserUpdateResponseDto responseDto = userService.updateProfile(userId, requestDto);

            ApiResponse<UserUpdateResponseDto> response = ApiResponse.success(
                    "사용자 정보 수정 완료",
                    responseDto
            );

            return ResponseEntity.ok(response);

        }
        catch (IllegalArgumentException e) {
            ApiResponse<UserUpdateResponseDto> response = ApiResponse.error(
                    "UPDATE_ERROR",
                    e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // 이메일 중복 확인
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmailDuplicate(
            @RequestParam String email
    ){

        boolean isDuplicate = userService.existsByEmail(email);

        ApiResponse<Boolean> response = ApiResponse.success(
                "이메일 중복 확인 완료",
                isDuplicate
        );
        return ResponseEntity.ok(response);

    }


}
