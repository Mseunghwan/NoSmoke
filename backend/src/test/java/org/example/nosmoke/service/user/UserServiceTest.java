package org.example.nosmoke.service.user;

import org.example.nosmoke.dto.token.TokenDto;
import org.example.nosmoke.dto.user.*;
import org.example.nosmoke.entity.User;
import org.example.nosmoke.repository.SmokingInfoRepository;
import org.example.nosmoke.repository.UserRepository;
import org.example.nosmoke.util.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class) // Mockito 사용 선언
public class UserServiceTest {

    @InjectMocks // 가짜(Mock) 객체들을 주입받아 테스트 할 진짜 서비스
    private UserService userService;


    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private SmokingInfoRepository smokingInfoRepository;
    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private ValueOperations<String, Object> valueOperations;


    // 1. 회원가입 테스트

    @Test
    @DisplayName("회원가입 성공")
    void 회원가입_성공(){
        // given
        UserSignupRequestDto request = new UserSignupRequestDto("홍길동", "test@gmail.com", "password123");

            // 이메일 중복 체크 통과 가정
        given(userRepository.existsByEmail(request.getEmail())).willReturn(false);
            // 비밀번호 암호화 동작 가정
        given(passwordEncoder.encode(request.getPassword())).willReturn("encodedPassword");
            // DB 저장 시, 저장된 User 객체 리턴 가정
        given(userRepository.save(any(User.class))).willAnswer(invocation -> {
            User user = (User) invocation.getArgument(0);

            return new User(user.getName(), user.getEmail(), user.getPassword(), 0) {
                @Override
                public Long getId(){ return 1L; }
            };
        });

        // when
        UserSignupResponseDto response = userService.signup(request);

        // then
        assertThat(response.getName()).isEqualTo("홍길동");
        assertThat(response.getEmail()).isEqualTo("test@gmail.com");
        verify(userRepository).save(any(User.class)); // save가 호출됐는지 확인

    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void 이메일_중복_회원가입_실패(){
        // given
        UserSignupRequestDto request = new UserSignupRequestDto("홍길동", "duplicate@email.com", "pw");
            // 이미 존재하는 이메일이라고 가정
        given(userRepository.existsByEmail(request.getEmail())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.signup(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 가입된 이메일 입니다.");
    }

    // 2. 로그인 테스트

    @Test
    @DisplayName("로그인 성공")
    void 로그인_성공(){
        // given
        UserLoginRequestDto request = new UserLoginRequestDto("test@email.com", "password123");
        User user = new User("홍길동", "test@email.com", "encodedPw", 0) {
            @Override public Long getId() { return 1L; }
        };

            // 1. 유저 조회 성공 가정
        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));
            // 2. 비밀번호 일치 가정
        given(passwordEncoder.matches(request.getPassword(), user.getPassword())).willReturn(true);
            // 3. 토큰 생성 가정
        TokenDto fakeToken = TokenDto.builder().accessToken("access").refreshToken("refresh").build();
        given(jwtTokenProvider.createTokenDto(any(), any())).willReturn(fakeToken);
            // 4. Redis 저장 로직
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        // when
        UserLoginResponseDto response = userService.login(request);

        // then
        assertThat(response.getEmail()).isEqualTo("test@email.com");
        assertThat(response.getAccessToken()).isEqualTo("access");

        // Redis에 토큰 저장 로직이 호출되었는지 검증
        verify(valueOperations).set(eq("RT:" + user.getEmail()), eq("refresh"), anyLong(), any());

    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 이메일")
    void 로그인_실패_존재하지_않는_이메일(){
        // given
        UserLoginRequestDto request = new UserLoginRequestDto("unknown@gmail.com", "pw");
        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 사용자입니다.");
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void 로그인_실패_비밀번호_불일치(){
        // given
        UserLoginRequestDto request = new UserLoginRequestDto("test@email.com", "wrongPw");
        User user = new User("홍길동", "test@email.com", "encodedPw", 0);

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));
        // 비밀번호가 다르다고 가정(matches -> false)
        given(passwordEncoder.matches(request.getPassword(), user.getPassword())).willReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호가 틀렸습니다.");
    }

    // 3. 사용자 이름 수정 테스트

    @Test
    @DisplayName("닉네임 변경 성공")
    void 닉네임_변경_성공(){
        // given
        Long userId = 1L;

        UserNameUpdateRequestDto request = new UserNameUpdateRequestDto("새로운이름");

        User user = new User("기존이름", "test@email.com", "pw", 0){
            @Override public Long getId() { return 1L; }
        };

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // 중복된 이름이 없다고 가정
        given(userRepository.existsByName("새로운이름")).willReturn(false);
        // save 호출 시 입력된 user 그대로 반환
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        UserUpdateResponseDto response = userService.updateNameProfile(userId, request);

        // then
        assertThat(response.getName()).isEqualTo("새로운이름");
        // 실제로 이름이 바뀌었는지 객체 상태 확인
        assertThat(user.getName()).isEqualTo("새로운이름");

    }

    @Test
    @DisplayName("닉네임 변경 실패 - 이미 존재하는 닉네임")
    void 닉네임_변경_실패_중복(){
        // given
        Long userId = 1L;
        UserNameUpdateRequestDto request = new UserNameUpdateRequestDto("중복이름");

        User user = new User("기존이름", "test@email.com", "pw", 0);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        // 중복이름은 이미 DB에 있다고 가정
        given(userRepository.existsByName("중복이름")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.updateNameProfile(userId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 사용중인 사용자명입니다.");

    }

    // 4. 비밀번호 변경 테스트
    @Test
    @DisplayName("비밀번호 변경 성공")
    void 비밀번호_변경_성공() {
        // given
        Long userId = 1L;

        PasswordUpdateRequestDto request = new PasswordUpdateRequestDto("oldPw", "newPw", "newPw");

        // DB에 저장된 유저는 암호화된 비밀번호("encodedOldPw")를 가지고 있음
        User user = new User("홍길동", "test@email.com", "encodedOldPw", 0);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // 1. 현재 비밀번호 검증 통과 (입력한 oldPw 와 DB의 encodedOldPw 가 일치한다고 가정)
        given(passwordEncoder.matches("oldPw", "encodedOldPw")).willReturn(true);
        // 2. 기존 비밀번호와 달라야 함 (새 비번 newPw 가 기존 encodedOldPw 와 다르다고 가정)
        given(passwordEncoder.matches("newPw", "encodedOldPw")).willReturn(false);
        // 3. 새 비밀번호 암호화 수행
        given(passwordEncoder.encode("newPw")).willReturn("encodedNewPw");

        // when
        userService.updatePassword(userId, request);

        // then
        // 유저 객체의 비밀번호가 암호화된 새 비밀번호로 바뀌었는지 확인
        assertThat(user.getPassword()).isEqualTo("encodedNewPw");
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 현재 비밀번호 불일치")
    void 비밀번호_변경_실패_현재비번_틀림() {
        // given
        Long userId = 1L;
        // 틀린 비밀번호 "wrongPw" 입력
        PasswordUpdateRequestDto request = new PasswordUpdateRequestDto("wrongPw", "newPw", "newPw");

        User user = new User("홍길동", "test@email.com", "encodedOldPw", 0);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        // 비밀번호 불일치 가정 (false)
        given(passwordEncoder.matches("wrongPw", "encodedOldPw")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.updatePassword(userId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("현재 비밀번호가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 새 비밀번호와 확인 비밀번호 불일치")
    void 비밀번호_변경_실패_확인비번_불일치() {
        // given
        Long userId = 1L;
        // 새비번("newPw") != 확인비번("differentPw")
        PasswordUpdateRequestDto request = new PasswordUpdateRequestDto("oldPw", "newPw", "differentPw");

        User user = new User("홍길동", "test@email.com", "encodedOldPw", 0);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        // 현재 비밀번호는 맞다고 통과시킴
        given(passwordEncoder.matches("oldPw", "encodedOldPw")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.updatePassword(userId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
    }


    // 5. 토큰 재발급 테스트
    @Test
    @DisplayName("토큰 재발급 성공")
    void 토큰_재발급_성공() {
        // given
        String accessToken = "oldAccessToken";
        String refreshToken = "validRefreshToken";
        String userId = "1";
        String email = "test@email.com";

        User user = new User("홍길동", email, "pw", 0);

        // Refresh Token 유효성 검증 통과
        given(jwtTokenProvider.validateToken(refreshToken)).willReturn(true);

        // Access Token에서 userPk(ID) 추출
        given(jwtTokenProvider.getUserPk(accessToken)).willReturn(userId);

        // 유저 조회
        given(userRepository.getByIdOrThrow(Long.parseLong(userId))).willReturn(user);

        // Redis에서 저장된 Refresh Token 가져오기
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("RT:" + email)).willReturn(refreshToken); // Redis와 입력값 일치

        // 새 토큰 생성
        TokenDto newToken = TokenDto.builder().accessToken("newAccess").refreshToken("newRefresh").build();
        given(jwtTokenProvider.createTokenDto(userId, email)).willReturn(newToken);

        // when
        TokenDto result = userService.reissue(accessToken, refreshToken);

        // then
        assertThat(result.getAccessToken()).isEqualTo("newAccess");
        assertThat(result.getRefreshToken()).isEqualTo("newRefresh");

        // Redis에 새 토큰이 업데이트되었는지 확인
        verify(valueOperations).set(eq("RT:" + email), eq("newRefresh"), anyLong());
    }

    @Test
    @DisplayName("토큰 재발급 실패 - Refresh Token 불일치 (탈취 의심)")
    void 토큰_재발급_실패_토큰_불일치() {
        // given
        String accessToken = "access";
        String refreshToken = "inputRefresh";
        String userId = "1";
        User user = new User("홍길동", "test@email.com", "pw", 0);

        given(jwtTokenProvider.validateToken(refreshToken)).willReturn(true);
        given(jwtTokenProvider.getUserPk(accessToken)).willReturn(userId);
        given(userRepository.getByIdOrThrow(Long.parseLong(userId))).willReturn(user);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        // Redis에는 다른 토큰이 저장되어 있다고 가정
        given(valueOperations.get("RT:test@email.com")).willReturn("differentRefresh");

        // when & then
        assertThatThrownBy(() -> userService.reissue(accessToken, refreshToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("토큰 정보가 일치하지 않습니다");
    }

    // 6. 로그아웃 테스트

    @Test
    @DisplayName("로그아웃 성공")
    void 로그아웃_성공() {
        // given
        String accessToken = "validAccessToken";
        String userId = "1";
        User user = new User("홍길동", "test@email.com", "pw", 0);

        // 토큰 유효성 검증
        given(jwtTokenProvider.validateToken(accessToken)).willReturn(true);

        // 유저 정보 가져오기
        given(jwtTokenProvider.getUserPk(accessToken)).willReturn(userId);
        given(userRepository.getByIdOrThrow(Long.parseLong(userId))).willReturn(user);

        // Redis 작업 준비
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        // Redis에 RT가 있다고 가정 (삭제 대상)
        given(valueOperations.get("RT:" + user.getEmail())).willReturn("refreshToken");

        // when
        userService.logout(accessToken);

        // then
        // Redis에서 RT 삭제되었는지 검증
        verify(redisTemplate).delete("RT:" + user.getEmail());

        // Access Token이 블랙리스트("logout")로 등록되었는지 검증
        verify(valueOperations).set(eq(accessToken), eq("logout"));
    }

}
