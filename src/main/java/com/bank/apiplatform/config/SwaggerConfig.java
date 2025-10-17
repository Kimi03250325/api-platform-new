package com.bank.apiplatform.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Swagger/OpenAPI 配置
 */
@Configuration
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("銀行 API 平台")
                        .version("1.0.0")
                        .description(getDescription())
                        .contact(new Contact()
                                .name("技術支援團隊")
                                .email("api-support@bank.com")
                                .url("https://support.bank.com")))
                .servers(Arrays.asList(
                        new Server().url("http://localhost:" + serverPort).description("本地開發環境"),
                        new Server().url("https://api.bank.com").description("生產環境")
                ))
                .components(new Components()
                        .addSecuritySchemes("ApiKeyAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-API-Key")
                                .description(getApiKeyDescription())))
                .addSecurityItem(new SecurityRequirement().addList("ApiKeyAuth"));
    }

    private String getDescription() {
        StringBuilder desc = new StringBuilder();
        desc.append("## 平台簡介\n\n");
        desc.append("銀行 API 平台提供配置化的通用查詢端點，支援多資料源、動態查詢和 API Key 認證。\n\n");
        desc.append("**注意事項：**\n");
        desc.append("- 本系統為內部使用系統\n");
        desc.append("- 所有 API 僅供授權人員使用\n");
        desc.append("- 版權所有 © 2025 銀行公司\n\n");
        desc.append("## 快速開始\n\n");
        desc.append("### 1. 取得 API Key\n");
        desc.append("所有 API 請求都需要在 Header 中包含 API Key：\n");
        desc.append("```\nX-API-Key: your_api_key_here\n```\n\n");
        desc.append("**測試用 API Key:** `api_live_1234567890abcdef1234567890abcdef`\n\n");
        desc.append("### 2. 查看可用查詢\n");
        desc.append("```\nGET /api/query/available\n```\n\n");
        desc.append("## 主要功能\n\n");
        desc.append("- 動態查詢配置\n");
        desc.append("- 多資料源支援\n");
        desc.append("- API Key 認證\n");
        desc.append("- 自動分頁\n");
        return desc.toString();
    }

    private String getApiKeyDescription() {
        StringBuilder desc = new StringBuilder();
        desc.append("## API Key 認證\n\n");
        desc.append("所有 API 請求都需要在 HTTP Header 中包含有效的 API Key。\n\n");
        desc.append("### Header 格式\n");
        desc.append("```\nX-API-Key: your_api_key_here\n```\n\n");
        desc.append("### 測試用 API Key\n");
        desc.append("```\napi_live_1234567890abcdef1234567890abcdef\n```\n");
        return desc.toString();
    }
}