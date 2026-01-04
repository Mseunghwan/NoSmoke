package org.example.nosmoke.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.vertexai.VertexAiGeminiChatModel;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class GeminiConfig {
    @Value("${project_id}")
    private String projectId;

    @Value("${project_location}")
    private String location;

    @Value("${gemini_model_name}")
    private String modelName;



    @Bean
    public ChatLanguageModel chatLanguageModel() {
        // Langchain4j 제공 Gemini 모델 빌더
        // API 키 설정 없어도 O - 기본 자격증명 사용
        return VertexAiGeminiChatModel.builder()
                .project(projectId)
                .location(location)
                .modelName(modelName)
                .build();
    }
}
