package org.example.nosmoke.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.nosmoke.service.monkey.MonkeyFacade;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final MonkeyFacade monkeyFacade;

    // 사용자가 /sub/channel/{userId} 에 구독(입장)하는 순간 실행 -- 첫 마디
    @EventListener
    public void handleSessionSubscribeEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = headerAccessor.getDestination();

        // 구독 주소가 맞는지 확인
        if(destination != null && destination.startsWith("/sub/channel/")){
            String userIdStr = destination.replace("/sub/channel/", "");
            try{
                Long userId = Long.parseLong(userIdStr);
                log.info(">>> [WebSocket] User {} 입장 감지!", userId);

                // 입장하자마자 스털링이 말을 건다
                monkeyFacade.sendWelcomeMessage(userId);
            } catch (NumberFormatException e){
                log.error(">>> 잘못된 유저 ID: {}", userIdStr);
            }
        }
    }

}
