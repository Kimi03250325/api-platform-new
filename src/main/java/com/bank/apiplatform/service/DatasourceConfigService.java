package com.bank.apiplatform.service;

import com.bank.apiplatform.dto.request.DatasourceConfigRequest;
import com.bank.apiplatform.entity.ApiDataSourceConfig;
import com.bank.apiplatform.repository.ApiDataSourceConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.util.List;

/**
 * 資料源配置服務（修正版）
 * 
 * 修正內容：
 * - Entity 中的密碼欄位名稱是 passwordEncrypted，不是 password
 * 
 * 路徑: src/main/java/com/bank/apiplatform/service/DatasourceConfigService.java
 * 
 * 請用此檔案內容完全取代原有的 DatasourceConfigService.java
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DatasourceConfigService {
    
    private final ApiDataSourceConfigRepository dataSourceConfigRepository;
    
    /**
     * 查詢所有資料源
     */
    public List<ApiDataSourceConfig> findAll() {
        log.debug("查詢所有資料源配置");
        return dataSourceConfigRepository.findAll();
    }
    
    /**
     * 根據 ID 查詢資料源
     */
    public ApiDataSourceConfig findById(Integer id) {
        log.debug("查詢資料源: id={}", id);
        return dataSourceConfigRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("資料源不存在: " + id));
    }
    
    /**
     * 根據代碼查詢資料源
     */
    public ApiDataSourceConfig findByCode(String datasourceCode) {
        log.debug("查詢資料源: datasourceCode={}", datasourceCode);
        return dataSourceConfigRepository.findByDatasourceCodeAndIsEnabled(datasourceCode, true)
            .orElseThrow(() -> new RuntimeException("資料源不存在或已停用: " + datasourceCode));
    }
    
    /**
     * 建立資料源
     */
    @Transactional
    public ApiDataSourceConfig create(DatasourceConfigRequest request) {
        log.info("建立資料源: datasourceCode={}", request.getDatasourceCode());
        
        // 檢查代碼是否已存在
        if (dataSourceConfigRepository.existsByDatasourceCode(request.getDatasourceCode())) {
            log.error("資料源代碼已存在: {}", request.getDatasourceCode());
            throw new RuntimeException("資料源代碼已存在: " + request.getDatasourceCode());
        }
        
        // 建立新的資料源配置
        ApiDataSourceConfig config = new ApiDataSourceConfig();
        copyProperties(request, config);
        
        ApiDataSourceConfig saved = dataSourceConfigRepository.save(config);
        log.info("資料源建立成功: id={}, datasourceCode={}", saved.getDatasourceId(), saved.getDatasourceCode());
        
        return saved;
    }
    
    /**
     * 更新資料源
     */
    @Transactional
    public ApiDataSourceConfig update(Integer id, DatasourceConfigRequest request) {
        log.info("更新資料源: id={}, datasourceCode={}", id, request.getDatasourceCode());
        
        ApiDataSourceConfig config = findById(id);
        
        // 如果修改了代碼，檢查新代碼是否已存在
        if (!config.getDatasourceCode().equals(request.getDatasourceCode())) {
            if (dataSourceConfigRepository.existsByDatasourceCode(request.getDatasourceCode())) {
                log.error("資料源代碼已存在: {}", request.getDatasourceCode());
                throw new RuntimeException("資料源代碼已存在: " + request.getDatasourceCode());
            }
        }
        
        copyProperties(request, config);
        ApiDataSourceConfig updated = dataSourceConfigRepository.save(config);
        
        log.info("資料源更新成功: id={}, datasourceCode={}", updated.getDatasourceId(), updated.getDatasourceCode());
        
        return updated;
    }
    
    /**
     * 刪除資料源
     */
    @Transactional
    public void delete(Integer id) {
        log.info("刪除資料源: id={}", id);
        
        ApiDataSourceConfig config = findById(id);
        String datasourceCode = config.getDatasourceCode();
        
        // TODO: 檢查是否有查詢配置使用此資料源
        
        dataSourceConfigRepository.delete(config);
        log.info("資料源刪除成功: id={}, datasourceCode={}", id, datasourceCode);
    }
    
    /**
     * 測試資料庫連線
     */
    public boolean testConnection(DatasourceConfigRequest request) {
        log.info("測試資料庫連線: jdbcUrl={}", request.getJdbcUrl());
        
        // 驗證必要參數
        if (request.getDriverClassName() == null || request.getDriverClassName().isEmpty()) {
            throw new RuntimeException("缺少驅動類別名稱");
        }
        
        if (request.getJdbcUrl() == null || request.getJdbcUrl().isEmpty()) {
            throw new RuntimeException("缺少 JDBC URL");
        }
        
        if (request.getUsername() == null || request.getUsername().isEmpty()) {
            throw new RuntimeException("缺少用戶名");
        }
        
        // 建立測試用的資料源
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(request.getDriverClassName());
        dataSource.setUrl(request.getJdbcUrl());
        dataSource.setUsername(request.getUsername());
        dataSource.setPassword(request.getPassword());
        
        // 嘗試連線
        try (Connection conn = dataSource.getConnection()) {
            boolean isValid = conn.isValid(5); // 5 秒超時
            
            if (isValid) {
                log.info("✅ 資料庫連線測試成功: jdbcUrl={}", request.getJdbcUrl());
                
                // 記錄資料庫資訊
                String dbProductName = conn.getMetaData().getDatabaseProductName();
                String dbProductVersion = conn.getMetaData().getDatabaseProductVersion();
                log.info("資料庫資訊: {} {}", dbProductName, dbProductVersion);
                
                return true;
            } else {
                log.error("❌ 資料庫連線無效: jdbcUrl={}", request.getJdbcUrl());
                throw new RuntimeException("資料庫連線無效");
            }
            
        } catch (Exception e) {
            log.error("❌ 資料庫連線測試失敗: jdbcUrl={}, 錯誤: {}", 
                     request.getJdbcUrl(), e.getMessage(), e);
            throw new RuntimeException("連線測試失敗: " + e.getMessage(), e);
        }
    }
    
    /**
     * 測試現有資料源連線
     */
    public boolean testExistingConnection(String datasourceCode) {
        log.info("測試現有資料源連線: datasourceCode={}", datasourceCode);
        
        ApiDataSourceConfig config = findByCode(datasourceCode);
        
        // 轉換為請求物件
        DatasourceConfigRequest request = new DatasourceConfigRequest();
        request.setJdbcUrl(config.getJdbcUrl());
        request.setUsername(config.getUsername());
        request.setPassword(config.getPasswordEncrypted()); // ← 修正：使用 passwordEncrypted
        request.setDriverClassName(config.getDriverClass());
        
        return testConnection(request);
    }
    
    /**
     * 複製屬性從 Request 到 Entity
     */
    private void copyProperties(DatasourceConfigRequest request, ApiDataSourceConfig config) {
        config.setDatasourceCode(request.getDatasourceCode());
        config.setDatasourceName(request.getDatasourceName());
        config.setJdbcUrl(request.getJdbcUrl());
        config.setUsername(request.getUsername());
        
        // ← 修正：只在有提供密碼時才更新（使用 passwordEncrypted 欄位）
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            // TODO: 實際環境應該在這裡加密密碼
            // String encrypted = encryptPassword(request.getPassword());
            // config.setPasswordEncrypted(encrypted);
            
            // 暫時直接儲存（實際環境請加密）
            config.setPasswordEncrypted(request.getPassword());
        }
        
        // 設定驅動類別
        config.setDriverClass(request.getDriverClassName());
        
        // 設定啟用狀態
        config.setIsEnabled(request.getIsEnabled() != null ? request.getIsEnabled() : true);
        
        // 設定描述
        config.setDescription(request.getDescription());
        
        // 設定資料庫類型（如果有提供）
        if (request.getDbType() != null && !request.getDbType().isEmpty()) {
            config.setDbType(request.getDbType());
        } else {
            // 根據 JDBC URL 自動判斷資料庫類型
            config.setDbType(detectDbType(request.getJdbcUrl()));
        }
        
        // 設定連線池參數
        if (request.getMaxPoolSize() != null) {
            config.setMaxPoolSize(request.getMaxPoolSize());
        }
        if (request.getMinIdle() != null) {
            config.setMinIdle(request.getMinIdle());
        }
        if (request.getConnectionTimeout() != null) {
            config.setConnectionTimeout(request.getConnectionTimeout());
        }
    }
    
    /**
     * 根據 JDBC URL 自動偵測資料庫類型
     */
    private String detectDbType(String jdbcUrl) {
        if (jdbcUrl == null || jdbcUrl.isEmpty()) {
            return "UNKNOWN";
        }
        
        String url = jdbcUrl.toLowerCase();
        
        if (url.contains(":sqlserver:")) {
            return "SQLSERVER";
        } else if (url.contains(":mysql:")) {
            return "MYSQL";
        } else if (url.contains(":oracle:")) {
            return "ORACLE";
        } else if (url.contains(":postgresql:")) {
            return "POSTGRESQL";
        } else if (url.contains(":mariadb:")) {
            return "MARIADB";
        } else if (url.contains(":h2:")) {
            return "H2";
        } else {
            return "OTHER";
        }
    }
    
    /**
     * 查詢所有已啟用的資料源
     */
    public List<ApiDataSourceConfig> findAllEnabled() {
        log.debug("查詢所有已啟用的資料源");
        return dataSourceConfigRepository.findByIsEnabled(true);
    }
    
    /**
     * 根據資料庫類型查詢資料源
     */
    public List<ApiDataSourceConfig> findByDbType(String dbType) {
        log.debug("查詢資料源: dbType={}", dbType);
        return dataSourceConfigRepository.findByDbType(dbType);
    }
    
    /**
     * 加密密碼（TODO: 實作加密邏輯）
     * 
     * 建議使用 AES 或 RSA 加密
     */
    private String encryptPassword(String plainPassword) {
        // TODO: 實作加密邏輯
        // 範例：使用 AES 加密
        // return AESUtil.encrypt(plainPassword, secretKey);
        
        // 暫時直接返回（請勿在生產環境使用）
        return plainPassword;
    }
    
    /**
     * 解密密碼（TODO: 實作解密邏輯）
     */
    private String decryptPassword(String encryptedPassword) {
        // TODO: 實作解密邏輯
        // 範例：使用 AES 解密
        // return AESUtil.decrypt(encryptedPassword, secretKey);
        
        // 暫時直接返回（請勿在生產環境使用）
        return encryptedPassword;
    }
}