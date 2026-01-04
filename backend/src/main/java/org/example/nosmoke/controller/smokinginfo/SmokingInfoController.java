package org.example.nosmoke.controller.quitsurvey;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.nosmoke.dto.ApiResponse;
import org.example.nosmoke.dto.smokinginfo.SmokingInfoRequestDto;
import org.example.nosmoke.dto.smokinginfo.SmokingInfoResponseDto;
import org.example.nosmoke.entity.SmokingInfo;
import org.example.nosmoke.service.smokinginfo.SmokingInfoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/smoking-info")
@RequiredArgsConstructor
public class SmokingInfoController{

    private final SmokingInfoService smokingInfoService;

    @PostMapping
    public ResponseEntity<ApiResponse<SmokingInfoResponseDto>> saveSmokingInfo(@Valid @RequestBody SmokingInfoRequestDto requestDto) {

        try{
            // 1. SecurityContextHolder에서 현재 인증된 사용자 정보를 가져옵니다.
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // 2. 사용제 주체에서 UserDetails를 얻어 사용자 ID를 추출합니다
            String userId = authentication.getName();


            // 3. 서비스 메소드를 호출할 때, DTO와 함께 추출한 userId를 넘겨줍니다.
            SmokingInfo savedInfo = smokingInfoService.smokingInfoSave(Long.valueOf(userId), requestDto);


            SmokingInfoResponseDto responseDto = new SmokingInfoResponseDto(savedInfo);

            ApiResponse<SmokingInfoResponseDto> response = ApiResponse.success(
                    "흡연 정보가 등록되었습니다.",
                    responseDto
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            ApiResponse<SmokingInfoResponseDto> response = ApiResponse.error(
                    "SAVE_ERROR",
                    e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PutMapping
    public ResponseEntity<ApiResponse<Void>> updateSmokingInfo(@Valid @RequestBody SmokingInfoRequestDto requestDto) {
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();

            smokingInfoService.smokingInfoSave(Long.valueOf(userId), requestDto);

            ApiResponse<Void> response = ApiResponse.success("흡연 정보가 성공적으로 수정되었습니다.", null);

            return ResponseEntity.status(HttpStatus.OK).body(response);


        } catch (IllegalArgumentException e) {
            ApiResponse<Void> response = ApiResponse.error("UPDATE_ERROR", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }


}
