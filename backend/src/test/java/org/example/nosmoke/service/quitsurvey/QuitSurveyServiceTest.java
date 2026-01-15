package org.example.nosmoke.service.quitsurvey;

import org.example.nosmoke.dto.quitsurvey.QuitSurveyRequestDto;
import org.example.nosmoke.entity.QuitSurvey;
import org.example.nosmoke.entity.User;
import org.example.nosmoke.repository.QuitSurveyRepository;
import org.example.nosmoke.repository.UserRepository;
import org.example.nosmoke.service.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class QuitSurveyServiceTest {

    @InjectMocks
    private QuitSurveyService quitSurveyService;

    @Mock
    private QuitSurveyRepository quitSurveyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService; // 포인트 지급 확인용

    // 1. 설문 저장 (Save)

    @Test
    @DisplayName("설문 저장 성공 - 금연 성공 시 포인트 지급 확인")
    void 설문_저장_성공_포인트지급() {
        // given
        Long userId = 1L;
        // 금연 성공으로 설정
        QuitSurveyRequestDto request = new QuitSurveyRequestDto(
                true, 5, "스트레스 원인", 3, "메모"
        );

        given(userRepository.findById(userId)).willReturn(Optional.of(new User()));

        // 저장 시 Entity 반환 설정
        given(quitSurveyRepository.save(any(QuitSurvey.class))).willAnswer(invocation -> {
            return (QuitSurvey) invocation.getArgument(0);
        });

        // when
        QuitSurvey result = quitSurveyService.saveSurvey(userId, request);

        // then
        assertThat(result.isSuccess()).isTrue();

        // [중요] 금연 성공했으므로 addPoints(userId, 10)이 한 번 호출되어야 함
        verify(userService).addPoints(eq(userId), eq(10));
    }

    @Test
    @DisplayName("설문 저장 성공 - 금연 실패 시 포인트 미지급 확인")
    void 설문_저장_성공_포인트미지급() {
        // given
        Long userId = 1L;
        // 금연 실패(false)
        QuitSurveyRequestDto request = new QuitSurveyRequestDto(
                false, 9, "회식", 8, "힘들었다"
        );

        given(userRepository.findById(userId)).willReturn(Optional.of(new User()));
        given(quitSurveyRepository.save(any(QuitSurvey.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        QuitSurvey result = quitSurveyService.saveSurvey(userId, request);

        // then
        assertThat(result.isSuccess()).isFalse();

        // 실패했으므로 addPoints는 절대로 호출되면 안 됨 --> never()
        verify(userService, never()).addPoints(any(), any(Integer.class));
    }

    @Test
    @DisplayName("설문 저장 실패 - 존재하지 않는 유저")
    void 설문_저장_실패_유저없음() {
        // given
        Long userId = 999L;
        QuitSurveyRequestDto request = new QuitSurveyRequestDto(true, 1, "", 1, "");

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> quitSurveyService.saveSurvey(userId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("해당 사용자를 찾을 수 없습니다");
    }

    // 2. 설문 목록 조회

    @Test
    @DisplayName("설문 목록 조회 성공")
    void 설문_목록_조회_성공() {
        // given
        Long userId = 1L;
        QuitSurvey survey1 = new QuitSurvey(userId, true, 1, "", 1, "");
        QuitSurvey survey2 = new QuitSurvey(userId, false, 5, "", 5, "");

        given(userRepository.findById(userId)).willReturn(Optional.of(new User()));
        given(quitSurveyRepository.findByUserId(userId)).willReturn(List.of(survey1, survey2));

        // when
        List<QuitSurvey> result = quitSurveyService.findSurveyByUserId(userId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).isSuccess()).isTrue();
    }

    @Test
    @DisplayName("설문 조회 실패 - 유저 없음")
    void 설문_조회_실패_유저없음() {
        // given
        Long userId = 999L;
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> quitSurveyService.findSurveyByUserId(userId))
                .isInstanceOf(IllegalArgumentException.class);
    }
}