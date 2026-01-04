package org.example.nosmoke;

import dev.langchain4j.model.chat.ChatLanguageModel;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class NoSmokeApplicationTests {

    // [핵심] 젠킨스에는 Google 인증 정보가 없으므로,
    // 실제 GeminiConfig 빈 대신 가짜(Mock) 객체를 사용하여 컨텍스트 로드 에러를 막습니다.
    @MockBean
    private ChatLanguageModel chatLanguageModel;

    @Test
    void contextLoads() {
        // 이 테스트는 단순히 Spring Context가 잘 뜨는지만 확인합니다.
        // @MockBean 덕분에 Gemini 연결 없이도 성공할 것입니다.
    }
}