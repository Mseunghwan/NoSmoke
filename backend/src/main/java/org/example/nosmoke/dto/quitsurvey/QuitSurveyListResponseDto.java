package org.example.nosmoke.dto.quitsurvey;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.nosmoke.entity.QuitSurvey;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public class QuitSurveyListResponseDto {
    private final List<QuitSurveyResponseDto> surveys;
    private final int totalSurveys; // 설문 전체 수
    private final int successDays; // 성공 일수
    private final double successRate;  // 성공률
    private final double averageStressLevel; // 평균 스트레스 레벨
    private final double averageCravingLevel; // 평균 충동 레벨

    // 생성자 호출 시 통계 계산
    public QuitSurveyListResponseDto(List<QuitSurvey> surveyEntities) {
        if(surveyEntities == null || surveyEntities.isEmpty()) {
            this.surveys = Collections.emptyList();
            this.totalSurveys = 0;
            this.successDays = 0;
            this.successRate = 0.0;
            this.averageStressLevel = 0.0;
            this.averageCravingLevel = 0.0;
            return;
        }

        // 엔터티 리스트를 DTO 리스트로 변환
        this.surveys = surveyEntities.stream().map(QuitSurveyResponseDto::new).collect(Collectors.toList());
        // 설문 수 계산
        this.totalSurveys = surveys.size();
        // 금연 성공 일 수 계산
        this.successDays = (int) surveyEntities.stream()
                .filter(QuitSurvey::isSuccess)
                .count();

        // 성공률 계산 --> 0 나눔 방지
        this.successRate = (this.totalSurveys > 0)
                ? ((double) this.successDays / this.totalSurveys) * 100.0
                : 0.0;

        // 스트레스 수준 계산
        this.averageStressLevel = surveyEntities.stream()
                .mapToInt(QuitSurvey::getStressLevel)
                .average()
                .orElse(0.0);

        // 평균 흡연 충동 수준
        this.averageCravingLevel = surveyEntities.stream()
                .mapToInt(QuitSurvey::getCravingLevel)
                .average()
                .orElse(0.0);
    }
}
