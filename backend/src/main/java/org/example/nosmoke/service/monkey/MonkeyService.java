package org.example.nosmoke.service.monkey;

import lombok.RequiredArgsConstructor;
import org.example.nosmoke.dto.monkey.MonkeyChatContextDto;
import org.example.nosmoke.entity.MonkeyMessage;
import org.example.nosmoke.entity.SmokingInfo;
import org.example.nosmoke.entity.User;
import org.example.nosmoke.repository.MonkeyMessageRepository;
import org.example.nosmoke.repository.SmokingInfoRepository;
import org.example.nosmoke.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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

        return String.format(
                "당신은 금연을 돕는 다정한 원숭이 '스털링'입니다.\n" +
                        "사용자 이름: %s\n" +
                        "사용자 정보: %d일째 금연 중\n" +
                        "사용자 메시지: \"%s\"\n" +
                        "\n" +
                        "--- [반드시 지켜야 할 답변 규칙] ---\n" +
                        "1. **절대로 사용자의 메시지를 그대로 다시 반복하지 마십시오.** (가장 중요)\n" +
                        "2. 사용자의 말에 대한 '공감'과 '답변'만 3문장 이내로 간결하게 하십시오.\n" +
                        "3. 말투는 친근하고 격려하는 어조(해요체)를 사용하십시오.\n" +
                        "4. 답변 내용만 즉시 출력하십시오.",
                name,
                quitDays,
                userMessage
        );
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
    public MonkeyMessage saveMessage(Long userId, String content, MonkeyMessage.MessageType type){

        User user = userRepository.getReferenceById(userId);

        MonkeyMessage message = MonkeyMessage.builder()
                .user(user)
                .content(content)
                .messageType(type)
                .build();

        monkeyMessageRepository.save(message);

        return message;
    }

    // 메시지 조회
    public Slice<MonkeyMessage> findMessagesByUserId(Long userId, int page, int size){
        Pageable pageable = PageRequest.of(page,size);

        return monkeyMessageRepository.findByUser_IdOrderByCreatedAtDesc(userId, pageable);
    }

}