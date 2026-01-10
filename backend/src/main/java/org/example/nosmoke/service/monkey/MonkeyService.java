package org.example.nosmoke.service.monkey;

import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import org.example.nosmoke.entity.MonkeyMessage;
import org.example.nosmoke.entity.QuitSurvey;
import org.example.nosmoke.entity.SmokingInfo;
import org.example.nosmoke.entity.User;
import org.example.nosmoke.repository.MonkeyMessageRepository;
import org.example.nosmoke.repository.QuitSurveyRepository;
import org.example.nosmoke.repository.SmokingInfoRepository;
import org.example.nosmoke.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MonkeyService {
    private final MonkeyMessageRepository monkeyMessageRepository;
    private final UserRepository userRepository;
    private final SmokingInfoRepository smokingInfoRepository;

    public User getUser(Long userId){
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    public SmokingInfo getSmokingInfo(Long userId){
        return smokingInfoRepository.findByUserId(userId)
                .orElse(null);
    }

    // DB에 직접 접근하는 부분
    @Transactional
    public void saveMessage(User user, String content, MonkeyMessage.MessageType type){
        MonkeyMessage message = MonkeyMessage.builder()
                .user(user)
                .content(content)
                .messageType(type)
                .build();

        monkeyMessageRepository.save(message);

    }

    public List<MonkeyMessage> findMessagesByUserId(Long userId){
        return monkeyMessageRepository.findByUser_IdOrderByCreatedAtDesc(userId);
    }


}