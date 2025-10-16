-- ============================================
-- 動態查詢資料表相容性遷移腳本（非破壞式） SQL Server 2022
-- 目的：將既有結構調整為與 JPA 實體一致，且保留既有資料
-- 包含：api_query_config（欄位調整）、api_query_params（建立）、
--       api_datasource_config（從 datasource_config 相容建立與搬遷）
-- 建議在維護時段執行；如需回滾，請先備份資料
-- ============================================

USE api_db;
GO

-- ============================================
-- 1) api_query_config 結構相容調整
-- ============================================
IF OBJECT_ID('dbo.api_query_config', 'U') IS NOT NULL
BEGIN
    -- 將主鍵欄位由 config_id → query_id（若尚未改名）
    IF COL_LENGTH('dbo.api_query_config', 'query_id') IS NULL AND COL_LENGTH('dbo.api_query_config', 'config_id') IS NOT NULL
    BEGIN
        EXEC sp_rename 'dbo.api_query_config.config_id', 'query_id', 'COLUMN';
    END;

    -- 擴增欄位型別與 NOT NULL 條件
    IF COL_LENGTH('dbo.api_query_config', 'query_code') IS NOT NULL
        ALTER TABLE dbo.api_query_config ALTER COLUMN query_code VARCHAR(100) NOT NULL;

    IF COL_LENGTH('dbo.api_query_config', 'query_name') IS NOT NULL
        ALTER TABLE dbo.api_query_config ALTER COLUMN query_name NVARCHAR(200) NOT NULL;

    -- 確保 datasource_code 為 NOT NULL（若為 NULL，先以空字串暫代）
    IF COL_LENGTH('dbo.api_query_config', 'datasource_code') IS NOT NULL
    BEGIN
        UPDATE dbo.api_query_config SET datasource_code = ISNULL(datasource_code, 'PRIMARY_DB');
        ALTER TABLE dbo.api_query_config ALTER COLUMN datasource_code VARCHAR(50) NOT NULL;
    END;

    -- 移除舊欄位（若存在）
    IF COL_LENGTH('dbo.api_query_config', 'db_type') IS NOT NULL
        ALTER TABLE dbo.api_query_config DROP COLUMN db_type;

    IF COL_LENGTH('dbo.api_query_config', 'param_config') IS NOT NULL
        ALTER TABLE dbo.api_query_config DROP COLUMN param_config;

    -- 新增缺漏欄位
    IF COL_LENGTH('dbo.api_query_config', 'description') IS NULL
        ALTER TABLE dbo.api_query_config ADD description NVARCHAR(500) NULL;

    IF COL_LENGTH('dbo.api_query_config', 'category') IS NULL
        ALTER TABLE dbo.api_query_config ADD category VARCHAR(50) NULL;

    IF COL_LENGTH('dbo.api_query_config', 'is_enabled') IS NULL
        ALTER TABLE dbo.api_query_config ADD is_enabled BIT NOT NULL CONSTRAINT DF_api_query_config_is_enabled DEFAULT(1);

    IF COL_LENGTH('dbo.api_query_config', 'require_api_key') IS NULL
        ALTER TABLE dbo.api_query_config ADD require_api_key BIT NOT NULL CONSTRAINT DF_api_query_config_require DEFAULT(1);

    IF COL_LENGTH('dbo.api_query_config', 'max_page_size') IS NULL
        ALTER TABLE dbo.api_query_config ADD max_page_size INT NULL;

    IF COL_LENGTH('dbo.api_query_config', 'cache_seconds') IS NULL
        ALTER TABLE dbo.api_query_config ADD cache_seconds INT NULL;

    IF COL_LENGTH('dbo.api_query_config', 'created_by') IS NULL
        ALTER TABLE dbo.api_query_config ADD created_by NVARCHAR(50) NULL;

    IF COL_LENGTH('dbo.api_query_config', 'created_at') IS NULL
        ALTER TABLE dbo.api_query_config ADD created_at DATETIME2(7) NOT NULL CONSTRAINT DF_api_query_config_created_at DEFAULT(GETDATE());

    IF COL_LENGTH('dbo.api_query_config', 'updated_at') IS NULL
        ALTER TABLE dbo.api_query_config ADD updated_at DATETIME2(7) NOT NULL CONSTRAINT DF_api_query_config_updated_at DEFAULT(GETDATE());

    -- 索引確保
    IF NOT EXISTS (
        SELECT 1 FROM sys.indexes WHERE name = 'idx_query_config_code' AND object_id = OBJECT_ID('dbo.api_query_config')
    )
        CREATE INDEX idx_query_config_code ON dbo.api_query_config(query_code);

    IF NOT EXISTS (
        SELECT 1 FROM sys.indexes WHERE name = 'idx_query_config_category' AND object_id = OBJECT_ID('dbo.api_query_config')
    )
        CREATE INDEX idx_query_config_category ON dbo.api_query_config(category);

    IF NOT EXISTS (
        SELECT 1 FROM sys.indexes WHERE name = 'idx_query_config_enabled' AND object_id = OBJECT_ID('dbo.api_query_config')
    )
        CREATE INDEX idx_query_config_enabled ON dbo.api_query_config(is_enabled);
END
GO

-- 更新觸發器（updated_at 依 query_id）
IF OBJECT_ID('dbo.trg_api_query_config_update', 'TR') IS NOT NULL
    DROP TRIGGER dbo.trg_api_query_config_update;
GO

CREATE TRIGGER trg_api_query_config_update
ON dbo.api_query_config
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE t SET updated_at = GETDATE()
    FROM dbo.api_query_config t
    INNER JOIN inserted i ON t.query_id = i.query_id;
END;
GO

-- ============================================
-- 2) 建立 api_query_params（若不存在）
-- ============================================
IF OBJECT_ID('dbo.api_query_params', 'U') IS NULL
BEGIN
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

    CREATE INDEX idx_query_params_qid ON dbo.api_query_params(query_id);
    CREATE INDEX idx_query_params_name ON dbo.api_query_params(param_name);
END
GO

-- ============================================
-- 3) 建立並搬遷 datasource_config → api_datasource_config（相容）
-- ============================================
IF OBJECT_ID('dbo.api_datasource_config', 'U') IS NULL
BEGIN
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

    CREATE INDEX idx_api_ds_code ON dbo.api_datasource_config(datasource_code);
    CREATE INDEX idx_api_ds_enabled ON dbo.api_datasource_config(is_enabled);
END
GO

-- 若已存在 api_datasource_config，補齊缺漏欄位（相容新增）
IF OBJECT_ID('dbo.api_datasource_config', 'U') IS NOT NULL
BEGIN
    IF COL_LENGTH('dbo.api_datasource_config', 'driver_class') IS NULL
        ALTER TABLE dbo.api_datasource_config ADD driver_class NVARCHAR(200) NULL;
    IF COL_LENGTH('dbo.api_datasource_config', 'min_idle') IS NULL
        ALTER TABLE dbo.api_datasource_config ADD min_idle INT NULL;
    IF COL_LENGTH('dbo.api_datasource_config', 'connection_timeout') IS NULL
        ALTER TABLE dbo.api_datasource_config ADD connection_timeout INT NULL;
    IF COL_LENGTH('dbo.api_datasource_config', 'description') IS NULL
        ALTER TABLE dbo.api_datasource_config ADD description NVARCHAR(500) NULL;
    -- created_by 欄位相容（若不存在則新增；若為 NOT NULL 改為可為 NULL）
    IF COL_LENGTH('dbo.api_datasource_config', 'created_by') IS NULL
        ALTER TABLE dbo.api_datasource_config ADD created_by NVARCHAR(50) NULL;
    ELSE IF EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
        WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'api_datasource_config' 
          AND COLUMN_NAME = 'created_by' AND IS_NULLABLE = 'NO'
    )
        ALTER TABLE dbo.api_datasource_config ALTER COLUMN created_by NVARCHAR(50) NULL;
    -- updated_by 欄位相容
    IF COL_LENGTH('dbo.api_datasource_config', 'updated_by') IS NULL
        ALTER TABLE dbo.api_datasource_config ADD updated_by NVARCHAR(50) NULL;
    ELSE IF EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
        WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'api_datasource_config' 
          AND COLUMN_NAME = 'updated_by' AND IS_NULLABLE = 'NO'
    )
        ALTER TABLE dbo.api_datasource_config ALTER COLUMN updated_by NVARCHAR(50) NULL;
END
GO

-- 將既有 datasource_config 的資料搬到 api_datasource_config（若來源存在）
IF OBJECT_ID('dbo.datasource_config', 'U') IS NOT NULL
BEGIN
    INSERT INTO dbo.api_datasource_config (
        datasource_code, datasource_name, db_type, jdbc_url, username, password_encrypted,
        driver_class, max_pool_size, min_idle, connection_timeout, is_enabled, description, created_by, updated_by
    )
    SELECT 
        d.datasource_code,
        d.datasource_name,
        ISNULL(d.db_type, 'SQLSERVER'),
        ISNULL(d.jdbc_url, ''),
        ISNULL(d.username, ''),
        ISNULL(d.password_encrypted, ''),
        NULL,               -- driver_class 無法從舊表取得
        d.max_pool_size,
        NULL,               -- min_idle 無法從舊表取得
        NULL,               -- connection_timeout 無法從舊表取得
        ISNULL(d.is_enabled, 1),
        d.description,
        'SYSTEM',
        'SYSTEM'
    FROM dbo.datasource_config d
    WHERE NOT EXISTS (
        SELECT 1 FROM dbo.api_datasource_config t WHERE t.datasource_code = d.datasource_code
    );
END
GO

-- 觸發器：api_datasource_config.updated_at
IF OBJECT_ID('dbo.trg_api_datasource_config_update', 'TR') IS NOT NULL
    DROP TRIGGER dbo.trg_api_datasource_config_update;
GO

CREATE TRIGGER trg_api_datasource_config_update
ON dbo.api_datasource_config
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE t SET updated_at = GETDATE()
    FROM dbo.api_datasource_config t
    INNER JOIN inserted i ON t.datasource_id = i.datasource_id;
END;
GO

-- ============================================
-- 4) 驗證摘要
-- ============================================
PRINT N'=== 驗證摘要 ===';
SELECT 'api_query_config' AS [table], COUNT(*) AS [rows] FROM dbo.api_query_config
UNION ALL
SELECT 'api_query_params', COUNT(*) FROM sys.objects WHERE name = 'api_query_params' AND type = 'U'
UNION ALL
SELECT 'api_datasource_config', COUNT(*) FROM dbo.api_datasource_config;
GO

PRINT N'✅ 動態查詢相容性遷移完成';
GO

