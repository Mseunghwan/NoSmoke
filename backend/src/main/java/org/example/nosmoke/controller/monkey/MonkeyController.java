package org.example.nosmoke.controller.monkey;

import lombok.RequiredArgsConstructor;
import org.example.nosmoke.dto.ApiResponse;
import org.example.nosmoke.dto.monkey.MonkeyMessageResponseDto;
import org.example.nosmoke.entity.MonkeyMessage;
import org.example.nosmoke.service.monkey.MonkeyDialogueService;
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
    private final MonkeyDialogueService monkeyDialogueService;

    // 스털링 챗봇 기능
    @PostMapping("/chat/{userId}")
    public ResponseEntity<ApiResponse<String>> chat(
            @PathVariable Long userId,
            @RequestBody Map<String, String> payload // {"message": "금연이 너무 힘들어"} 식으로
            ){
        String userMessage = payload.get("message");
        String aiResponse = monkeyDialogueService.chatWithSterling(userId, userMessage);

        return ResponseEntity.ok(ApiResponse.success("응답 성공", aiResponse));
    }

    // 건강 분석 요청
    @PostMapping("/analysis/{userId}")
    public ResponseEntity<ApiResponse<String>> getHealthAnalysis(@PathVariable Long userId) {
        String analysis = monkeyDialogueService.analyzeHealth(userId);
        return ResponseEntity.ok(ApiResponse.success("분석 완료", analysis));
    }

    @GetMapping("/messages")
    public ResponseEntity<ApiResponse<List<MonkeyMessageResponseDto>>> getMyMonkeyMessage(){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();

            List<MonkeyMessage> messages = monkeyDialogueService.findMessagesByUserId(Long.parseLong(userId));

            List<MonkeyMessageResponseDto> responseDtos = messages.stream()
                    .map(MonkeyMessageResponseDto::new)
                    .collect(Collectors.toList());

            ApiResponse<List<MonkeyMessageResponseDto>> response = ApiResponse.success(
                    "원숭이 메시지 조회가 완료되었습니다.",
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
