package com.bank.apiplatform.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

/**
 * 資料庫連線配置
 * 
 * 支援多環境配置：dev, test, prod
 */
@Configuration
@Slf4j
public class DatabaseConfig {
    
    @Value("${spring.datasource.url}")
    private String jdbcUrl;
    
    @Value("${spring.datasource.username}")
    private String username;
    
    @Value("${spring.datasource.password}")
    private String password;
    
    @Value("${spring.datasource.hikari.maximum-pool-size:20}")
    private int maximumPoolSize;
    
    @Value("${spring.datasource.hikari.minimum-idle:5}")
    private int minimumIdle;
    
    /**
     * 開發環境資料源配置
     */
    @Bean
    @Profile("dev")
    public DataSource devDataSource() {
        log.info("初始化開發環境資料庫連線池");
        return createDataSource("ApiHikariPool-Dev");
    }
    
    /**
     * 測試環境資料源配置
     */
    @Bean
    @Profile("test")
    public DataSource testDataSource() {
        log.info("初始化測試環境資料庫連線池");
        return createDataSource("ApiHikariPool-Test");
    }
    
    /**
     * 生產環境資料源配置（主從讀寫分離）
     */
    @Bean
    @Profile("prod")
    public DataSource prodDataSource() {
        log.info("初始化生產環境資料庫連線池");
        HikariConfig config = createHikariConfig("ApiHikariPool-Prod");
        
        // 生產環境特殊配置
        config.setMaximumPoolSize(50);
        config.setMinimumIdle(10);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        
        // SQL Server 生產環境優化
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("applicationIntent", "ReadWrite");
        config.addDataSourceProperty("multiSubnetFailover", "true");
        
        HikariDataSource dataSource = new HikariDataSource(config);
        log.info("生產環境資料庫連線池初始化完成");
        
        return dataSource;
    }
    
    /**
     * 建立資料源
     */
    private DataSource createDataSource(String poolName) {
        HikariConfig config = createHikariConfig(poolName);
        return new HikariDataSource(config);
    }
    
    /**
     * 建立 HikariCP 配置
     */
    private HikariConfig createHikariConfig(String poolName) {
        HikariConfig config = new HikariConfig();
        
        // 基本連線設定
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        
        // 連線池設定
        config.setPoolName(poolName);
        config.setMaximumPoolSize(maximumPoolSize);
        config.setMinimumIdle(minimumIdle);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setConnectionTestQuery("SELECT 1");
        
        // SQL Server 優化設定
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        
        // 連線洩漏偵測（開發/測試環境）
        if (!poolName.contains("Prod")) {
            config.setLeakDetectionThreshold(60000);
        }
        
        log.info("HikariCP 配置完成：{}", poolName);
        log.debug("JDBC URL: {}", jdbcUrl);
        log.debug("最大連線數: {}", maximumPoolSize);
        log.debug("最小閒置連線數: {}", minimumIdle);
        
        return config;
    }
}