package org.example.nosmoke.service.monkey;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.nosmoke.config.RabbitMqConfig;
import org.example.nosmoke.dto.monkey.MonkeyAiRequestEvent;
import org.example.nosmoke.dto.monkey.MonkeyChatContextDto;
import org.example.nosmoke.entity.MonkeyMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonkeyFacade {

    private final MonkeyService monkeyService;
    // Facade 에서 이제 더이상 AI를 직접 부르지 않아도 되기에 AiService가 아닌 RabbitTemplate 부름
    private final RabbitTemplate rabbitTemplate;


    // 채팅 기능
    public String chatWithSterling(Long userId, String userMessage){
        MonkeyChatContextDto context = monkeyService.getChatContext(userId);

        String prompt = monkeyService.createPersonPrompt(context, userMessage);

        rabbitTemplate.convertAndSend(
                RabbitMqConfig.EXCHANGE_NAME,
                RabbitMqConfig.ROUTING_KEY,
                new MonkeyAiRequestEvent(userId, prompt)
        );

        log.info(">>> AI 요청 큐 발행 완료 (User: {}) ", userId);

        return "스털링이 고민을 시작했습니다...";
    }

    // 건강 분석 기능
    public String analyzeHealth(Long userId){
        MonkeyChatContextDto context = monkeyService.getChatContext(userId);

        if (context.getSmokingInfo() == null || context.getSmokingInfo().getQuitStartDate() == null) {
            return "아직 금연 정보를 등록하지 않으셨군요. 정보 탭에서 금연 시작일을 설정해주세요!";
        }

        String prompt = monkeyService.createHealthAnalysisPrompt(context);

        rabbitTemplate.convertAndSend(
                RabbitMqConfig.EXCHANGE_NAME,
                RabbitMqConfig.ROUTING_KEY,
                new MonkeyAiRequestEvent(userId, prompt)
        );

        log.info(">>> 건강 보고서 큐 발행 완료 (User: {}) ", userId);

        return "건강 보고서를 작성 중 입니다..";
    }

    // 메세지 조회
    public List<MonkeyMessage> findMessagesByUserId(Long userId) {
        return monkeyService.findMessagesByUserId(userId);
    }

}
