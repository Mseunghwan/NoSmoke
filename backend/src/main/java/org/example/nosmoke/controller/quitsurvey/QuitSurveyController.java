package org.example.nosmoke.controller.quitsurvey;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.nosmoke.dto.ApiResponse;
import org.example.nosmoke.dto.quitsurvey.QuitSurveyListResponseDto;
import org.example.nosmoke.dto.quitsurvey.QuitSurveyRequestDto;
import org.example.nosmoke.dto.quitsurvey.QuitSurveyResponseDto;
import org.example.nosmoke.dto.smokinginfo.SmokingInfoResponseDto;
import org.example.nosmoke.entity.QuitSurvey;
import org.example.nosmoke.service.quitsurvey.QuitSurveyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/surveys")
@RequiredArgsConstructor
public class QuitSurveyController {
    private final QuitSurveyService quitSurveyService;

//    일일 금연 설문 등록
    @PostMapping
    public ResponseEntity<ApiResponse<QuitSurveyResponseDto>> saveSmokingInfo(@Valid@RequestBody QuitSurveyRequestDto requestDto) {
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();

            QuitSurvey savedSurvey = quitSurveyService.saveSurvey(Long.parseLong(userId), requestDto);
            QuitSurveyResponseDto responseDto = new QuitSurveyResponseDto(savedSurvey);

            // 우선 획득 포인트 여기서 정해주기
            if (savedSurvey.isSuccess()) {
                final int POINTS_FOR_SUCCESS = 10;
                responseDto.setPointsEarned(POINTS_FOR_SUCCESS);
            }

            ApiResponse<QuitSurveyResponseDto> response = ApiResponse.success(
                    "일일 흡연 설문이 등록되었습니다.",
                    responseDto
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        }catch(IllegalArgumentException e){

            ApiResponse<QuitSurveyResponseDto> response = ApiResponse.error(
                    "SAVE_ERROR",
                    e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<QuitSurveyListResponseDto>> getMySurveys(){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();

            List<QuitSurvey> savedSurveys = quitSurveyService.findSurveyByUserId(Long.parseLong(userId));

            QuitSurveyListResponseDto responseDto = new QuitSurveyListResponseDto(savedSurveys);

            ApiResponse<QuitSurveyListResponseDto> response = ApiResponse.success(
                    "사용자의 설문 목록 조회가 완료되었습니다",
                    responseDto
            );

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch(IllegalArgumentException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("SURVEY_NOT_FOUND", e.getMessage()));

        }
    }
}

