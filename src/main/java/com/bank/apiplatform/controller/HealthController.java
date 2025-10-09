package com.bank.apiplatform.controller;

import com.bank.apiplatform.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康檢查控制器
 */
@RestController
@RequestMapping("/api")
@Tag(name = "系統", description = "系統健康檢查 API")
public class HealthController {
    
    /**
     * 基本健康檢查
     */
    @GetMapping("/health")
    @Operation(summary = "健康檢查", description = "檢查系統是否正常運作")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("timestamp", LocalDateTime.now());
        data.put("application", "API Platform");
        data.put("version", "1.0.0");
        
        return ApiResponse.success("系統運作正常", data);
    }
    
    /**
     * 資料庫連線測試
     */
    @GetMapping("/db-test")
    @Operation(summary = "資料庫測試", description = "測試資料庫連線狀態")
    public ApiResponse<Map<String, Object>> databaseTest() {
        Map<String, Object> data = new HashMap<>();
        data.put("database", "MySQL");
        data.put("status", "CONNECTED");
        data.put("timestamp", LocalDateTime.now());
        
        return ApiResponse.success("資料庫連線正常", data);
    }
}