package org.example.nosmoke.service.monkey;

import org.example.nosmoke.dto.monkey.MonkeyChatContextDto;
import org.example.nosmoke.entity.MonkeyMessage;
import org.example.nosmoke.entity.SmokingInfo;
import org.example.nosmoke.entity.User;
import org.example.nosmoke.repository.MonkeyMessageRepository;
import org.example.nosmoke.repository.SmokingInfoRepository;
import org.example.nosmoke.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MonkeyServiceTest {

    @InjectMocks
    private MonkeyService monkeyService;

    @Mock private MonkeyMessageRepository monkeyMessageRepository;
    @Mock private UserRepository userRepository;
    @Mock private SmokingInfoRepository smokingInfoRepository;

    // 1. 채팅 컨텍스트 조회 (User + SmokingInfo)
    @Test
    @DisplayName("채팅 컨텍스트 조회 성공")
    void getChatContext_성공() {
        // given
        Long userId = 1L;
        User user = new User("홍길동", "test@email.com", "pw", 0);
        SmokingInfo smokingInfo = new SmokingInfo(userId, "담배", 10, LocalDate.now(), LocalDate.now(), "목표");

        // UserRepository.getByIdOrThrow 가 User를 반환한다고 가정
        given(userRepository.getByIdOrThrow(userId)).willReturn(user);
        // SmokingInfoRepository.getByUserIdOrNull 가 SmokingInfo를 반환한다고 가정
        given(smokingInfoRepository.getByUserIdOrNull(userId)).willReturn(smokingInfo);

        // when
        MonkeyChatContextDto result = monkeyService.getChatContext(userId);

        // then
        assertThat(result.getUser().getName()).isEqualTo("홍길동");
        assertThat(result.getSmokingInfo()).isNotNull();
    }

    // 2. 프롬프트 생성 로직 검증 (문자열 포맷팅)
    @Test
    @DisplayName("페르소나(원숭이) 프롬프트 생성 성공")
    void createPersonPrompt_검증() {
        // given
        String userMessage = "담배 피우고 싶어";
        User user = new User("홍길동", "a@a.com", "pw", 0);

        // 금연 5일차 설정 (오늘 - 5일)
        SmokingInfo smokingInfo = new SmokingInfo(1L, "Type", 10, LocalDate.now().minusDays(5), LocalDate.now(), "Goal");
        MonkeyChatContextDto context = new MonkeyChatContextDto(user, smokingInfo);

        // when
        String prompt = monkeyService.createPersonPrompt(context, userMessage);

        // then
        // 프롬프트 안에 핵심 정보가 포함되어 있는지 확인
        assertThat(prompt).contains("홍길동"); // 이름 (User 객체에 따라 다를 수 있음, 없으면 제외)
        assertThat(prompt).contains("5일째 금연 중"); // 금연 일수
        assertThat(prompt).contains(userMessage); // 사용자 메시지
        assertThat(prompt).contains("원숭이 '스털링'"); // 페르소나 확인
    }

    @Test
    @DisplayName("건강 분석 프롬프트 생성 성공")
    void createHealthAnalysisPrompt_검증() {
        // given
        User user = new User("홍길동", "a@a.com", "pw", 0);
        // 금연 10일차
        SmokingInfo smokingInfo = new SmokingInfo(1L, "Type", 10, LocalDate.now().minusDays(10), LocalDate.now(), "Goal");
        MonkeyChatContextDto context = new MonkeyChatContextDto(user, smokingInfo);

        // when
        String prompt = monkeyService.createHealthAnalysisPrompt(context);

        // then
        assertThat(prompt).contains("금연 10일차");
        assertThat(prompt).contains("전문 의사입니다");
    }

    // 3. 메시지 저장 (Save)
    @Test
    @DisplayName("메시지 저장 성공")
    void saveMessage_성공() {
        // given
        Long userId = 1L;
        String content = "안녕하세요";
        MonkeyMessage.MessageType type = MonkeyMessage.MessageType.USER;
        User user = new User("홍길동", "test@email.com", "pw", 0);

        // getReferenceById는 프록시 객체를 반환하지만, Mockito에선 그냥 User 객체를 줘도 무방
        given(userRepository.getReferenceById(userId)).willReturn(user);

        // when
        MonkeyMessage savedMessage = monkeyService.saveMessage(userId, content, type);

        // then
        // 반환된 객체 검증
        assertThat(savedMessage.getContent()).isEqualTo(content);
        assertThat(savedMessage.getMessageType()).isEqualTo(type);

        // 레포지토리 save 호출 검증
        verify(monkeyMessageRepository).save(any(MonkeyMessage.class));
    }

    // 4. 메시지 조회 (Slice Paging)
    @Test
    @DisplayName("메시지 목록 조회 성공")
    void findMessagesByUserId_성공() {
        // given
        Long userId = 1L;
        int page = 0;
        int size = 10;

        // 가짜 메시지 리스트 생성
        List<MonkeyMessage> messages = List.of(
                MonkeyMessage.builder().content("msg1").messageType(MonkeyMessage.MessageType.USER).build(),
                MonkeyMessage.builder().content("msg2").messageType(MonkeyMessage.MessageType.REACTIVE).build()
        );
        // Slice 구현체 생성
        Slice<MonkeyMessage> messageSlice = new SliceImpl<>(messages);

        given(monkeyMessageRepository.findByUser_IdOrderByCreatedAtDesc(eq(userId), any(Pageable.class)))
                .willReturn(messageSlice);

        // when
        Slice<MonkeyMessage> result = monkeyService.findMessagesByUserId(userId, page, size);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getContent()).isEqualTo("msg1");

        // 올바른 페이지 요청이 갔는지 검증
        verify(monkeyMessageRepository).findByUser_IdOrderByCreatedAtDesc(eq(userId), eq(PageRequest.of(page, size)));
    }
}