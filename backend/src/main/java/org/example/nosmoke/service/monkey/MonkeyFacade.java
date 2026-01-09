package org.example.nosmoke.service.monkey;

import lombok.RequiredArgsConstructor;
import org.example.nosmoke.entity.MonkeyMessage;
import org.example.nosmoke.entity.SmokingInfo;
import org.example.nosmoke.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MonkeyFacade {

    private final MonkeyService monkeyService;
    private final AiService aiService;

    // 채팅 기능
    public String chatWithSterling(Long userId, String userMessage){
        User user = monkeyService.getUser(userId);
        SmokingInfo smokingInfo = monkeyService.getSmokingInfo(userId);

        // 응답 생성
        String aiResponse = aiService.generateChatResponse(user, smokingInfo, userMessage);

        // 저장
        monkeyService.saveMessage(user, aiResponse, MonkeyMessage.MessageType.REACTIVE);

        return aiResponse;


    }

    // 건강 분석 기능
    public String analyzeHealth(Long userId){
        SmokingInfo smokingInfo = monkeyService.getSmokingInfo(userId);

        return aiService.generateHealthAnalysis(smokingInfo);
    }

    public List<MonkeyMessage> findMessagesByUserId(Long userId){
        return monkeyService.findMessagesByUserId(userId);
    }

}
