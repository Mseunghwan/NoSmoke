package org.example.nosmoke.service.monkey;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.nosmoke.config.RabbitMqConfig;
import org.example.nosmoke.dto.monkey.MonkeyAiRequestEvent;
import org.example.nosmoke.dto.monkey.MonkeyChatContextDto;
import org.example.nosmoke.dto.monkey.MonkeyMessageResponseDto;
import org.example.nosmoke.entity.MonkeyMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Slice;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonkeyFacade {

    private final MonkeyService monkeyService;
    // Facade 에서 이제 더이상 AI를 직접 부르지 않아도 되기에 AiService가 아닌 RabbitTemplate 부름
    private final RabbitTemplate rabbitTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final TaskScheduler taskScheduler;


    // 채팅 기능
    public String chatWithSterling(Long userId, String userMessage){ // 성능 개선해야

        // 유저 메시지 DB에 저장
        monkeyService.saveMessage(userId, userMessage, MonkeyMessage.MessageType.USER);

        // DB 에서 채팅 조회
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

    public void sendWelcomeMessage(Long userId){
        String welcomeText = "어서오세요! 금연 도우미 스털링입니다.\n오늘 몸 상태는 좀 어떠신가요?";

        MonkeyMessageResponseDto responseDto = MonkeyMessageResponseDto.builder()
                .content(welcomeText)
                .messageType("PROACTIVE")
                .build();

        taskScheduler.schedule(() -> {
            try {
                messagingTemplate.convertAndSend("/sub/channel/" + userId, responseDto);
                log.info(">>> [Proactive] 스털링이 먼저 인사를 건넸습니다. (To: {})", userId);
            } catch (Exception e) {
                log.error(">>> 웰컴 메시지 전송 중 에러", e);
            }
        }, Instant.now().plusMillis(500));
    }

    // 메세지 조회
    public Slice<MonkeyMessage> findMessagesByUserId(Long userId, int page, int size) {
        return monkeyService.findMessagesByUserId(userId, page, size);
    }

}
