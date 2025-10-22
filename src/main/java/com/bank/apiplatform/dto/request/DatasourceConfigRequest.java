package com.bank.apiplatform.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 資料源配置請求 DTO
 * 
 * 路徑: src/main/java/com/bank/apiplatform/dto/request/DatasourceConfigRequest.java
 * 
 * 建立方法:
 * 1. 在 Eclipse 右鍵點擊 com.bank.apiplatform.dto.request 套件
 *    (如果沒有 request 套件，先建立: 右鍵 dto > New > Package > 輸入 com.bank.apiplatform.dto.request)
 * 2. New > Class
 * 3. Name: DatasourceConfigRequest
 * 4. Finish
 * 5. 複製此檔案內容，取代原內容
 * 6. Ctrl + S 儲存
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatasourceConfigRequest {
    
    /**
     * 資料源代碼（唯一識別碼）
     * 例: PRIMARY_DB, REPORT_DB, ARCHIVE_DB
     * 規則: 大寫英文字母 + 底線
     */
    @NotBlank(message = "資料源代碼不能為空")
    private String datasourceCode;
    
    /**
     * 資料源名稱
     * 例: 主資料庫, 報表資料庫, 歸檔資料庫
     */
    @NotBlank(message = "資料源名稱不能為空")
    private String datasourceName;
    
    /**
     * JDBC 連線 URL
     * SQL Server 範例: jdbc:sqlserver://localhost:1433;databaseName=api_db;encrypt=true;trustServerCertificate=true
     * MySQL 範例: jdbc:mysql://localhost:3306/api_db?useSSL=false&serverTimezone=UTC
     * Oracle 範例: jdbc:oracle:thin:@localhost:1521:orcl
     * PostgreSQL 範例: jdbc:postgresql://localhost:5432/api_db
     */
    @NotBlank(message = "JDBC URL 不能為空")
    private String jdbcUrl;
    
    /**
     * 資料庫用戶名
     */
    @NotBlank(message = "用戶名不能為空")
    private String username;
    
    /**
     * 資料庫密碼
     */
    @NotBlank(message = "密碼不能為空")
    private String password;
    
    /**
     * JDBC 驅動類別名稱
     * SQL Server: com.microsoft.sqlserver.jdbc.SQLServerDriver
     * MySQL: com.mysql.cj.jdbc.Driver
     * Oracle: oracle.jdbc.OracleDriver
     * PostgreSQL: org.postgresql.Driver
     */
    @NotBlank(message = "驅動類別名稱不能為空")
    private String driverClassName;
    
    /**
     * 是否啟用
     * true: 啟用（可使用）
     * false: 停用（不可使用）
     */
    @NotNull(message = "啟用狀態不能為空")
    private Boolean isEnabled;
    
    /**
     * 資料源描述
     * 例: 銀行 API 平台主資料庫，用於儲存核心業務資料
     */
    private String description;
    
    /**
     * 資料庫類型（可選）
     * 例: SQLSERVER, MYSQL, ORACLE, POSTGRESQL
     */
    private String dbType;
    
    /**
     * 最大連線數（可選）
     * 預設: 10
     */
    private Integer maxPoolSize;
    
    /**
     * 最小閒置連線數（可選）
     * 預設: 2
     */
    private Integer minIdle;
    
    /**
     * 連線超時時間（秒）（可選）
     * 預設: 30
     */
    private Integer connectionTimeout;
}