-- ============================================
-- 銀行 API 平台 - SQL Server 2022 完整建表腳本
-- 版本: v2.0
-- 執行順序: 01
-- 更新日期: 2025-10-21
-- 說明: 包含所有業務表與動態查詢表
-- ============================================

USE api_db;
GO

PRINT N'開始建立資料表...';
PRINT N'';
GO

-- ============================================
-- 1. API Keys 管理表
-- ============================================
PRINT N'[1/11] 建立 api_keys 資料表...';
GO

IF OBJECT_ID('dbo.api_keys', 'U') IS NOT NULL
    DROP TABLE dbo.api_keys;
GO

CREATE TABLE dbo.api_keys (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    app_name NVARCHAR(100) NOT NULL,
    api_key VARCHAR(64) NOT NULL UNIQUE,
    api_secret VARCHAR(128) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE, INACTIVE, SUSPENDED
    ip_whitelist NVARCHAR(MAX),  -- JSON 格式儲存 IP 白名單
    rate_limit INT NOT NULL DEFAULT 1000,
    description NVARCHAR(500),
    created_by NVARCHAR(50),
    created_at DATETIME2(7) NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2(7) NOT NULL DEFAULT GETDATE(),
    expired_at DATETIME2(7),
    last_used_at DATETIME2(7),
    CONSTRAINT CK_api_keys_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED'))
);
GO

CREATE INDEX idx_api_keys_status ON dbo.api_keys(status);
CREATE INDEX idx_api_keys_expired ON dbo.api_keys(expired_at);
GO

EXEC sys.sp_addextendedproperty 
    @name=N'MS_Description', 
    @value=N'API Key 管理表 - 管理所有 API 金鑰與權限', 
    @level0type=N'SCHEMA', @level0name=N'dbo',
    @level1type=N'TABLE', @level1name=N'api_keys';
GO

-- ============================================
-- 2. 客戶資料表
-- ============================================
PRINT N'[2/11] 建立 customers 資料表...';
GO

IF OBJECT_ID('dbo.customers', 'U') IS NOT NULL
    DROP TABLE dbo.customers;
GO

CREATE TABLE dbo.customers (
    customer_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    customer_code VARCHAR(50) NOT NULL UNIQUE,
    name NVARCHAR(100) NOT NULL,
    id_number VARCHAR(20),
    email VARCHAR(100),
    phone VARCHAR(20),
    risk_level VARCHAR(20),  -- LOW, MEDIUM, HIGH
    account_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE, INACTIVE, SUSPENDED
    created_at DATETIME2(7) NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2(7) NOT NULL DEFAULT GETDATE(),
    CONSTRAINT CK_customers_risk CHECK (risk_level IN ('LOW', 'MEDIUM', 'HIGH')),
    CONSTRAINT CK_customers_status CHECK (account_status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED'))
);
GO

CREATE INDEX idx_customers_code ON dbo.customers(customer_code);
CREATE INDEX idx_customers_status ON dbo.customers(account_status);
CREATE INDEX idx_customers_name ON dbo.customers(name);
CREATE INDEX idx_customers_email ON dbo.customers(email);
GO

EXEC sys.sp_addextendedproperty 
    @name=N'MS_Description', 
    @value=N'客戶資料表 - 儲存客戶基本資料', 
    @level0type=N'SCHEMA', @level0name=N'dbo',
    @level1type=N'TABLE', @level1name=N'customers';
GO

-- ============================================
-- 3. 投資組合表
-- ============================================
PRINT N'[3/11] 建立 portfolios 資料表...';
GO

IF OBJECT_ID('dbo.portfolios', 'U') IS NOT NULL
    DROP TABLE dbo.portfolios;
GO

CREATE TABLE dbo.portfolios (
    portfolio_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    portfolio_code VARCHAR(50) NOT NULL UNIQUE,
    portfolio_name NVARCHAR(200) NOT NULL,
    total_value DECIMAL(18,2) NOT NULL DEFAULT 0,
    currency VARCHAR(3) NOT NULL DEFAULT 'TWD',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME2(7) NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2(7) NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_portfolios_customer FOREIGN KEY (customer_id) 
        REFERENCES dbo.customers(customer_id) ON DELETE CASCADE,
    CONSTRAINT CK_portfolios_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'CLOSED'))
);
GO

CREATE INDEX idx_portfolios_customer ON dbo.portfolios(customer_id);
CREATE INDEX idx_portfolios_code ON dbo.portfolios(portfolio_code);
CREATE INDEX idx_portfolios_status ON dbo.portfolios(status);
GO

EXEC sys.sp_addextendedproperty 
    @name=N'MS_Description', 
    @value=N'投資組合表 - 儲存客戶投資組合資料', 
    @level0type=N'SCHEMA', @level0name=N'dbo',
    @level1type=N'TABLE', @level1name=N'portfolios';
GO

-- ============================================
-- 4. 交易記錄表
-- ============================================
PRINT N'[4/11] 建立 transactions 資料表...';
GO

IF OBJECT_ID('dbo.transactions', 'U') IS NOT NULL
    DROP TABLE dbo.transactions;
GO

CREATE TABLE dbo.transactions (
    transaction_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    portfolio_id BIGINT NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,  -- BUY, SELL, DIVIDEND, FEE
    instrument_code VARCHAR(50) NOT NULL,
    instrument_name NVARCHAR(200),
    quantity DECIMAL(18,4) NOT NULL,
    price DECIMAL(18,4) NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'TWD',
    transaction_date DATETIME2(7) NOT NULL,
    settlement_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    notes NVARCHAR(500),
    created_at DATETIME2(7) NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2(7) NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_transactions_portfolio FOREIGN KEY (portfolio_id) 
        REFERENCES dbo.portfolios(portfolio_id) ON DELETE CASCADE,
    CONSTRAINT CK_transactions_type CHECK (transaction_type IN ('BUY', 'SELL', 'DIVIDEND', 'FEE', 'TRANSFER')),
    CONSTRAINT CK_transactions_status CHECK (status IN ('PENDING', 'COMPLETED', 'CANCELLED', 'FAILED'))
);
GO

CREATE INDEX idx_transactions_portfolio ON dbo.transactions(portfolio_id);
CREATE INDEX idx_transactions_date ON dbo.transactions(transaction_date);
CREATE INDEX idx_transactions_type ON dbo.transactions(transaction_type);
CREATE INDEX idx_transactions_status ON dbo.transactions(status);
GO

EXEC sys.sp_addextendedproperty 
    @name=N'MS_Description', 
    @value=N'交易記錄表 - 儲存所有投資交易記錄', 
    @level0type=N'SCHEMA', @level0name=N'dbo',
    @level1type=N'TABLE', @level1name=N'transactions';
GO

-- ============================================
-- 5. API 訪問日誌表
-- ============================================
PRINT N'[5/11] 建立 api_access_log 資料表...';
GO

IF OBJECT_ID('dbo.api_access_log', 'U') IS NOT NULL
    DROP TABLE dbo.api_access_log;
GO

CREATE TABLE dbo.api_access_log (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    api_key VARCHAR(64),
    app_name NVARCHAR(100),
    endpoint VARCHAR(255),
    method VARCHAR(10),
    status_code INT,
    response_time INT,  -- 毫秒
    ip_address VARCHAR(45),
    user_agent NVARCHAR(500),
    request_body NVARCHAR(MAX),
    response_body NVARCHAR(MAX),
    error_message NVARCHAR(MAX),
    created_at DATETIME2(7) NOT NULL DEFAULT GETDATE()
);
GO

CREATE INDEX idx_api_log_key ON dbo.api_access_log(api_key);
CREATE INDEX idx_api_log_time ON dbo.api_access_log(created_at);
CREATE INDEX idx_api_log_endpoint ON dbo.api_access_log(endpoint);
CREATE INDEX idx_api_log_status ON dbo.api_access_log(status_code);
GO

EXEC sys.sp_addextendedproperty 
    @name=N'MS_Description', 
    @value=N'API 訪問日誌表 - 記錄所有 API 請求', 
    @level0type=N'SCHEMA', @level0name=N'dbo',
    @level1type=N'TABLE', @level1name=N'api_access_log';
GO

-- ============================================
-- 6. 外部系統介接日誌表
-- ============================================
PRINT N'[6/11] 建立 external_system_log 資料表...';
GO

IF OBJECT_ID('dbo.external_system_log', 'U') IS NOT NULL
    DROP TABLE dbo.external_system_log;
GO

CREATE TABLE dbo.external_system_log (
    log_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    system_name VARCHAR(50) NOT NULL,
    operation VARCHAR(100),
    request_data NVARCHAR(MAX),
    response_data NVARCHAR(MAX),
    status VARCHAR(20),  -- SUCCESS, FAILED, TIMEOUT
    error_message NVARCHAR(MAX),
    execution_time INT,  -- 毫秒
    created_at DATETIME2(7) NOT NULL DEFAULT GETDATE(),
    CONSTRAINT CK_external_log_status CHECK (status IN ('SUCCESS', 'FAILED', 'TIMEOUT'))
);
GO

CREATE INDEX idx_external_log_system ON dbo.external_system_log(system_name);
CREATE INDEX idx_external_log_time ON dbo.external_system_log(created_at);
CREATE INDEX idx_external_log_status ON dbo.external_system_log(status);
GO

EXEC sys.sp_addextendedproperty 
    @name=N'MS_Description', 
    @value=N'外部系統介接日誌表 - 記錄與外部系統的通訊', 
    @level0type=N'SCHEMA', @level0name=N'dbo',
    @level1type=N'TABLE', @level1name=N'external_system_log';
GO

-- ============================================
-- 7. 系統參數表
-- ============================================
PRINT N'[7/11] 建立 system_parameters 資料表...';
GO

IF OBJECT_ID('dbo.system_parameters', 'U') IS NOT NULL
    DROP TABLE dbo.system_parameters;
GO

CREATE TABLE dbo.system_parameters (
    param_id INT IDENTITY(1,1) PRIMARY KEY,
    param_key VARCHAR(100) NOT NULL UNIQUE,
    param_value NVARCHAR(MAX),
    description NVARCHAR(500),
    created_at DATETIME2(7) NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2(7) NOT NULL DEFAULT GETDATE()
);
GO

EXEC sys.sp_addextendedproperty 
    @name=N'MS_Description', 
    @value=N'系統參數表 - 儲存系統配置參數', 
    @level0type=N'SCHEMA', @level0name=N'dbo',
    @level1type=N'TABLE', @level1name=N'system_parameters';
GO

-- ============================================
-- 8. 資料來源配置表
-- ============================================
PRINT N'[8/11] 建立 api_datasource_config 資料表...';
GO

IF OBJECT_ID('dbo.api_datasource_config', 'U') IS NOT NULL
    DROP TABLE dbo.api_datasource_config;
GO

CREATE TABLE dbo.api_datasource_config (
    datasource_id INT IDENTITY(1,1) PRIMARY KEY,
    datasource_code VARCHAR(50) NOT NULL UNIQUE,
    datasource_name NVARCHAR(100) NOT NULL,
    db_type VARCHAR(20) NOT NULL,  -- SQLSERVER, MYSQL, POSTGRESQL, ORACLE
    jdbc_url VARCHAR(500) NOT NULL,
    username VARCHAR(100) NOT NULL,
    password_encrypted VARCHAR(500) NOT NULL,
    driver_class NVARCHAR(200) NULL,
    max_pool_size INT NULL DEFAULT 20,
    min_idle INT NULL DEFAULT 5,
    connection_timeout INT NULL DEFAULT 30000,
    is_enabled BIT NOT NULL DEFAULT 1,
    description NVARCHAR(500) NULL,
    created_at DATETIME2(7) NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2(7) NOT NULL DEFAULT GETDATE(),
    CONSTRAINT CK_datasource_dbtype CHECK (db_type IN ('SQLSERVER', 'MYSQL', 'POSTGRESQL', 'ORACLE'))
);
GO

CREATE INDEX idx_api_ds_code ON dbo.api_datasource_config(datasource_code);
CREATE INDEX idx_api_ds_enabled ON dbo.api_datasource_config(is_enabled);
GO

EXEC sys.sp_addextendedproperty 
    @name=N'MS_Description', 
    @value=N'資料來源配置表 - 動態資料庫連線配置', 
    @level0type=N'SCHEMA', @level0name=N'dbo',
    @level1type=N'TABLE', @level1name=N'api_datasource_config';
GO

-- ============================================
-- 9. API 查詢配置表
-- ============================================
PRINT N'[9/11] 建立 api_query_config 資料表...';
GO

IF OBJECT_ID('dbo.api_query_config', 'U') IS NOT NULL
    DROP TABLE dbo.api_query_config;
GO

CREATE TABLE dbo.api_query_config (
    query_id INT IDENTITY(1,1) PRIMARY KEY,
    query_code VARCHAR(100) NOT NULL UNIQUE,
    query_name NVARCHAR(200) NOT NULL,
    datasource_code VARCHAR(50) NOT NULL,
    query_sql NVARCHAR(MAX) NOT NULL,
    description NVARCHAR(500),
    category VARCHAR(50),  -- CUSTOMER, PORTFOLIO, TRANSACTION, REPORT
    is_enabled BIT NOT NULL DEFAULT 1,
    require_api_key BIT NOT NULL DEFAULT 1,
    max_page_size INT NULL DEFAULT 100,
    cache_seconds INT NULL DEFAULT 0,
    created_by NVARCHAR(50) NULL,
    created_at DATETIME2(7) NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2(7) NOT NULL DEFAULT GETDATE()
);
GO

CREATE INDEX idx_query_config_code ON dbo.api_query_config(query_code);
CREATE INDEX idx_query_config_category ON dbo.api_query_config(category);
CREATE INDEX idx_query_config_enabled ON dbo.api_query_config(is_enabled);
GO

EXEC sys.sp_addextendedproperty 
    @name=N'MS_Description', 
    @value=N'API 查詢配置表 - 動態 SQL 查詢配置', 
    @level0type=N'SCHEMA', @level0name=N'dbo',
    @level1type=N'TABLE', @level1name=N'api_query_config';
GO

-- ============================================
-- 10. API 查詢參數定義表
-- ============================================
PRINT N'[10/11] 建立 api_query_params 資料表...';
GO

IF OBJECT_ID('dbo.api_query_params', 'U') IS NOT NULL
    DROP TABLE dbo.api_query_params;
GO

CREATE TABLE dbo.api_query_params (
    param_id INT IDENTITY(1,1) PRIMARY KEY,
    query_id INT NOT NULL,
    param_name VARCHAR(100) NOT NULL,
    param_type VARCHAR(20) NOT NULL,  -- STRING, INTEGER, LONG, DECIMAL, DATETIME, BOOLEAN
    is_required BIT NOT NULL DEFAULT 0,
    default_value NVARCHAR(200) NULL,
    validation_regex NVARCHAR(500) NULL,
    description NVARCHAR(200) NULL,
    created_at DATETIME2(7) NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_query_params_config FOREIGN KEY (query_id)
        REFERENCES dbo.api_query_config(query_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT CK_param_type CHECK (param_type IN ('STRING', 'INTEGER', 'LONG', 'DECIMAL', 'DATETIME', 'DATE', 'BOOLEAN'))
);
GO

CREATE INDEX idx_query_params_qid ON dbo.api_query_params(query_id);
CREATE INDEX idx_query_params_name ON dbo.api_query_params(param_name);
GO

EXEC sys.sp_addextendedproperty 
    @name=N'MS_Description', 
    @value=N'API 查詢參數定義表 - 參數名稱/型別/必填等', 
    @level0type=N'SCHEMA', @level0name=N'dbo',
    @level1type=N'TABLE', @level1name=N'api_query_params';
GO

-- ============================================
-- 11. 舊版相容表 (datasource_config) - 僅供遷移用
-- ============================================
PRINT N'[11/11] 建立 datasource_config 相容表...';
GO

IF OBJECT_ID('dbo.datasource_config', 'U') IS NOT NULL
    DROP TABLE dbo.datasource_config;
GO

CREATE TABLE dbo.datasource_config (
    datasource_id INT IDENTITY(1,1) PRIMARY KEY,
    datasource_code VARCHAR(50) NOT NULL UNIQUE,
    datasource_name NVARCHAR(100) NOT NULL,
    db_type VARCHAR(20) NOT NULL,
    jdbc_url VARCHAR(500) NOT NULL,
    username VARCHAR(100) NOT NULL,
    password_encrypted VARCHAR(500) NOT NULL,
    max_pool_size INT NULL,
    is_enabled BIT NOT NULL DEFAULT 1,
    description NVARCHAR(500) NULL,
    created_at DATETIME2(7) NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2(7) NOT NULL DEFAULT GETDATE()
);
GO

EXEC sys.sp_addextendedproperty 
    @name=N'MS_Description', 
    @value=N'資料來源配置表(舊版) - 僅供相容性保留，請使用 api_datasource_config', 
    @level0type=N'SCHEMA', @level0name=N'dbo',
    @level1type=N'TABLE', @level1name=N'datasource_config';
GO

-- ============================================
-- 建立更新時間觸發器
-- ============================================
PRINT N'';
PRINT N'建立觸發器...';
GO

-- api_keys 觸發器
IF OBJECT_ID('dbo.trg_api_keys_update', 'TR') IS NOT NULL
    DROP TRIGGER dbo.trg_api_keys_update;
GO

CREATE TRIGGER trg_api_keys_update
ON dbo.api_keys
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE dbo.api_keys
    SET updated_at = GETDATE()
    FROM dbo.api_keys t
    INNER JOIN inserted i ON t.id = i.id;
END;
GO

-- customers 觸發器
IF OBJECT_ID('dbo.trg_customers_update', 'TR') IS NOT NULL
    DROP TRIGGER dbo.trg_customers_update;
GO

CREATE TRIGGER trg_customers_update
ON dbo.customers
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE dbo.customers
    SET updated_at = GETDATE()
    FROM dbo.customers t
    INNER JOIN inserted i ON t.customer_id = i.customer_id;
END;
GO

-- portfolios 觸發器
IF OBJECT_ID('dbo.trg_portfolios_update', 'TR') IS NOT NULL
    DROP TRIGGER dbo.trg_portfolios_update;
GO

CREATE TRIGGER trg_portfolios_update
ON dbo.portfolios
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE dbo.portfolios
    SET updated_at = GETDATE()
    FROM dbo.portfolios t
    INNER JOIN inserted i ON t.portfolio_id = i.portfolio_id;
END;
GO

-- transactions 觸發器
IF OBJECT_ID('dbo.trg_transactions_update', 'TR') IS NOT NULL
    DROP TRIGGER dbo.trg_transactions_update;
GO

CREATE TRIGGER trg_transactions_update
ON dbo.transactions
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE dbo.transactions
    SET updated_at = GETDATE()
    FROM dbo.transactions t
    INNER JOIN inserted i ON t.transaction_id = i.transaction_id;
END;
GO

-- system_parameters 觸發器
IF OBJECT_ID('dbo.trg_system_parameters_update', 'TR') IS NOT NULL
    DROP TRIGGER dbo.trg_system_parameters_update;
GO

CREATE TRIGGER trg_system_parameters_update
ON dbo.system_parameters
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE dbo.system_parameters
    SET updated_at = GETDATE()
    FROM dbo.system_parameters t
    INNER JOIN inserted i ON t.param_id = i.param_id;
END;
GO

-- api_query_config 觸發器
IF OBJECT_ID('dbo.trg_api_query_config_update', 'TR') IS NOT NULL
    DROP TRIGGER dbo.trg_api_query_config_update;
GO

CREATE TRIGGER trg_api_query_config_update
ON dbo.api_query_config
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE dbo.api_query_config
    SET updated_at = GETDATE()
    FROM dbo.api_query_config t
    INNER JOIN inserted i ON t.query_id = i.query_id;
END;
GO

-- api_datasource_config 觸發器
IF OBJECT_ID('dbo.trg_api_datasource_config_update', 'TR') IS NOT NULL
    DROP TRIGGER dbo.trg_api_datasource_config_update;
GO

CREATE TRIGGER trg_api_datasource_config_update
ON dbo.api_datasource_config
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE dbo.api_datasource_config
    SET updated_at = GETDATE()
    FROM dbo.api_datasource_config t
    INNER JOIN inserted i ON t.datasource_id = i.datasource_id;
END;
GO

-- datasource_config 觸發器 (舊版相容)
IF OBJECT_ID('dbo.trg_datasource_config_update', 'TR') IS NOT NULL
    DROP TRIGGER dbo.trg_datasource_config_update;
GO

CREATE TRIGGER trg_datasource_config_update
ON dbo.datasource_config
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE dbo.datasource_config
    SET updated_at = GETDATE()
    FROM dbo.datasource_config t
    INNER JOIN inserted i ON t.datasource_id = i.datasource_id;
END;
GO

-- ============================================
-- 驗證資料表建立
-- ============================================
PRINT N'';
PRINT N'========================================';
PRINT N'✅ 資料表建立完成！';
PRINT N'========================================';
PRINT N'';

SELECT 
    t.name AS [資料表名稱],
    SUM(p.rows) AS [資料筆數],
    CAST(ep.value AS NVARCHAR(500)) AS [說明]
FROM sys.tables t
LEFT JOIN sys.partitions p ON t.object_id = p.object_id
LEFT JOIN sys.extended_properties ep 
    ON t.object_id = ep.major_id 
    AND ep.minor_id = 0 
    AND ep.name = 'MS_Description'
WHERE t.schema_id = SCHEMA_ID('dbo')
    AND p.index_id IN (0, 1)
GROUP BY t.name, ep.value
ORDER BY t.name;
GO

PRINT N'';
PRINT N'========================================';
PRINT N'📋 資料表清單:';
PRINT N'  1. api_keys - API金鑰管理';
PRINT N'  2. customers - 客戶資料';
PRINT N'  3. portfolios - 投資組合';
PRINT N'  4. transactions - 交易記錄';
PRINT N'  5. api_access_log - API訪問日誌';
PRINT N'  6. external_system_log - 外部系統日誌';
PRINT N'  7. system_parameters - 系統參數';
PRINT N'  8. api_datasource_config - 資料源配置(新)';
PRINT N'  9. api_query_config - 查詢配置';
PRINT N'  10. api_query_params - 查詢參數';
PRINT N'  11. datasource_config - 資料源配置(舊)';
PRINT N'========================================';
PRINT N'';
PRINT N'✅ 建表腳本執行完畢！';
PRINT N'👉 下一步: 執行 02_insert_sample_data_sqlserver.sql';
GO