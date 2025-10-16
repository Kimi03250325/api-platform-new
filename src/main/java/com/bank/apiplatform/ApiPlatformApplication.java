package com.bank.apiplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 銀行 API 平台主程式test
 * 
 * @author Bank Development Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
public class ApiPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiPlatformApplication.class, args);
        
        System.out.println("\n========================================");
        System.out.println("銀行 API 平台啟動成功！");
        System.out.println("========================================");
        System.out.println("API 文件：http://localhost:8080/swagger-ui/index.html");
        System.out.println("健康檢查：http://localhost:8080/actuator/health");
        System.out.println("動態查詢測試工具：http://localhost:8080/tools/query-tool/");
        System.out.println("========================================\n");
    }
}