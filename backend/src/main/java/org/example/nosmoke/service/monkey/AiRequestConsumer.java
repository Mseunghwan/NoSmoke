package org.example.nosmoke.service.monkey;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.nosmoke.config.RabbitMqConfig;
import org.example.nosmoke.dto.monkey.MonkeyAiRequestEvent;
import org.example.nosmoke.dto.monkey.MonkeyMessageResponseDto;
import org.example.nosmoke.entity.MonkeyMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiRequestConsumer {

    private final AiService aiService;
    private final MonkeyService monkeyService;
    private final SimpMessagingTemplate messagingTemplate; // 웹 소켓 주입

    // RabbitMQ 리스너
    // concurrency = "1" : 스레드 1개만 사용하여 순차적인 처리 (Rate Limit 효과)
    @RabbitListener(queues = RabbitMqConfig.QUEUE_NAME, concurrency = "1")
    public void receiveMessage(MonkeyAiRequestEvent event){
        Long userId = event.getUserId();
        String prompt = event.getPrompt();

        log.info(">>> [Worker] 큐에서 작업 꺼냄 (User: {}) ", userId);

        try {
            // AI 호출 (여기서 1 ~ 3초)
            String aiResponse = aiService.generateResponse(prompt);

            // 결과 DB 저장
            MonkeyMessage saveMessage = monkeyService.saveMessage(userId, aiResponse, MonkeyMessage.MessageType.REACTIVE);

            log.info(">>> [Worker] 처리 완료 및 저장 성공 (User: {})", userId);

            // WebSocket으로 실시간 알림 전송
            // 구독 주소 : /sub/channel/{userId}
            MonkeyMessageResponseDto responseDto = new MonkeyMessageResponseDto(saveMessage);

            messagingTemplate.convertAndSend("/sub/channel/" + userId, responseDto);

            log.info(">>> [WebSocket] 실시간 응답 전송 완료 (Target: /sub/channel/{}) ", userId);

        } catch (Exception e) {
            log.error(">>> [Worker] AI 처리 중 오류 발생 : {}", e.getMessage());
        }
    }

}
