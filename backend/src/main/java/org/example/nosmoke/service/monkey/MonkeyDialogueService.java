package org.example.nosmoke.service.monkey;
// 사용자 행동(설문, 대화) 토대로 반응하는 스털링 Service

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
import org.example.nosmoke.service.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MonkeyDialogueService {
    private final MonkeyMessageRepository monkeyMessageRepository;
    private final UserRepository userRepository;
    private final SmokingInfoRepository smokingInfoRepository;

    private final ChatLanguageModel chatLanguageModel;
    private final QuitSurveyRepository quitSurveyRepository;

    // QuitSurveyService에서 설문 DB에 저장한 신호를 보내면 해당 service 호출,
    // QuitSurveyService가 MonkeyDialogueService 알도록 의존성 주입해주어야
    @Transactional
    public void generateAndSaveReactiveMessage(Long userId, QuitSurvey latestSurvey) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("메시지를 생성할 사용자를 찾을 수 없습니다. id = " + userId));

        // 사용자 기본 흡연정보 불러옴(gemini에 전달 정보 위함)
        SmokingInfo smokingInfo = smokingInfoRepository.findByUserId(userId).orElse(null);

        // 프롬프트 생성
        String prompt = createPrompt(smokingInfo, latestSurvey);

        // 모델 호출 후 응답 회신
        String answer = chatLanguageModel.generate(prompt);

        // monkey 엔터티 생성
        MonkeyMessage message = MonkeyMessage.builder()
                .user(user)
                .content(answer)
                .messageType(MonkeyMessage.MessageType.REACTIVE)
                .build();

        monkeyMessageRepository.save(message);

    }

    @Transactional
    public void generateAndSaveProactiveMessage(User user) {
        SmokingInfo smokingInfo = smokingInfoRepository.findByUserId(user.getId()).orElse(null);

        // 최근 5개의 설문기록 조회
        List<QuitSurvey> recentSurveys = quitSurveyRepository.findTop5ByUserIdOrderByCreatedAtDesc(user.getId());

        String prompt = createProactivePrompt(user, smokingInfo, recentSurveys);
        String answer = chatLanguageModel.generate(prompt);

        MonkeyMessage message = MonkeyMessage.builder()
                .user(user)
                .content(answer)
                .messageType(MonkeyMessage.MessageType.PROACTIVE)
                .build();
        monkeyMessageRepository.save(message);
    }

    public List<MonkeyMessage> findMessagesByUserId(Long userId) {
        return monkeyMessageRepository.findByUser_IdOrderByCreatedAtDesc(userId);
    }

    public String createPrompt(SmokingInfo smokingInfo, QuitSurvey latestSurvey) {
        String quitGoal = (smokingInfo != null) ? smokingInfo.getQuitGoal() : "알 수 없음";
        String successStatus = latestSurvey.isSuccess() ? "성공" : "실패";

        return String.format(
                "너는 사용자의 금연을 돕는 귀여운 원숭이 캐릭터 '스털링'야. 항상 사용자를 '주인님'이라고 부르고, 말 끝에 '끼끼!'나 '끽!'을 붙여야 해. " +
                        "아래 정보를 바탕으로, 주인님에게 아주 친근하고 따뜻한 격려 메시지를 한두 문장으로 만들어줘. " +
                        "만약 금연에 실패했다면, 질책하지 말고 따뜻하게 위로해줘야 해.\n\n" +
                        "- 주인님의 금연 목표: %s\n" +
                        "- 오늘의 금연 성공 여부: %s\n" +
                        "- 오늘의 스트레스 지수 (1~10): %d\n" +
                        "- 스트레스 원인: %s\n" +
                        "- 오늘의 흡연 충동 지수 (1-10): %d",
                quitGoal, successStatus, latestSurvey.getStressLevel(), latestSurvey.getStressCause(), latestSurvey.getCravingLevel()
        );
    }

    public String createProactivePrompt(User user, SmokingInfo smokingInfo, List<QuitSurvey> recentSurveys) {
        // 금연 일수 세기
        long quitDays = (smokingInfo != null && smokingInfo.getQuitStartDate() != null)
                ? java.time.Duration.between(smokingInfo.getQuitStartDate(), java.time.LocalDateTime.now()).toDays()
                : 0;

        String quitGoal = (smokingInfo != null) ? smokingInfo.getQuitGoal() : "";

        // 최근 설문 데이터 분석
        long successCount = recentSurveys.stream().filter(QuitSurvey::isSuccess).count();
        String recentTrend = "최근 금연 성공률은 " + (recentSurveys.isEmpty() ? 0 : (successCount * 100 / recentSurveys.size())) + "%에요.";

        return String.format(
                "너는 사용자의 금연을 돕는 귀여운 원숭이 캐릭터 '스털링'이야. 항상 사용자를 '주인님'이라고 부르고, 말 끝에 '끼끼!'나 '끽!'을 붙여야 해. " +
                        "오늘은 주인님이 아무런 행동을 하지 않았지만, 네가 먼저 응원의 메시지를 보내는 상황이야. 아래 정보를 바탕으로 주인님을 격려하고, 금연 의지를 다질 수 있는 메시지를 한두 문장으로 만들어줘.\n\n" +
                        "- 주인님 닉네임: %s\n" +
                        "- 주인님의 금연 목표: %s\n" +
                        "- 금연 시작한 지: %d일째\n" +
                        "- 최근 금연 기록 분석: %s\n\n" +
                        "위 정보를 활용해서, 오늘 하루도 잘 해낼 수 있도록 힘을 불어넣어 줘!",
                user.getName(), quitGoal, quitDays, recentTrend
        );
    }

}
