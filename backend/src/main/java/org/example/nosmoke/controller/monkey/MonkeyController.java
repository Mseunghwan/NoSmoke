package org.example.nosmoke.controller.monkey;

import lombok.RequiredArgsConstructor;
import org.example.nosmoke.dto.ApiResponse;
import org.example.nosmoke.dto.monkey.MonkeyMessageResponseDto;
import org.example.nosmoke.entity.MonkeyMessage;
import org.example.nosmoke.service.monkey.MonkeyFacade;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/monkey")
@RequiredArgsConstructor
public class MonkeyController {
    private final MonkeyFacade monkeyFacade;

    // 스털링 챗봇 기능
    @PostMapping("/chat/{userId}")
    public ResponseEntity<ApiResponse<String>> chat(
            @PathVariable Long userId,
            @RequestBody Map<String, String> payload // {"message": "금연이 너무 힘들어"} 식으로
            ){
        String userMessage = payload.get("message");
        String aiResponse = monkeyFacade.chatWithSterling(userId, userMessage);

        return ResponseEntity.ok(ApiResponse.success("응답 성공", aiResponse));
    }

    // 건강 분석 요청
    @PostMapping("/analysis/{userId}")
    public ResponseEntity<ApiResponse<String>> getHealthAnalysis(@PathVariable Long userId) {
        String analysis = monkeyFacade.analyzeHealth(userId);
        return ResponseEntity.ok(ApiResponse.success("분석 완료", analysis));
    }

    @GetMapping("/messages")
    public ResponseEntity<ApiResponse<Slice<MonkeyMessageResponseDto>>> getMyMonkeyMessage(
            @RequestParam(defaultValue = "0") int page, // 0 페이지부터 시작
            @RequestParam(defaultValue = "20") int size // 한 번에 20개 메시지 로딩
    ){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();

            Slice<MonkeyMessage> messageSlice = monkeyFacade.findMessagesByUserId(Long.parseLong(userId), page, size);

            Slice<MonkeyMessageResponseDto> responseDtos = messageSlice
                    .map(MonkeyMessageResponseDto::new);

            ApiResponse<Slice<MonkeyMessageResponseDto>> response = ApiResponse.success(
                    "메시지 목록 조회 성공(Slice)",
                    responseDtos
            );

            return ResponseEntity.ok(response);
        } catch (Exception e){
            return ResponseEntity.
                    internalServerError().
                    body(ApiResponse.error("MESSAGE_FETCH_ERROR", e.getMessage()));

        }
    }
}
