package org.example.nosmoke.dto.dashboard;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DashboardResponseDto {
    private final long quitDays; // 금연 일수
    private final long savedMoney; // 절약한 돈
    private final long cigarettesNotSmoked; // 피우지 않은 담배 개수
    private final List<String> healthImprovements; // 건강 개선 효과
    private final int currentStreak; // 현재 연속 성공일
    private final int longestStreak; // 최장 연속 성공일
    private final int points; // 보유 포인트
    private final boolean hasSurveyedToday; // 오늘 설문 완료 여부
}
