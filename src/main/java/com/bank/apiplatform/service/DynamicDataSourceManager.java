package com.bank.apiplatform.service;

import com.bank.apiplatform.entity.ApiDataSourceConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 動態資料源管理器
 * 負責建立、快取和管理多個資料源的連線池
 */
@Component
@Slf4j
public class DynamicDataSourceManager {
    
    // 資料源連線池快取 (datasourceCode -> DataSource)
    private final Map<String, DataSource> dataSourceCache = new ConcurrentHashMap<>();
    
    /**
     * 取得或建立資料源
     * 
     * @param config 資料源配置
     * @return DataSource 資料源
     */
    public DataSource getDataSource(ApiDataSourceConfig config) {
        String datasourceCode = config.getDatasourceCode();
        
        // 從快取中取得
        if (dataSourceCache.containsKey(datasourceCode)) {
            log.debug("從快取取得資料源: {}", datasourceCode);
            return dataSourceCache.get(datasourceCode);
        }
        
        // 建立新的資料源 (雙重檢查鎖定)
        synchronized (this) {
            if (!dataSourceCache.containsKey(datasourceCode)) {
                DataSource dataSource = createDataSource(config);
                dataSourceCache.put(datasourceCode, dataSource);
                log.info("建立新資料源連線池: {} ({})", datasourceCode, config.getDbType());
            }
        }
        
        return dataSourceCache.get(datasourceCode);
    }
    
    /**
     * 建立 HikariCP 資料源
     */
    private DataSource createDataSource(ApiDataSourceConfig config) {
        HikariConfig hikariConfig = new HikariConfig();
        
        // 基本連線設定
        hikariConfig.setJdbcUrl(config.getJdbcUrl());
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(decryptPassword(config.getPasswordEncrypted()));
        hikariConfig.setDriverClassName(config.getDriverClass());
        
        // 連線池設定
        hikariConfig.setMaximumPoolSize(config.getMaxPoolSize() != null ? config.getMaxPoolSize() : 10);
        hikariConfig.setMinimumIdle(config.getMinIdle() != null ? config.getMinIdle() : 2);
        hikariConfig.setConnectionTimeout(config.getConnectionTimeout() != null ? config.getConnectionTimeout() : 30000);
        hikariConfig.setIdleTimeout(600000);  // 10分鐘
        hikariConfig.setMaxLifetime(1800000); // 30分鐘
        hikariConfig.setPoolName("DynamicPool-" + config.getDatasourceCode());
        
        // 連線測試查詢
        hikariConfig.setConnectionTestQuery(getTestQuery(config.getDbType()));
        
        // 資料庫特定設定
        configureDataSourceProperties(hikariConfig, config.getDbType());
        
        return new HikariDataSource(hikariConfig);
    }
    
    /**
     * 根據資料庫類型設定專屬屬性
     */
    private void configureDataSourceProperties(HikariConfig config, String dbType) {
        switch (dbType.toUpperCase()) {
            case "SQLSERVER":
                config.addDataSourceProperty("applicationIntent", "ReadWrite");
                config.addDataSourceProperty("trustServerCertificate", "true");
                break;
                
            case "MYSQL":
                config.addDataSourceProperty("cachePrepStmts", "true");
                config.addDataSourceProperty("prepStmtCacheSize", "250");
                config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
                config.addDataSourceProperty("useServerPrepStmts", "true");
                break;
                
            case "ORACLE":
                config.addDataSourceProperty("oracle.jdbc.implicitStatementCacheSize", "25");
                break;
                
            case "POSTGRES":
                config.addDataSourceProperty("prepareThreshold", "3");
                break;
        }
    }
    
    /**
     * 取得測試查詢語句
     */
    private String getTestQuery(String dbType) {
        switch (dbType.toUpperCase()) {
            case "MYSQL":
            case "POSTGRES":
                return "SELECT 1";
            case "ORACLE":
                return "SELECT 1 FROM DUAL";
            case "SQLSERVER":
            default:
                return "SELECT 1";
        }
    }
    
    /**
     * 解密密碼
     * TODO: 實作 AES 或其他加密演算法
     */
    private String decryptPassword(String encryptedPassword) {
        // 實際環境需要實作加密解密
        // 這裡暫時直接返回
        return encryptedPassword;
    }
    
    /**
     * 關閉並移除指定資料源
     */
    public void removeDataSource(String datasourceCode) {
        DataSource dataSource = dataSourceCache.remove(datasourceCode);
        if (dataSource instanceof HikariDataSource) {
            ((HikariDataSource) dataSource).close();
            log.info("關閉資料源連線池: {}", datasourceCode);
        }
    }
    
    /**
     * 關閉所有資料源
     */
    public void closeAll() {
        dataSourceCache.forEach((code, ds) -> {
            if (ds instanceof HikariDataSource) {
                ((HikariDataSource) ds).close();
                log.info("關閉資料源: {}", code);
            }
        });
        dataSourceCache.clear();
        log.info("所有資料源連線池已關閉");
    }
    
    /**
     * 取得當前快取的資料源數量
     */
    public int getCachedDataSourceCount() {
        return dataSourceCache.size();
    }
}