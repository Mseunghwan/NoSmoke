package org.example.nosmoke.controller.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.example.nosmoke.dto.user.UserLoginRequestDto;
import org.example.nosmoke.dto.user.UserSignupRequestDto;
import org.example.nosmoke.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.data.redis.repositories.enabled=false") // Redis Repository 자동설정 끄기
@AutoConfigureMockMvc
@Transactional // 테스트 끝나면 db 롤백
public class UserApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private RedisTemplate<String, Object> redisTemplate;

    @MockitoBean
    private RedisConnectionFactory redisConnectionFactory;

    @BeforeEach
    void setUp() {
        ValueOperations<String, Object> valueOperations = mock(ValueOperations.class);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
    }

    @Test
    @DisplayName("회원가입 API 통합 테스트 - 성공")
    void 회원가입_통합테스트_성공() throws Exception {
        // given
        UserSignupRequestDto request = new UserSignupRequestDto(
                "통합테스터",
                "integration@test.com",
                "password123"
        );

        // when
        ResultActions result = mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isCreated()) // 201 Created 확인
                .andDo(print()); // 로그 출력

        // DB에 진짜 들어갔는지 확인
        assertThat(userRepository.existsByEmail("integration@test.com")).isTrue();
    }

    @Test
    @DisplayName("로그인 API 통합 테스트 - 성공 (토큰 발급 확인)")
    void 로그인_통합테스트_성공() throws Exception {
        // given
        // 1. 회원가입 먼저 시켜두기 (DB에 데이터가 있어야 로그인하니까)
        UserSignupRequestDto signupRequest = new UserSignupRequestDto(
                "로그인테스터",
                "login@test.com",
                "password123"
        );
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)));

        // 2. 로그인 요청 객체 생성
        UserLoginRequestDto loginRequest = new UserLoginRequestDto(
                "login@test.com",
                "password123"
        );

        // when
        ResultActions result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists()) // 토큰이 발급되었는지 확인
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andDo(print());
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 틀림")
    void 로그인_실패_비밀번호() throws Exception {
        // given
        // 회원가입
        UserSignupRequestDto signupRequest = new UserSignupRequestDto(
                "실패테스터",
                "fail@test.com",
                "password123"
        );
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)));

        // 틀린 비밀번호로 로그인 요청
        UserLoginRequestDto loginRequest = new UserLoginRequestDto(
                "fail@test.com",
                "wrongPassword"
        );

        // when
        ResultActions result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)));

        // then
        result.andExpect(status().isUnauthorized()) // 401 Unauthorized 확인
                .andExpect(jsonPath("$.message").value("비밀번호가 틀렸습니다."))
                .andDo(print());
    }
}
