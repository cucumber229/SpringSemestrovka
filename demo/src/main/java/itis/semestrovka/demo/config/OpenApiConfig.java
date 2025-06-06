package itis.semestrovka.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    /**
     * Общая информация о вашем API (Info) и схема безопасности для X-CSRF-TOKEN.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("X-CSRF-TOKEN"))
                .components(new Components()
                        .addSecuritySchemes("X-CSRF-TOKEN",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .name("X-CSRF-TOKEN")
                        )
                )
                .info(new Info()
                        .title("TeamManager API")
                        .version("v1")
                        .description("Documentation for REST endpoints")
                );
    }

    /**
     * Группировка OpenAPI — показываем только URL, начинающиеся на /api/**.
     * MVC-эндпоинты (/projects/**, /tasks/** и т.д.) при этом не появятся в Swagger.
     */
    @Bean
    public GroupedOpenApi apiGroup() {
        return GroupedOpenApi.builder()
                .group("API")             // имя группы, видимое в Swagger UI
                .pathsToMatch("/api/**")  // показывать только REST-методы /api/...
                .build();
    }
}
