package org.example.nosmoke.service.dashboard;

import org.example.nosmoke.dto.dashboard.DashboardResponseDto;
import org.example.nosmoke.dto.quitsurvey.QuitSurveyLightDto;
import org.example.nosmoke.entity.SmokingInfo;
import org.example.nosmoke.entity.User;
import org.example.nosmoke.repository.QuitSurveyRepository;
import org.example.nosmoke.repository.SmokingInfoRepository;
import org.example.nosmoke.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @InjectMocks
    private DashboardService dashboardService;

    @Mock private UserRepository userRepository;
    @Mock private SmokingInfoRepository smokingInfoRepository;
    @Mock private QuitSurveyRepository quitSurveyRepository;

    // 1. 대시보드 조회

    @Test
    @DisplayName("대시보드 조회 성공 - 모든 데이터가 있을 때")
    void 대시보드_조회_성공() {
        // given
        Long userId = 1L;
        LocalDate today = LocalDate.now();

        // 유저 정보 (포인트 500점)
        User user = new User("홍길동", "test@email.com", "pw", 500);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // 흡연 정보 (10일 전 금연 시작, 하루 10개비)
        // 10일간 안 피움 -> 100개비 절약
        // 절약 금액 = (100개 * 4500원) / 20개 = 22,500원
        SmokingInfo smokingInfo = new SmokingInfo(userId, "담배", 10, today.minusDays(10), today.plusDays(100), "목표");
        given(smokingInfoRepository.findByUserId(userId)).willReturn(Optional.of(smokingInfo));

        // 설문 기록 (최신순 정렬 가정: 오늘(성공) -> 어제(성공) -> 2일전(실패))
        // 스트릭(연속 성공)은 2일이어야 함
        List<QuitSurveyLightDto> surveys = List.of(
                new QuitSurveyLightDto(true, LocalDateTime.now()),               // 오늘 (성공)
                new QuitSurveyLightDto(true, LocalDateTime.now().minusDays(1)),  // 어제 (성공)
                new QuitSurveyLightDto(false, LocalDateTime.now().minusDays(2))  // 2일전 (실패) -> 여기서 끊김
        );
        given(quitSurveyRepository.findAllLightByUserId(eq(userId), any(Pageable.class)))
                .willReturn(surveys);

        // when
        DashboardResponseDto result = dashboardService.getDashboardInfo(userId);

        // then
        // 계산 검증
        assertThat(result.getQuitDays()).isEqualTo(10L);
        assertThat(result.getCigarettesNotSmoked()).isEqualTo(100L); // 10일 * 10개비
        assertThat(result.getSavedMoney()).isEqualTo(22500L); // (100 * 4500) / 20

        // 스트릭 검증 (오늘, 어제 성공 = 2)
        assertThat(result.getCurrentStreak()).isEqualTo(2);

        // 오늘 설문 여부 (오늘 날짜 데이터가 있으므로 true)
        assertThat(result.isHasSurveyedToday()).isTrue();

        // 유저 포인트 확인
        assertThat(result.getPoints()).isEqualTo(500);
    }

    @Test
    @DisplayName("대시보드 조회 - 흡연 정보가 없을 때 (초기 상태)")
    void 대시보드_조회_흡연정보_없음() {
        // given
        Long userId = 1L;
        User user = new User("신입", "new@email.com", "pw", 0);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(smokingInfoRepository.findByUserId(userId)).willReturn(Optional.empty()); // 정보 없음
        given(quitSurveyRepository.findAllLightByUserId(eq(userId), any(Pageable.class)))
                .willReturn(List.of()); // 설문도 없음

        // when
        DashboardResponseDto result = dashboardService.getDashboardInfo(userId);

        // then
        // 모든 수치가 0으로 초기화되어야 함
        assertThat(result.getQuitDays()).isEqualTo(0);
        assertThat(result.getSavedMoney()).isEqualTo(0);
        assertThat(result.getCurrentStreak()).isEqualTo(0);
        assertThat(result.isHasSurveyedToday()).isFalse();
    }

    @Test
    @DisplayName("대시보드 조회 실패 - 존재하지 않는 유저")
    void 대시보드_조회_실패_유저없음() {
        // given
        Long userId = 999L;
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> dashboardService.getDashboardInfo(userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 사용자입니다.");
    }
}