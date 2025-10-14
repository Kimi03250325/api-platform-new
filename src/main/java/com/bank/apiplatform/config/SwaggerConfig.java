package com.bank.apiplatform.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI 配置
 */
@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("銀行 API 平台")
                .version("1.0.0")
                .description("財富管理系統 API 介接平台 - 支援多資料源動態查詢")
                .contact(new Contact()
                    .name("Bank API Team")
                    .email("api@bank.com"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8080")
                    .description("開發環境")
            ));
    }
}