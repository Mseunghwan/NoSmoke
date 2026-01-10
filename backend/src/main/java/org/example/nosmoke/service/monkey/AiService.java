package org.example.nosmoke.service.monkey;

import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final ChatLanguageModel chatLanguageModel;

    // 채팅, 건강분석 공용 AI 호출 메서드
    public String generateResponse(String prompt) {
        log.info(">>> AI 요청 전송 (Thread: {})", Thread.currentThread());
        String response = chatLanguageModel.generate(prompt);
        log.info(">>> AI 응답 수신 완료");
        return response;
    }

}
