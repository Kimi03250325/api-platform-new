package com.bank.apiplatform.config;

import com.bank.apiplatform.entity.ApiDataSourceConfig;
import com.bank.apiplatform.repository.ApiDataSourceConfigRepository;
import com.bank.apiplatform.service.DynamicDataSourceManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 資料源配置驗證器
 * 在應用啟動時檢查所有已啟用的資料源配置
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSourceValidator implements ApplicationRunner {
    
    private final ApiDataSourceConfigRepository dataSourceConfigRepository;
    private final DynamicDataSourceManager dataSourceManager;
    
    @Override
    public void run(ApplicationArguments args) {
        try {
            validateDataSources();
        } catch (Exception e) {
            log.error("資料源配置驗證過程發生錯誤: {}", e.getMessage(), e);
        }
    }
    
    private void validateDataSources() {
        log.info("==========================================");
        log.info("開始驗證資料源配置...");
        log.info("==========================================");
        
        List<ApiDataSourceConfig> configs = dataSourceConfigRepository.findByIsEnabled(true);
        
        if (configs.isEmpty()) {
            log.warn("⚠️ 沒有找到已啟用的資料源配置");
            return;
        }
        
        log.info("找到 {} 個已啟用的資料源配置", configs.size());
        
        int validCount = 0;
        int warningCount = 0;
        int errorCount = 0;
        
        for (ApiDataSourceConfig config : configs) {
            String datasourceCode = config.getDatasourceCode();
            log.info("----------------------------------------");
            log.info("檢查資料源: {} ({})", datasourceCode, config.getDatasourceName());
            
            try {
                // 驗證配置
                String validationResult = dataSourceManager.validateConfig(config);
                
                if ("驗證通過".equals(validationResult)) {
                    // 檢查是否缺少 driver_class
                    if (config.getDriverClass() == null || config.getDriverClass().trim().isEmpty()) {
                        log.warn("⚠️ 資料源 {} 缺少 driver_class，將自動使用預設值", datasourceCode);
                        warningCount++;
                    } else {
                        log.info("✅ 資料源 {} 配置正常", datasourceCode);
                        validCount++;
                    }
                } else {
                    log.error("❌ 資料源 {} 配置錯誤: {}", datasourceCode, validationResult);
                    errorCount++;
                }
                
                logConfigDetails(config);
                
            } catch (Exception e) {
                log.error("❌ 驗證資料源 {} 時發生錯誤: {}", datasourceCode, e.getMessage());
                errorCount++;
            }
        }
        
        log.info("==========================================");
        log.info("資料源配置驗證完成");
        log.info("✅ 正常: {} 個", validCount);
        log.info("⚠️ 警告: {} 個 (缺少 driver_class，將自動判斷)", warningCount);
        log.info("❌ 錯誤: {} 個", errorCount);
        log.info("==========================================");
        
        if (errorCount > 0) {
            log.error("發現 {} 個資料源配置錯誤，請檢查並修正！", errorCount);
        }
    }
    
    /**
     * 記錄配置詳情
     */
    private void logConfigDetails(ApiDataSourceConfig config) {
        log.info("  - 資料庫類型: {}", config.getDbType());
        log.info("  - JDBC URL: {}", maskSensitiveInfo(config.getJdbcUrl()));
        log.info("  - 使用者名稱: {}", config.getUsername());
        log.info("  - 驅動類別: {}", config.getDriverClass() != null ? 
                 config.getDriverClass() : "未設定(將自動判斷)");
        log.info("  - 最大連線數: {}", config.getMaxPoolSize() != null ? 
                 config.getMaxPoolSize() : "預設(10)");
        log.info("  - 最小閒置連線: {}", config.getMinIdle() != null ? 
                 config.getMinIdle() : "預設(2)");
        log.info("  - 連線逾時: {} ms", config.getConnectionTimeout() != null ? 
                 config.getConnectionTimeout() : "預設(30000)");
    }
    
    /**
     * 遮蔽敏感資訊（密碼）
     */
    private String maskSensitiveInfo(String jdbcUrl) {
        if (jdbcUrl == null) {
            return "null";
        }
        
        // 簡單的密碼遮蔽處理
        return jdbcUrl.replaceAll("password=[^;&]+", "password=***")
                      .replaceAll("pwd=[^;&]+", "pwd=***")
                      .replaceAll("Password=[^;&]+", "Password=***");
    }
}