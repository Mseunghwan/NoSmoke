package org.example.nosmoke.dto.smokinginfo;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Getter
@RequiredArgsConstructor
public class SmokingInfoRequestDto {
    @NotBlank(message = "담배 종류는 필수입니다.")
    @Size(max = 100, message = "담배 종류는 100자 이내여야 합니다.")
    private final String cigaretteType;

    @NotNull(message = "일일 소비량은 필수입니다.")
    @Min(value = 1, message = "일일 소비량은 1개 이상이어야 합니다.")
    @Max(value = 100, message = "일일 소비량은 100개 이하여야 합니다.")
    private final int dailyConsumption;

    @NotNull(message = "금연 시작일은 필수입니다.")
    private final LocalDate quitStartTime;

    @NotNull(message = "목표 날짜는 필수입니다.")
    private final LocalDate targetDate;

    @NotBlank(message = "금연 목표는 필수입니다.")
    @Size(max = 255, message = "금연 목표는 255자 이내여야 합니다.")
    private final String quitGoal;
}
