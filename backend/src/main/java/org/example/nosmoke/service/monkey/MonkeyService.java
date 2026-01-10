package org.example.nosmoke.service.monkey;

import lombok.RequiredArgsConstructor;
import org.example.nosmoke.dto.monkey.MonkeyChatContextDto;
import org.example.nosmoke.entity.MonkeyMessage;
import org.example.nosmoke.entity.SmokingInfo;
import org.example.nosmoke.entity.User;
import org.example.nosmoke.repository.MonkeyMessageRepository;
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

    // 정보 조회
    public MonkeyChatContextDto getChatContext(Long userId){
        User user = userRepository.getByIdOrThrow(userId);
        SmokingInfo smokingInfo = smokingInfoRepository.getByUserIdOrNull(userId);

        return new MonkeyChatContextDto(user, smokingInfo);
    }

    // 채팅용 프롬프트 생성
    public String createPersonPrompt(MonkeyChatContextDto context, String userMessage) {
        String name = context.getUser().getName();
        long quitDays = 0;

        // 날짜 계산
        if (context.getSmokingInfo() != null && context.getSmokingInfo().getQuitStartDate() != null) {
            quitDays = context.getSmokingInfo().getQuitDays();
        }

        return "당신은 '스털링'이라는 이름의 AI 금연 도우미 원숭이입니다. " +
                "사용자를 '" + name + " 주인님'이라고 부르세요. 말투는 예의 바르지만 장난기 있고 귀여워야 하며, 말 끝에 '끼끼!'나 '끽!'을 붙이세요. " +
                "주인님은 현재 금연 " + quitDays + "일차입니다. 주인님의 말을 잘 듣고 금연을 응원해주세요.\n\n" +
                "[주인님의 말씀]: " + userMessage + "\n[스털링의 대답]:";
    }

    // 건강 분석용 프롬프트 생성
    public String createHealthAnalysisPrompt(MonkeyChatContextDto context) {
        long quitDays = context.getSmokingInfo().getQuitDays();

        return String.format(
                "당신은 금연 클리닉의 전문 의사입니다. 환자는 현재 금연 %d일차입니다. " +
                        "현재 시점에서 의학적으로 환자의 신체에 어떤 긍정적인 변화가 일어나고 있는지 설명하고, " +
                        "앞으로 주의해야 할 금단 증상이나 건강 관리 조언을 전문적이고 정중한 '해요체'(~해요, ~입니다)로 3줄 요약해서 분석해 주세요. " +
                        "캐릭터 연기나 불필요한 미사여구는 배제하고, 신뢰감 있는 정보를 전달해 주세요.",
                quitDays
        );
    }

    // 메시지 저장(쓰기 트랜잭션)
    @Transactional
    public void saveMessage(Long userId, String content, MonkeyMessage.MessageType type){

        User user = userRepository.getReferenceById(userId);

        MonkeyMessage message = MonkeyMessage.builder()
                .user(user)
                .content(content)
                .messageType(type)
                .build();

        monkeyMessageRepository.save(message);
    }

    // 메시지 조회
    public List<MonkeyMessage> findMessagesByUserId(Long userId) {
        return monkeyMessageRepository.findByUser_IdOrderByCreatedAtDesc(userId);
    }

}