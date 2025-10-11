-- ============================================
-- 銀行 API 平台 - SQL Server 2022 資料表建立腳本
-- 執行順序：01
-- 日期：2025-01-10
-- ============================================

USE api_db;
GO

-- ============================================
-- 1. API Keys 管理表
-- ============================================
IF OBJECT_ID('dbo.api_keys', 'U') IS NOT NULL
    DROP TABLE dbo.api_keys;
GO

CREATE TABLE dbo.api_keys (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    app_name NVARCHAR(100) NOT NULL,
    api_key VARCHAR(64) NOT NULL UNIQUE,
    api_secret VARCHAR(128) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    ip_whitelist NVARCHAR(MAX),  -- JSON 格式
    rate_limit INT NOT NULL DEFAULT 1000,
    description NVARCHAR(500),
    created_by NVARCHAR(50),
    created_at DATETIME2(7) NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2(7) NOT NULL DEFAULT GETDATE(),
    expired_at DATETIME2(7),
    last_used_at DATETIME2(7)
);
GO

-- 建立索引
CREATE INDEX idx_api_keys_status ON dbo.api_keys(status);
CREATE INDEX idx_api_keys_expired ON dbo.api_keys(expired_at);
GO

-- 建立註解（使用擴充屬性）
EXEC sys.sp_addextendedproperty 
    @name=N'MS_Description', 
    @value=N'API Key 管理表', 
    @level0type=N'SCHEMA', @level0name=N'dbo',
    @level1type=N'TABLE', @level1name=N'api_keys';
GO

-- ============================================
-- 2. 客戶資料表
-- ============================================
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
    risk_level VARCHAR(20),
    account_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME2(7) NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2(7) NOT NULL DEFAULT GETDATE()
);
GO

-- 建立索引
CREATE INDEX idx_customers_code ON dbo.customers(customer_code);
CREATE INDEX idx_customers_status ON dbo.customers(account_status);
CREATE INDEX idx_customers_name ON dbo.customers(name);
GO

EXEC sys.sp_addextendedproperty 
    @name=N'MS_Description', 
    @value=N'客戶資料表', 
    @level0type=N'SCHEMA', @level0name=N'dbo',
    @level1type=N'TABLE', @level1name=N'customers';
GO

-- ============================================
-- 3. 投資組合表
-- ============================================
IF OBJECT_ID('dbo.portfolios', 'U') IS NOT NULL
    DROP TABLE dbo.portfolios;
GO

CREATE TABLE dbo.portfolios (
    portfolio_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    portfolio_code VARCHAR(50) NOT NULL UNIQUE,
    portfolio_name NVARCHAR(100),
    total_value DECIMAL(18, 2),
    currency VARCHAR(3) NOT NULL DEFAULT 'TWD',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME2(7) NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2(7) NOT NULL DEFAULT GETDATE(),
    
    CONSTRAINT fk_portfolios_customer 
        FOREIGN KEY (customer_id) REFERENCES dbo.customers(customer_id)
        ON DELETE NO ACTION ON UPDATE CASCADE
);
GO

-- 建立索引
CREATE INDEX idx_portfolios_customer ON dbo.portfolios(customer_id);
CREATE INDEX idx_portfolios_code ON dbo.portfolios(portfolio_code);
CREATE INDEX idx_portfolios_status ON dbo.portfolios(status);
GO

EXEC sys.sp_addextendedproperty 
    @name=N'MS_Description', 
    @value=N'投資組合表', 
    @level0type=N'SCHEMA', @level0name=N'dbo',
    @level1type=N'TABLE', @level1name=N'portfolios';
GO

-- ============================================
-- 4. 交易記錄表
-- ============================================
IF OBJECT_ID('dbo.transactions', 'U') IS NOT NULL
    DROP TABLE dbo.transactions;
GO

CREATE TABLE dbo.transactions (
    transaction_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    portfolio_id BIGINT NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    instrument_code VARCHAR(50),
    quantity DECIMAL(18, 4),
    price DECIMAL(18, 2),
    amount DECIMAL(18, 2),
    currency VARCHAR(3),
    transaction_date DATETIME2(7) NOT NULL,
    settlement_date DATE,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME2(7) NOT NULL DEFAULT GETDATE(),
    
    CONSTRAINT fk_transactions_portfolio 
        FOREIGN KEY (portfolio_id) REFERENCES dbo.portfolios(portfolio_id)
        ON DELETE NO ACTION ON UPDATE CASCADE
);
GO

-- 建立索引
CREATE INDEX idx_transactions_portfolio ON dbo.transactions(portfolio_id);
CREATE INDEX idx_transactions_date ON dbo.transactions(transaction_date);
CREATE INDEX idx_transactions_status ON dbo.transactions(status);
CREATE INDEX idx_transactions_type ON dbo.transactions(transaction_type);
GO

EXEC sys.sp_addextendedproperty 
    @name=N'MS_Description', 
    @value=N'交易記錄表', 
    @level0type=N'SCHEMA', @level0name=N'dbo',
    @level1type=N'TABLE', @level1name=N'transactions';
GO

-- ============================================
-- 5. API 訪問日誌表（分區表建議）
-- ============================================
IF OBJECT_ID('dbo.api_access_log', 'U') IS NOT NULL
    DROP TABLE dbo.api_access_log;
GO

CREATE TABLE dbo.api_access_log (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    api_key VARCHAR(64),
    app_name NVARCHAR(100),
    endpoint NVARCHAR(255),
    method VARCHAR(10),
    status_code INT,
    response_time INT,
    ip_address VARCHAR(45),
    user_agent NVARCHAR(500),
    error_message NVARCHAR(MAX),
    created_at DATETIME2(7) NOT NULL DEFAULT GETDATE()
);
GO

-- 建立索引
CREATE INDEX idx_api_log_key ON dbo.api_access_log(api_key);
CREATE INDEX idx_api_log_time ON dbo.api_access_log(created_at);
CREATE INDEX idx_api_log_endpoint ON dbo.api_access_log(endpoint);
GO

-- ============================================
-- 6. 外部系統介接日誌表
-- ============================================
IF OBJECT_ID('dbo.external_system_log', 'U') IS NOT NULL
    DROP TABLE dbo.external_system_log;
GO

CREATE TABLE dbo.external_system_log (
    log_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    system_name NVARCHAR(50) NOT NULL,
    operation NVARCHAR(100),
    request_data NVARCHAR(MAX),
    response_data NVARCHAR(MAX),
    status VARCHAR(20),
    error_message NVARCHAR(MAX),
    execution_time INT,
    created_at DATETIME2(7) NOT NULL DEFAULT GETDATE()
);
GO

-- 建立索引
CREATE INDEX idx_external_log_system ON dbo.external_system_log(system_name);
CREATE INDEX idx_external_log_time ON dbo.external_system_log(created_at);
CREATE INDEX idx_external_log_status ON dbo.external_system_log(status);
GO

-- ============================================
-- 7. 系統參數表
-- ============================================
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

-- ============================================
-- 建立更新時間觸發器（自動更新 updated_at）
-- ============================================

-- api_keys 觸發器
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

-- system_parameters 觸發器
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

-- ============================================
-- 驗證資料表建立
-- ============================================
SELECT 
    t.name AS '資料表名稱',
    SUM(p.rows) AS '資料筆數',
    CAST(ep.value AS NVARCHAR(500)) AS '說明'
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