package com.bank.apiplatform.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI 配置
 */
@Configuration
public class SwaggerConfig {
    
    private static final String SECURITY_SCHEME_NAME = "X-API-Key";
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // API 基本資訊
                .info(new Info()
                        .title("銀行 API 平台")
                        .version("1.0.0")
                        .description("""
                                # 財富管理系統 API 介接平台
                                
                                ## 認證方式
                                所有 API（除白名單外）都需要在 HTTP Header 中提供 API Key：
                                - Header 名稱：`X-API-Key`
                                - 格式：`api_xxxxx...`
                                
                                ## 如何取得 API Key
                                1. 呼叫 `POST /api/admin/api-keys/generate` 產生新的 API Key
                                2. 複製回應中的 `apiKey` 值
                                3. 點擊右上角 🔓 Authorize 按鈕，貼上 API Key
                                
                                ## 白名單路徑（不需要 API Key）
                                - `/actuator/health`
                                - `/swagger-ui/**`
                                - `/v3/api-docs/**`
                                - `/api/health`
                                - `/api/db-test`
                                - `/api/admin/api-keys/**`
                                
                                ## 流量限制
                                每個 API Key 預設每分鐘最多 1000 次請求
                                """)
                        .contact(new Contact()
                                .name("銀行開發團隊")
                                .email("api-support@bank.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                
                // 伺服器清單
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("開發環境"),
                        new Server()
                                .url("https://api-dev.bank.com")
                                .description("測試環境"),
                        new Server()
                                .url("https://api.bank.com")
                                .description("正式環境")
                ))
                
                // 安全性配置
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-API-Key")
                                .description("請在此輸入您的 API Key（格式：api_xxxxx...）\n\n" +
                                           "💡 提示：先呼叫 'POST /api/admin/api-keys/generate' 產生 API Key")))
                
                // 全域套用安全需求
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
}