-- ============================================
-- 銀行 API 平台 - 動態查詢配置表 (SQL Server 2022)
-- 執行順序: 03
-- 日期: 2025-01-15
-- ============================================

USE api_db;
GO

-- ============================================
-- 1. 資料源配置表
-- ============================================
IF OBJECT_ID('dbo.api_datasource_config', 'U') IS NOT NULL
    DROP TABLE dbo.api_datasource_config;
GO

CREATE TABLE dbo.api_datasource_config (
    datasource_id INT IDENTITY(1,1) PRIMARY KEY,
    datasource_code VARCHAR(50) NOT NULL UNIQUE,
    datasource_name NVARCHAR(100) NOT NULL,
    db_type VARCHAR(20) NOT NULL,                    -- SQLSERVER/MYSQL/ORACLE/POSTGRES
    jdbc_url VARCHAR(500) NOT NULL,
    username VARCHAR(100) NOT NULL,
    password_encrypted VARCHAR(500) NOT NULL,        -- 加密後的密碼
    driver_class VARCHAR(200),
    max_pool_size INT DEFAULT 10,
    min_idle INT DEFAULT 2,
    connection_timeout INT DEFAULT 30000,            -- 毫秒
    is_enabled BIT NOT NULL DEFAULT 1,
    description NVARCHAR(500),
    created_at DATETIME2(7) NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2(7) NOT NULL DEFAULT GETDATE()
);
GO

CREATE INDEX idx_datasource_code ON dbo.api_datasource_config(datasource_code);
CREATE INDEX idx_is_enabled ON dbo.api_datasource_config(is_enabled);
GO

-- ============================================
-- 2. 查詢配置主表
-- ============================================
IF OBJECT_ID('dbo.api_query_config', 'U') IS NOT NULL
    DROP TABLE dbo.api_query_config;
GO

CREATE TABLE dbo.api_query_config (
    query_id INT IDENTITY(1,1) PRIMARY KEY,
    query_code VARCHAR(100) NOT NULL UNIQUE,
    query_name NVARCHAR(200) NOT NULL,
    datasource_code VARCHAR(50) NOT NULL,
    query_sql NVARCHAR(MAX) NOT NULL,                -- SQL查詢語句 (使用 :paramName 佔位符)
    description NVARCHAR(500),
    category VARCHAR(50),                            -- 分類 (CUSTOMER/PORTFOLIO/TRANSACTION)
    is_enabled BIT NOT NULL DEFAULT 1,
    require_api_key BIT NOT NULL DEFAULT 1,
    max_page_size INT DEFAULT 100,
    cache_seconds INT DEFAULT 0,                     -- 快取秒數 (0=不快取)
    created_by VARCHAR(50),
    created_at DATETIME2(7) NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2(7) NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_query_datasource FOREIGN KEY (datasource_code) 
        REFERENCES dbo.api_datasource_config(datasource_code)
);
GO

CREATE INDEX idx_query_code ON dbo.api_query_config(query_code);
CREATE INDEX idx_datasource_code ON dbo.api_query_config(datasource_code);
CREATE INDEX idx_category ON dbo.api_query_config(category);
CREATE INDEX idx_is_enabled ON dbo.api_query_config(is_enabled);
GO

-- ============================================
-- 3. 查詢參數定義表
-- ============================================
IF OBJECT_ID('dbo.api_query_params', 'U') IS NOT NULL
    DROP TABLE dbo.api_query_params;
GO

CREATE TABLE dbo.api_query_params (
    param_id INT IDENTITY(1,1) PRIMARY KEY,
    query_id INT NOT NULL,
    param_name VARCHAR(100) NOT NULL,
    param_type VARCHAR(20) NOT NULL,                 -- STRING/INT/LONG/DECIMAL/DATE/DATETIME
    is_required BIT NOT NULL DEFAULT 0,
    default_value NVARCHAR(200),
    validation_regex VARCHAR(500),
    description NVARCHAR(200),
    created_at DATETIME2(7) NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_params_query FOREIGN KEY (query_id) 
        REFERENCES dbo.api_query_config(query_id) ON DELETE CASCADE
);
GO

CREATE INDEX idx_query_params ON dbo.api_query_params(query_id);
GO

-- ============================================
-- 建立更新時間觸發器
-- ============================================

-- 資料源配置表觸發器
IF OBJECT_ID('trg_datasource_config_update', 'TR') IS NOT NULL
    DROP TRIGGER trg_datasource_config_update;
GO

CREATE TRIGGER trg_datasource_config_update
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

-- 查詢配置表觸發器
IF OBJECT_ID('trg_query_config_update', 'TR') IS NOT NULL
    DROP TRIGGER trg_query_config_update;
GO

CREATE TRIGGER trg_query_config_update
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

-- ============================================
-- 驗證資料表建立
-- ============================================
SELECT 
    t.name AS '資料表名稱',
    SUM(p.rows) AS '資料筆數'
FROM sys.tables t
LEFT JOIN sys.partitions p ON t.object_id = p.object_id
WHERE t.schema_id = SCHEMA_ID('dbo')
    AND t.name IN ('api_datasource_config', 'api_query_config', 'api_query_params')
    AND p.index_id IN (0, 1)
GROUP BY t.name
ORDER BY t.name;
GO