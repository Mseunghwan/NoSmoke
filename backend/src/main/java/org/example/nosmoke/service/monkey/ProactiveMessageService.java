package org.example.nosmoke.service.monkey;

import lombok.RequiredArgsConstructor;
import org.example.nosmoke.entity.User;
import org.example.nosmoke.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProactiveMessageService {

    private final UserRepository userRepository;
    private final MonkeyDialogueService monkeyDialogueService;

    // 우선 매일 오전 9시 기준으로 메소드 실행
    @Scheduled(cron = "0 0 9 * * *")
    public void sendProactiveMessagesToAllUsers(){

        System.out.println("모든 유저에게 메시지를 일단 보내봅시다");

        // User 들의 정보를 DB에서 찾아옵니다
        List<User> allUsers = userRepository.findAll();

        // 각 사용자 별 맞춤형 메시지 생성 후 저장
        for (User user : allUsers) {
            monkeyDialogueService.generateAndSaveProactiveMessage(user);
        }
        System.out.println("능동 메시지 전송 완료");
    }

}
