package ru.daniil.NauJava.config.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerJwtConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Локальный сервер"),
                        new Server().url("/").description("Текущий сервер")
                ))
                .info(new Info()
                        .title("Calorie Tracker API")
                        .version("1.0")
                        .description("""
                            ## Получение JWT токена:
                            
                            1. **Выполнить GET запрос:** `/api/auth/token`
                            2. **Скопировать** `accessToken` из ответа
                            
                            ## Использование токена:
                            
                            1. Нажать кнопку **Authorize** выше
                            2. Ввести: `скопированный_токен`
                            3. Все запросы будут отправляться с заголовком `Authorization`
                            
                            ## Информация:
                            - JWT токен действителен 24 часа
                            """))

                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("""
                                            Введите: <JWT_токен>
                                            """))
                );
    }
}