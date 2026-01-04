package org.example.nosmoke;

import dev.langchain4j.model.chat.ChatLanguageModel;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class NoSmokeApplicationTests {

    @MockitoBean
    private ChatLanguageModel chatLanguageModel;

    @Test
    void contextLoads() {
        // Context Load Test
    }
}