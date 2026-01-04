package org.example.nosmoke.controller.monkey;

import lombok.RequiredArgsConstructor;
import org.example.nosmoke.dto.ApiResponse;
import org.example.nosmoke.dto.monkey.MonkeyMessageResponseDto;
import org.example.nosmoke.entity.MonkeyMessage;
import org.example.nosmoke.service.monkey.MonkeyDialogueService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/monkey")
@RequiredArgsConstructor
public class MonkeyController {
    private final MonkeyDialogueService monkeyDialogueService;

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
