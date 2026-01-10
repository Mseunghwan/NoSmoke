package org.example.nosmoke.service.monkey;

import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.nosmoke.entity.SmokingInfo;
import org.example.nosmoke.entity.User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final ChatLanguageModel chatLanguageModel;

    // 스털링과 1 : 1 채팅 기능
    public String generateChatResponse(User user, SmokingInfo smokingInfo, String userMessage){

        // 스털링 페르소나 및 사용자 정보 주입
        String systemPrompt = createPersonaPrompt(user, smokingInfo);

        // 프롬프트 결합
        String fullPrompt = systemPrompt + "\n\n[주인님의 말씀]: " + userMessage + "\n[스털링의 대답]:";

        return chatLanguageModel.generate(fullPrompt);
    }

    // 건강 상태 분석 (현재 상태 기반 조언)
    public String generateHealthAnalysis(SmokingInfo smokingInfo) {
       if (smokingInfo == null || smokingInfo.getQuitStartDate() == null) {
            return "끼끼! 아직 금연 정보를 등록하지 않으셨군요. 정보 탭에서 금연 시작일을 설정해주세요!";
        }

        long quitDays = ChronoUnit.DAYS.between(smokingInfo.getQuitStartDate(), LocalDate.now());

        String prompt = String.format(
                "당신은 금연 클리닉의 전문 의사입니다. 환자는 현재 금연 %d일차입니다. " +
                        "현재 시점에서 의학적으로 환자의 신체에 어떤 긍정적인 변화가 일어나고 있는지 설명하고, " +
                        "앞으로 주의해야 할 금단 증상이나 건강 관리 조언을 전문적이고 정중한 '해요체'(~해요, ~입니다)로 3줄 요약해서 분석해 주세요. " +
                        "캐릭터 연기나 불필요한 미사여구는 배제하고, 신뢰감 있는 정보를 전달해 주세요.",
                quitDays
        );

        return chatLanguageModel.generate(prompt);
    }

    // 페르소나 생성
    private String createPersonaPrompt(User user, SmokingInfo info) {
        String name = user.getName();

        long days = (info != null && info.getQuitStartDate() != null) ?
                ChronoUnit.DAYS.between(info.getQuitStartDate(), LocalDate.now()) : 0;

        return "당신은 '스털링'이라는 이름의 AI 금연 도우미 원숭이입니다. " +
                "사용자를 '" + name + " 주인님'이라고 부르세요. 말투는 예의 바르지만 장난기 있고 귀여워야 하며, 말 끝에 '끼끼!'나 '끽!'을 붙이세요. " +
                "주인님은 현재 금연 " + days + "일차입니다. 주인님의 말을 잘 듣고 금연을 응원해주세요.";
    }



}
