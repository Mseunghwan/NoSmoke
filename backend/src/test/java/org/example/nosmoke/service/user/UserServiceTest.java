package org.example.nosmoke.service.user;

import org.example.nosmoke.dto.token.TokenDto;
import org.example.nosmoke.dto.user.UserLoginRequestDto;
import org.example.nosmoke.dto.user.UserLoginResponseDto;
import org.example.nosmoke.dto.user.UserSignupRequestDto;
import org.example.nosmoke.dto.user.UserSignupResponseDto;
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

    // 로그인 테스트

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



}
