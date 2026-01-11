package org.example.nosmoke;

import dev.langchain4j.model.chat.ChatLanguageModel;
import org.example.nosmoke.entity.MonkeyMessage;
import org.example.nosmoke.entity.User;
import org.example.nosmoke.repository.MonkeyMessageRepository;
import org.example.nosmoke.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class NoSmokeApplicationTests {

    @MockitoBean
    private ChatLanguageModel chatLanguageModel;

    @Test
    void contextLoads() {
        // Context Load Test
        }
}
