package org.example.nosmoke.dto.quitsurvey;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class QuitSurveyUpdateRequestDto {
    @NotNull(message = "금연 성공 여부는 필수입니다.")
    private final boolean isSuccess;

    @NotNull(message = "스트레스 수준은 필수입니다.")
    @Min(value = 1, message = "스트레스 수준은 1 이상이어야 합니다.")
    @Max(value = 10, message = "스트레스 수준은 10 이하여야 합니다.")
    private final Integer stressLevel;

    @Size(max = 255, message = "스트레스 원인은 255자 이내여야 합니다.")
    private final String stressCause;

    @NotNull(message = "흡연 충동 수준은 필수입니다.")
    @Min(value = 1, message = "흡연 충동 수준은 1 이상이어야 합니다.")
    @Max(value = 10, message = "흡연 충동 수준은 10 이하여야 합니다.")
    private final Integer cravingLevel;

    @Size(max = 1000, message = "추가 내용은 1000자 이내여야 합니다.")
    private final String additionalNotes;
}