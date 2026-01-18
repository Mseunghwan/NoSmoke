package org.example.nosmoke.controller.smokinginfo;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.example.nosmoke.dto.smokinginfo.SmokingInfoRequestDto;
import org.example.nosmoke.entity.SmokingInfo;
import org.example.nosmoke.entity.User;
import org.example.nosmoke.repository.SmokingInfoRepository;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.data.redis.repositories.enabled=false")
@AutoConfigureMockMvc
@Transactional
class SmokingInfoApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SmokingInfoRepository smokingInfoRepository;

    @MockitoBean
    private RedisTemplate<String, Object> redisTemplate;

    @MockitoBean
    private RedisConnectionFactory redisConnectionFactory;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Redis Mock 설정
        ValueOperations<String, Object> valueOperations = mock(ValueOperations.class);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        // 테스트용 유저 생성 (로그인 된 상태를 흉내내기 위해 필요)
        testUser = userRepository.save(new User("스모킹테스터", "smoke@test.com", "password123", 0));
    }

    @Test
    @DisplayName("흡연 정보 등록 API 테스트 - 성공")
    void 흡연정보_등록_성공() throws Exception {
        // given
        SmokingInfoRequestDto requestDto = new SmokingInfoRequestDto(
                "말보로 레드",
                20,
                LocalDate.now(), // 금연 시작일
                LocalDate.now().plusDays(100), // 목표 날짜
                "건강을 위해 금연하자"
        );

        // when
        // .with(user(String.valueOf(testUser.getId()))) : 인증된 사용자(Principal)로 요청을 보냄
        ResultActions result = mockMvc.perform(post("/api/smoking-info")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .with(user(String.valueOf(testUser.getId()))));

        // then
        result.andExpect(status().isCreated())
                .andDo(print());

        // DB 검증
        SmokingInfo savedInfo = smokingInfoRepository.findByUserId(testUser.getId()).orElseThrow();
        assertThat(savedInfo.getCigaretteType()).isEqualTo("말보로 레드");
        assertThat(savedInfo.getDailyConsumption()).isEqualTo(20);
    }

    @Test
    @DisplayName("흡연 정보 조회 API 테스트 - 성공")
    void 흡연정보_조회_성공() throws Exception {
        // given
        // 미리 데이터 저장
        SmokingInfo info = new SmokingInfo(
                testUser.getId(),
                "에쎄 체인지",
                10,
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2023, 12, 31),
                "조회 테스트 목표"
        );
        smokingInfoRepository.save(info);

        // when
        ResultActions result = mockMvc.perform(get("/api/smoking-info")
                .with(user(String.valueOf(testUser.getId()))));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.cigaretteType").value("에쎄 체인지"))
                .andExpect(jsonPath("$.data.dailyConsumption").value(10))
                .andExpect(jsonPath("$.data.quitGoal").value("조회 테스트 목표"))
                .andDo(print());
    }

    @Test
    @DisplayName("흡연 정보 수정 API 테스트 - 성공")
    void 흡연정보_수정_성공() throws Exception {
        // given
        // 1. 기존 데이터 저장
        SmokingInfo info = new SmokingInfo(
                testUser.getId(),
                "기존 담배",
                5,
                LocalDate.now(),
                LocalDate.now().plusDays(50),
                "기존 목표"
        );
        smokingInfoRepository.save(info);

        // 2. 수정할 데이터
        SmokingInfoRequestDto updateDto = new SmokingInfoRequestDto(
                "바뀐 담배",
                15, // 소비량 변경
                LocalDate.now(), // 금연 시작일 (보통 수정 시엔 무시되거나 그대로 유지됨, 로직에 따라 다름)
                LocalDate.now().plusDays(200), // 목표 날짜 변경
                "목표도 수정됨"
        );

        // when
        ResultActions result = mockMvc.perform(put("/api/smoking-info")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto))
                .with(user(String.valueOf(testUser.getId()))));

        // then
        result.andExpect(status().isOk())
                .andDo(print());

        // DB 변경 확인
        SmokingInfo updatedInfo = smokingInfoRepository.findByUserId(testUser.getId()).orElseThrow();
        assertThat(updatedInfo.getCigaretteType()).isEqualTo("바뀐 담배");
        assertThat(updatedInfo.getQuitGoal()).isEqualTo("목표도 수정됨");
        assertThat(updatedInfo.getDailyConsumption()).isEqualTo(15);
    }
}