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
        
        // 🔧 修正: 如果 driver_class 為空，根據 db_type 自動判斷
        String driverClass = config.getDriverClass();
        if (driverClass == null || driverClass.trim().isEmpty()) {
            driverClass = getDefaultDriverClass(config.getDbType());
            log.warn("資料源 {} 的 driver_class 為空，自動使用預設值: {}", 
                     config.getDatasourceCode(), driverClass);
        }
        hikariConfig.setDriverClassName(driverClass);
        
        // 連線池設定
        hikariConfig.setMaximumPoolSize(config.getMaxPoolSize() != null ? config.getMaxPoolSize() : 10);
        hikariConfig.setMinimumIdle(config.getMinIdle() != null ? config.getMinIdle() : 2);
        hikariConfig.setConnectionTimeout(config.getConnectionTimeout() != null ? 
                                         config.getConnectionTimeout() : 30000);
        hikariConfig.setIdleTimeout(600000);  // 10分鐘
        hikariConfig.setMaxLifetime(1800000); // 30分鐘
        hikariConfig.setPoolName("DynamicPool-" + config.getDatasourceCode());
        
        // 連線測試查詢
        hikariConfig.setConnectionTestQuery(getTestQuery(config.getDbType()));
        
        // 資料庫特定設定
        configureDataSourceProperties(hikariConfig, config.getDbType());
        
        log.info("✅ 資料源配置完成: {} - Driver: {}", config.getDatasourceCode(), driverClass);
        
        return new HikariDataSource(hikariConfig);
    }
    
    /**
     * 根據資料庫類型取得預設驅動類別
     * 
     * @param dbType 資料庫類型
     * @return 驅動類別完整名稱
     */
    private String getDefaultDriverClass(String dbType) {
        if (dbType == null || dbType.trim().isEmpty()) {
            throw new IllegalArgumentException("資料庫類型不能為空");
        }
        
        String upperDbType = dbType.trim().toUpperCase();
        
        switch (upperDbType) {
            case "MYSQL":
                return "com.mysql.cj.jdbc.Driver";
                
            case "SQLSERVER":
            case "MSSQL":
                return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
                
            case "ORACLE":
                return "oracle.jdbc.OracleDriver";
                
            case "POSTGRES":
            case "POSTGRESQL":
                return "org.postgresql.Driver";
                
            case "DB2":
                return "com.ibm.db2.jcc.DB2Driver";
                
            case "H2":
                return "org.h2.Driver";
                
            case "MARIADB":
                return "org.mariadb.jdbc.Driver";
                
            default:
                String errorMsg = String.format(
                    "不支援的資料庫類型: %s。支援的類型: MYSQL, SQLSERVER, ORACLE, POSTGRES, DB2, H2, MARIADB", 
                    dbType
                );
                log.error(errorMsg);
                throw new IllegalArgumentException(errorMsg);
        }
    }
    
    /**
     * 根據資料庫類型設定專屬屬性
     */
    private void configureDataSourceProperties(HikariConfig config, String dbType) {
        if (dbType == null) {
            return;
        }
        
        switch (dbType.toUpperCase()) {
            case "SQLSERVER":
            case "MSSQL":
                config.addDataSourceProperty("applicationIntent", "ReadWrite");
                config.addDataSourceProperty("trustServerCertificate", "true");
                config.addDataSourceProperty("encrypt", "false");
                break;
                
            case "MYSQL":
                config.addDataSourceProperty("cachePrepStmts", "true");
                config.addDataSourceProperty("prepStmtCacheSize", "250");
                config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
                config.addDataSourceProperty("useServerPrepStmts", "true");
                config.addDataSourceProperty("useLocalSessionState", "true");
                config.addDataSourceProperty("rewriteBatchedStatements", "true");
                config.addDataSourceProperty("cacheResultSetMetadata", "true");
                config.addDataSourceProperty("cacheServerConfiguration", "true");
                config.addDataSourceProperty("maintainTimeStats", "false");
                break;
                
            case "ORACLE":
                config.addDataSourceProperty("oracle.jdbc.implicitStatementCacheSize", "25");
                config.addDataSourceProperty("oracle.jdbc.defaultRowPrefetch", "20");
                break;
                
            case "POSTGRES":
            case "POSTGRESQL":
                config.addDataSourceProperty("prepareThreshold", "3");
                config.addDataSourceProperty("preparedStatementCacheQueries", "256");
                config.addDataSourceProperty("preparedStatementCacheSizeMiB", "5");
                break;
                
            case "MARIADB":
                config.addDataSourceProperty("cachePrepStmts", "true");
                config.addDataSourceProperty("prepStmtCacheSize", "250");
                break;
        }
    }
    
    /**
     * 取得測試查詢語句
     */
    private String getTestQuery(String dbType) {
        if (dbType == null) {
            return "SELECT 1";
        }
        
        switch (dbType.toUpperCase()) {
            case "MYSQL":
            case "POSTGRES":
            case "POSTGRESQL":
            case "MARIADB":
            case "H2":
                return "SELECT 1";
                
            case "ORACLE":
                return "SELECT 1 FROM DUAL";
                
            case "SQLSERVER":
            case "MSSQL":
            case "DB2":
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
    
    /**
     * 驗證資料源配置的有效性
     * 
     * @param config 資料源配置
     * @return 驗證結果訊息
     */
    public String validateConfig(ApiDataSourceConfig config) {
        StringBuilder errors = new StringBuilder();
        
        if (config.getDatasourceCode() == null || config.getDatasourceCode().trim().isEmpty()) {
            errors.append("資料源代碼不能為空; ");
        }
        
        if (config.getDbType() == null || config.getDbType().trim().isEmpty()) {
            errors.append("資料庫類型不能為空; ");
        } else {
            try {
                getDefaultDriverClass(config.getDbType());
            } catch (IllegalArgumentException e) {
                errors.append(e.getMessage()).append("; ");
            }
        }
        
        if (config.getJdbcUrl() == null || config.getJdbcUrl().trim().isEmpty()) {
            errors.append("JDBC URL 不能為空; ");
        }
        
        if (config.getUsername() == null || config.getUsername().trim().isEmpty()) {
            errors.append("使用者名稱不能為空; ");
        }
        
        return errors.length() == 0 ? "驗證通過" : errors.toString();
    }
}