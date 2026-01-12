package org.example.nosmoke.controller.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.nosmoke.dto.ApiResponse;
import org.example.nosmoke.dto.token.TokenDto;
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
    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserLoginResponseDto>> login(
            @Valid @RequestBody UserLoginRequestDto requestDto) {

        try {
            UserLoginResponseDto responseDto = userService.login(requestDto);
            ApiResponse<UserLoginResponseDto> response = ApiResponse.success(
                    "로그인이 완료되었습니다",
                    responseDto
            );
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (IllegalArgumentException e) {
            ApiResponse<UserLoginResponseDto> response = ApiResponse.error(
                    "LOGIN_ERROR",
                    e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);

        } catch (Exception e) {
            // [추가] Redis 연결 실패 등 예상치 못한 시스템 에러 처리
            e.printStackTrace(); // 서버 로그에 상세 에러 출력
            ApiResponse<UserLoginResponseDto> response = ApiResponse.error(
                    "SYSTEM_ERROR",
                    "시스템 오류가 발생했습니다: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenDto>> reissue(
            @RequestHeader("Authorization") String accessToken,
            @RequestHeader("RefreshToken") String refreshToken
    ){
        // Bearer 제거
        String resolvedAccessToken = resolveToken(accessToken);

        try{
            TokenDto tokenDto = userService.reissue(resolvedAccessToken, refreshToken);
            return ResponseEntity.ok(ApiResponse.success("토큰 재발급 완료", tokenDto));
        } catch(IllegalArgumentException e ){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("REISSUE_FAILED", e.getMessage()));
        }
    }

    // 로그아웃 API
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            @RequestHeader("Authorization") String accessToken
    ){
        String resolvedAccessToken = resolveToken(accessToken);

        try{
            userService.logout(resolvedAccessToken);
            return ResponseEntity.ok(ApiResponse.success("로그아웃 완료", "LOGOUT_SUCCESS"));
        } catch(IllegalArgumentException e ){
          return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                  .body(ApiResponse.error("LOGOUT_FAILED", e.getMessage()));
        }
    }

    private String resolveToken(String bearerToken) {
        if(bearerToken.startsWith("Bearer ")) {
           return bearerToken.substring(7);
        }
        return bearerToken;
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

    // 사용자 명 수정
    @PutMapping("/profile/{userId}")
    public ResponseEntity<ApiResponse<UserUpdateResponseDto>> updateProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UserNameUpdateRequestDto requestDto) {

        try{
            UserUpdateResponseDto responseDto = userService.updateNameProfile(userId, requestDto);

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

    @PutMapping("/password/{userId}")
    public ResponseEntity<ApiResponse<String>> updatePassword(
            @PathVariable Long userId,
            @Valid @RequestBody PasswordUpdateRequestDto requestDto
    ){
        try{
            userService.updatePassword(userId, requestDto);

            ApiResponse<String> response = ApiResponse.success(
                    "비밀번호 변경이 완료되었습니다.",
                    "SUCCESS"
            );
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            ApiResponse<String> response = ApiResponse.error(
                    "PASSWORD_UPDATE_ERROR",
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
