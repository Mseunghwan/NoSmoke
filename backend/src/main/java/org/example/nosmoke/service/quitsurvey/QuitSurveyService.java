package org.example.nosmoke.service.quitsurvey;

import lombok.RequiredArgsConstructor;
import org.example.nosmoke.dto.quitsurvey.QuitSurveyRequestDto;
import org.example.nosmoke.entity.QuitSurvey;
import org.example.nosmoke.repository.MonkeyMessageRepository;
import org.example.nosmoke.repository.QuitSurveyRepository;
import org.example.nosmoke.repository.UserRepository;
import org.example.nosmoke.service.monkey.MonkeyDialogueService;
import org.example.nosmoke.service.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuitSurveyService {
    private final QuitSurveyRepository quitSurveyRepository;
    private final UserRepository userRepository;
    private final MonkeyDialogueService monkeyDialogueService;
    private final UserService userService;

//    일일 금연 설문 저장 메서드
    @Transactional
    public QuitSurvey saveSurvey(Long userId, QuitSurveyRequestDto requestDto) {
        // User 확인
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. id = " + userId));

        QuitSurvey quitSurvey = new QuitSurvey(
                userId,
                requestDto.isSuccess(),
                requestDto.getStressLevel(),
                requestDto.getStressCause(),
                requestDto.getCravingLevel(),
                requestDto.getAdditionalNotes()
        );

        QuitSurvey savedSurvey = quitSurveyRepository.save(quitSurvey);

        // 금연 성공 시 포인트 지급
        if (savedSurvey.isSuccess()) {
            final int POINTS_FOR_SUCCESS = 10; // 성공시 10 포인트
            userService.addPoints(userId, POINTS_FOR_SUCCESS);
        }

        monkeyDialogueService.generateAndSaveReactiveMessage(userId, savedSurvey);

        return savedSurvey;

    }

    // 특정 사용자 설문 기록 조회
    public List<QuitSurvey> findSurveyByUserId(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. id = " + userId));
        return quitSurveyRepository.findByUserId(userId);
    }
}
