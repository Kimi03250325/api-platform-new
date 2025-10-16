-- ============================================
-- 動態查詢重建腳本（SQL Server 2022）
-- 目的：與 JPA 實體一致（api_query_config、api_query_params、api_datasource_config）
-- 安全性：此腳本會 DROP 並重建相關表，請於測試/開發環境執行
-- 執行順序建議：在 01/02 之後執行
-- ============================================

USE api_db;
GO

-- 1) 先刪除外鍵相依表
IF OBJECT_ID('dbo.api_query_params', 'U') IS NOT NULL
    DROP TABLE dbo.api_query_params;
GO

-- 2) 刪除查詢與資料源表
IF OBJECT_ID('dbo.api_query_config', 'U') IS NOT NULL
    DROP TABLE dbo.api_query_config;
GO

IF OBJECT_ID('dbo.api_datasource_config', 'U') IS NOT NULL
    DROP TABLE dbo.api_datasource_config;
GO

-- 3) 建立 api_datasource_config（對應 JPA 實體 ApiDataSourceConfig）
CREATE TABLE dbo.api_datasource_config (
    datasource_id INT IDENTITY(1,1) PRIMARY KEY,
    datasource_code VARCHAR(50) NOT NULL UNIQUE,
    datasource_name NVARCHAR(100) NOT NULL,
    db_type VARCHAR(20) NOT NULL,
    jdbc_url VARCHAR(500) NOT NULL,
    username VARCHAR(100) NOT NULL,
    password_encrypted VARCHAR(500) NOT NULL,
    driver_class NVARCHAR(200) NULL,
    max_pool_size INT NULL,
    min_idle INT NULL,
    connection_timeout INT NULL,
    is_enabled BIT NOT NULL DEFAULT 1,
    description NVARCHAR(500) NULL,
    created_at DATETIME2(7) NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2(7) NOT NULL DEFAULT GETDATE()
);
GO

CREATE INDEX idx_api_ds_code ON dbo.api_datasource_config(datasource_code);
CREATE INDEX idx_api_ds_enabled ON dbo.api_datasource_config(is_enabled);
GO

-- 4) 建立 api_query_config（對應 JPA 實體 ApiQueryConfig）
CREATE TABLE dbo.api_query_config (
    query_id INT IDENTITY(1,1) PRIMARY KEY,
    query_code VARCHAR(100) NOT NULL UNIQUE,
    query_name NVARCHAR(200) NOT NULL,
    datasource_code VARCHAR(50) NOT NULL,
    query_sql NVARCHAR(MAX) NOT NULL,
    description NVARCHAR(500),
    category VARCHAR(50),
    is_enabled BIT NOT NULL DEFAULT 1,
    require_api_key BIT NOT NULL DEFAULT 1,
    max_page_size INT NULL,
    cache_seconds INT NULL,
    created_by NVARCHAR(50) NULL,
    created_at DATETIME2(7) NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2(7) NOT NULL DEFAULT GETDATE()
);
GO

CREATE INDEX idx_query_config_code ON dbo.api_query_config(query_code);
CREATE INDEX idx_query_config_category ON dbo.api_query_config(category);
CREATE INDEX idx_query_config_enabled ON dbo.api_query_config(is_enabled);
GO

-- 5) 建立 api_query_params（對應 JPA 實體 ApiQueryParam）
CREATE TABLE dbo.api_query_params (
    param_id INT IDENTITY(1,1) PRIMARY KEY,
    query_id INT NOT NULL,
    param_name VARCHAR(100) NOT NULL,
    param_type VARCHAR(20) NOT NULL,
    is_required BIT NOT NULL DEFAULT 0,
    default_value NVARCHAR(200) NULL,
    validation_regex NVARCHAR(500) NULL,
    description NVARCHAR(200) NULL,
    created_at DATETIME2(7) NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_query_params_config FOREIGN KEY (query_id)
        REFERENCES dbo.api_query_config(query_id)
        ON DELETE CASCADE ON UPDATE CASCADE
);
GO

CREATE INDEX idx_query_params_qid ON dbo.api_query_params(query_id);
CREATE INDEX idx_query_params_name ON dbo.api_query_params(param_name);
GO

-- 6) 更新觸發器（updated_at）
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

-- 7) 種子資料：資料源（至少 PRIMARY_DB 與 application.yml 對應）
INSERT INTO dbo.api_datasource_config (
    datasource_code, datasource_name, db_type, jdbc_url, username, password_encrypted, is_enabled, max_pool_size, description
) VALUES (
    'PRIMARY_DB', N'主資料庫', 'SQLSERVER',
    'jdbc:sqlserver://localhost:1433;databaseName=api_db;encrypt=true;trustServerCertificate=true',
    'api_user', 'ENCRYPTED_PASSWORD_HERE', 1, 20, N'主要業務資料庫'
);
GO

-- 8) 種子資料：查詢配置與參數
SET IDENTITY_INSERT dbo.api_query_config ON;
GO

INSERT INTO dbo.api_query_config (
    query_id, query_code, query_name, datasource_code, query_sql, description, category, is_enabled, require_api_key, max_page_size, cache_seconds, created_by
) VALUES
(1, 'LIST_CUSTOMERS', N'查詢客戶列表', 'PRIMARY_DB',
 'SELECT * FROM dbo.customers WHERE account_status = @status ORDER BY created_at DESC',
 N'查詢客戶列表（支援分頁）', 'CUSTOMER', 1, 0, 200, 0, 'SYSTEM'),

(2, 'GET_CUSTOMER_BY_CODE', N'根據客戶代碼查詢', 'PRIMARY_DB',
 'SELECT * FROM dbo.customers WHERE customer_code = @customerCode',
 N'查詢單一客戶資料', 'CUSTOMER', 1, 0, 100, 0, 'SYSTEM'),

(3, 'GET_TRANSACTIONS_BY_PORTFOLIO', N'查詢投資組合交易', 'PRIMARY_DB',
 'SELECT * FROM dbo.transactions WHERE portfolio_id = @portfolioId AND transaction_date >= @startDate AND transaction_date <= @endDate ORDER BY transaction_date DESC',
 N'查詢特定期間交易記錄', 'TRANSACTION', 1, 0, 500, 0, 'SYSTEM');
GO

SET IDENTITY_INSERT dbo.api_query_config OFF;
GO

-- 參數定義
INSERT INTO dbo.api_query_params (query_id, param_name, param_type, is_required, default_value, description) VALUES
-- LIST_CUSTOMERS
(1, 'status', 'STRING', 0, 'ACTIVE', N'帳戶狀態'),
-- GET_CUSTOMER_BY_CODE
(2, 'customerCode', 'STRING', 1, NULL, N'客戶代碼'),
-- GET_TRANSACTIONS_BY_PORTFOLIO
(3, 'portfolioId', 'LONG', 1, NULL, N'投資組合ID'),
(3, 'startDate', 'DATETIME', 1, NULL, N'開始日期 yyyy-MM-dd HH:mm:ss'),
(3, 'endDate', 'DATETIME', 1, NULL, N'結束日期 yyyy-MM-dd HH:mm:ss');
GO

-- 加強驗證（加入正則）
UPDATE dbo.api_query_params SET validation_regex = '^[A-Z0-9]{5}$' WHERE query_id = 2 AND param_name = 'customerCode';
UPDATE dbo.api_query_params SET validation_regex = '^[0-9]+$' WHERE query_id = 3 AND param_name = 'portfolioId';
UPDATE dbo.api_query_params SET validation_regex = '^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$' WHERE query_id = 3 AND param_name IN ('startDate','endDate');
GO

-- 額外預設查詢：依名稱模糊查客戶
INSERT INTO dbo.api_query_config (
    query_code, query_name, datasource_code, query_sql, description, category, is_enabled, require_api_key, max_page_size, cache_seconds, created_by
) VALUES (
    'SEARCH_CUSTOMER_BY_NAME', N'依姓名模糊查詢客戶', 'PRIMARY_DB',
    'SELECT * FROM dbo.customers WHERE name LIKE ''%'' + @name + ''%'' ORDER BY updated_at DESC',
    N'關鍵字搜尋姓名', 'CUSTOMER', 1, 0, 100, 0, 'SYSTEM'
);
GO

DECLARE @qid INT = (SELECT query_id FROM dbo.api_query_config WHERE query_code = 'SEARCH_CUSTOMER_BY_NAME');
INSERT INTO dbo.api_query_params (query_id, param_name, param_type, is_required, default_value, validation_regex, description) VALUES
(@qid, 'name', 'STRING', 1, NULL, N'^.{1,20}$', N'姓名關鍵字(1-20字)');
GO

PRINT N'✅ 動態查詢結構與範例資料重建完成';
GO

