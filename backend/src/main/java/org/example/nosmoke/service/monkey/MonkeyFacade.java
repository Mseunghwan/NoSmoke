package org.example.nosmoke.service.monkey;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.nosmoke.dto.monkey.MonkeyChatContextDto;
import org.example.nosmoke.entity.MonkeyMessage;
import org.example.nosmoke.entity.SmokingInfo;
import org.example.nosmoke.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonkeyFacade {

    private final MonkeyService monkeyService;
    private final AiService aiService;

    // 채팅 기능
    public String chatWithSterling(Long userId, String userMessage){

        MonkeyChatContextDto context = monkeyService.getChatContext(userId);


        String prompt = monkeyService.createPersonPrompt(context, userMessage);


        String aiResponse = aiService.generateResponse(prompt);

        try {
            monkeyService.saveMessage(userId, aiResponse, MonkeyMessage.MessageType.REACTIVE);
        } catch (Exception e) {
            log.error("AI 응답 저장 실패 (유저: {}): {}", userId, aiResponse);
        }

        return aiResponse;
    }

    // 건강 분석 기능
    public String analyzeHealth(Long userId){
        MonkeyChatContextDto context = monkeyService.getChatContext(userId);

        if (context.getSmokingInfo() == null || context.getSmokingInfo().getQuitStartDate() == null) {
            return "아직 금연 정보를 등록하지 않으셨군요. 정보 탭에서 금연 시작일을 설정해주세요!";
        }

        String prompt = monkeyService.createHealthAnalysisPrompt(context);

        return aiService.generateResponse(prompt);
    }

}
