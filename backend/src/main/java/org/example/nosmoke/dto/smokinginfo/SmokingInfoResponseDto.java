package org.example.nosmoke.dto.smokinginfo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.nosmoke.entity.SmokingInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class SmokingInfoResponseDto {
    private final Long id;
    private final Long userId;
    private final String cigaretteType;
    private final int dailyConsumption;
    private final LocalDate quitStartDate;
    private final LocalDate targetDate;
    private final String quitGoal;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;

    // 추가 계산 필드들
    private int quitDays;        // 금연 일수
    private int moneySaved;      // 절약한 돈
    private int cigarettesNotSmoked; // 피우지 않은 담배 개수

    public SmokingInfoResponseDto(SmokingInfo savedInfo) {
        this.id = savedInfo.getId();
        this.userId = savedInfo.getUserId();
        this.cigaretteType = savedInfo.getCigaretteType();
        this.dailyConsumption = savedInfo.getDailyConsumption();
        this.quitStartDate = savedInfo.getQuitStartDate();
        this.targetDate = savedInfo.getTargetDate();
        this.quitGoal = savedInfo.getQuitGoal();
        this.createdAt = savedInfo.getCreatedAt();
        this.modifiedAt = savedInfo.getModifiedAt();
    }
}
