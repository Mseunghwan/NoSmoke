package org.example.nosmoke.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // API 문서의 기본 정보를 설정
        Info info = new Info()
                .title("NoSmoke Project API")
                .version("v1.0.0")
                .description("금연 앱 NoSmoke의 API 명세서입니다.");

        // JWT 인증 방식을 설정
        // - securitySchemeName: 우리가 사용할 인증 방식의 이름, "bearerAuth"로 지정
        String securitySchemeName = "bearerAuth";

        // API 요청 헤더에 인증 정보를 담을 방식을 정의
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(securitySchemeName);

        // Components 객체를 통해 SecurityScheme을 설정합니다.
        Components components = new Components()
                .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                        .name(securitySchemeName) // 스킴 이름
                        .type(SecurityScheme.Type.HTTP) // 인증 타입: HTTP
                        .scheme("bearer") // 스킴: bearer (JWT)
                        .bearerFormat("JWT")); // 베어러 포맷: JWT

        // 위에서 만든 Info, SecurityRequirement, Components를 OpenAPI 객체에 설정하여 반환
        return new OpenAPI()
                .info(info)
                .addSecurityItem(securityRequirement)
                .components(components);
    }
}
