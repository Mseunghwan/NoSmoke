package org.example.nosmoke.dto.quitsurvey;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class QuitSurveyLightDto {

    private final boolean isSuccess;
    private final LocalDateTime createdAt;

}
