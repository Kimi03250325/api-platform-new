package com.bank.apiplatform.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        // 定義 API Key 安全方案
        SecurityScheme apiKeyScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("X-API-Key")
                .description("請輸入 API Key");
        
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("API-Key");
        
        return new OpenAPI()
                .info(new Info()
                        .title("🏦 銀行 API 平台")
                        .version("1.0.0")
                        .description(buildDescription())
                        .contact(new Contact()
                                .name("API 支援團隊")
                                .email("api-support@bank.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("本地開發環境")
                ))
                .components(new Components()
                        .addSecuritySchemes("API-Key", apiKeyScheme))
                .addSecurityItem(securityRequirement)
                .externalDocs(new ExternalDocumentation()
                        .description("📊 動態查詢測試工具")
                        .url("/tools/query-tool"));
    }
    
    private String buildDescription() {
        // 請參考 artifact 中的完整內容
        return "...";
    }
}