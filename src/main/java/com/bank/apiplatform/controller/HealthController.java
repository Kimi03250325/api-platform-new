package com.bank.apiplatform.controller;

import com.bank.apiplatform.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康檢查控制器
 */
@RestController
@RequestMapping("/api")
@Tag(name = "系統", description = "系統健康檢查 API")
@Slf4j
@RequiredArgsConstructor
public class HealthController {
    
    private final DataSource dataSource;
    
    /**
     * 基本健康檢查
     */
    @GetMapping("/health")
    @Operation(summary = "健康檢查", description = "檢查系統是否正常運作")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("timestamp", LocalDateTime.now());
        data.put("application", "銀行 API 平台");
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
        
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            // 取得實際的資料庫資訊
            String databaseProductName = metaData.getDatabaseProductName();
            String databaseProductVersion = metaData.getDatabaseProductVersion();
            String databaseDriverName = metaData.getDriverName();
            String databaseDriverVersion = metaData.getDriverVersion();
            String url = metaData.getURL();
            String userName = metaData.getUserName();
            
            data.put("status", "CONNECTED");
            data.put("database", databaseProductName);  // 實際資料庫名稱
            data.put("version", databaseProductVersion);
            data.put("driver", databaseDriverName);
            data.put("driverVersion", databaseDriverVersion);
            data.put("url", maskPassword(url));  // 遮罩密碼
            data.put("username", userName);
            data.put("timestamp", LocalDateTime.now());
            
            // 測試查詢
            boolean isValid = connection.isValid(5);  // 5 秒超時
            data.put("connectionValid", isValid);
            
            log.info("資料庫連線測試成功: {} {}", databaseProductName, databaseProductVersion);
            
            return ApiResponse.success("資料庫連線正常", data);
            
        } catch (Exception e) {
            log.error("資料庫連線測試失敗", e);
            
            data.put("status", "DISCONNECTED");
            data.put("error", e.getMessage());
            data.put("timestamp", LocalDateTime.now());
            
            return ApiResponse.error(500, "資料庫連線失敗: " + e.getMessage(), data);
        }
    }
    
    /**
     * 資料庫連線池狀態
     */
    @GetMapping("/db-pool")
    @Operation(summary = "連線池狀態", description = "查看資料庫連線池狀態")
    public ApiResponse<Map<String, Object>> connectionPoolStatus() {
        Map<String, Object> data = new HashMap<>();
        
        try {
            // 如果使用 HikariCP，可以取得連線池狀態
            if (dataSource.getClass().getName().contains("HikariDataSource")) {
                com.zaxxer.hikari.HikariDataSource hikariDataSource = 
                    (com.zaxxer.hikari.HikariDataSource) dataSource;
                
                data.put("poolName", hikariDataSource.getPoolName());
                data.put("activeConnections", hikariDataSource.getHikariPoolMXBean().getActiveConnections());
                data.put("idleConnections", hikariDataSource.getHikariPoolMXBean().getIdleConnections());
                data.put("totalConnections", hikariDataSource.getHikariPoolMXBean().getTotalConnections());
                data.put("threadsAwaitingConnection", hikariDataSource.getHikariPoolMXBean().getThreadsAwaitingConnection());
                data.put("maxPoolSize", hikariDataSource.getMaximumPoolSize());
                data.put("minIdle", hikariDataSource.getMinimumIdle());
                
                log.info("連線池狀態查詢成功: {}", hikariDataSource.getPoolName());
            } else {
                data.put("poolType", dataSource.getClass().getName());
                data.put("message", "非 HikariCP 連線池，無法取得詳細資訊");
            }
            
            data.put("timestamp", LocalDateTime.now());
            return ApiResponse.success("連線池狀態查詢成功", data);
            
        } catch (Exception e) {
            log.error("查詢連線池狀態失敗", e);
            data.put("error", e.getMessage());
            return ApiResponse.error(500, "查詢連線池狀態失敗", data);
        }
    }
    
    /**
     * 遮罩 URL 中的密碼
     */
    private String maskPassword(String url) {
        if (url == null) {
            return null;
        }
        // 遮罩可能的密碼參數
        return url.replaceAll("(password|pwd)=([^;]+)", "$1=****");
    }
}