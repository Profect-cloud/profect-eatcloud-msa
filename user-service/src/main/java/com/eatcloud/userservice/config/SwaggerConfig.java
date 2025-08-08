package com.eatcloud.userservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI userServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("User Service API")
                        .description("사용자 관리 서비스 API 문서")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("EatCloud Team")
                                .email("profect.eatcloud@gmail.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080/user-service").description("API Gateway - User Service"),
                        new Server().url("http://localhost:8080").description("API Gateway (Legacy)"),
                        new Server().url("http://localhost:8081").description("User Service Direct")
                ));
    }
}
