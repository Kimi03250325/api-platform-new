-- ============================================
-- 銀行 API 平台 - SQL Server 2022 資料庫初始化腳本
-- 執行順序：00（第一個執行）
-- 說明：建立資料庫和使用者
-- 執行身份：需要 sysadmin 權限
-- ============================================

USE master;
GO

-- ============================================
-- 1. 建立資料庫
-- ============================================
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'api_db')
BEGIN
    CREATE DATABASE api_db
    ON PRIMARY 
    (
        NAME = api_db_data,
        FILENAME = 'D:\Database\APIDB\api_db_data.mdf',
        SIZE = 10MB,
        MAXSIZE = UNLIMITED,
        FILEGROWTH = 10MB
    )
    LOG ON 
    (
        NAME = api_db_log,
        FILENAME = 'D:\Database\APIDB\api_db_log.ldf',
        SIZE = 10MB,
        MAXSIZE = 2GB,
        FILEGROWTH = 10MB
    )
    COLLATE Chinese_Taiwan_Stroke_CI_AS;
    
    PRINT N'資料庫 api_db 建立成功';
END
ELSE
BEGIN
    PRINT N'資料庫 api_db 已存在';
END
GO

-- ============================================
-- 2. 設定資料庫選項
-- ============================================
ALTER DATABASE api_db SET RECOVERY FULL;
ALTER DATABASE api_db SET AUTO_CLOSE OFF;
ALTER DATABASE api_db SET AUTO_SHRINK OFF;
ALTER DATABASE api_db SET AUTO_CREATE_STATISTICS ON;
ALTER DATABASE api_db SET AUTO_UPDATE_STATISTICS ON;
ALTER DATABASE api_db SET PAGE_VERIFY CHECKSUM;
GO

PRINT N'資料庫選項設定完成';
GO

-- ============================================
-- 3. 建立應用程式登入帳號
-- ============================================
USE master;
GO

IF NOT EXISTS (SELECT name FROM sys.server_principals WHERE name = 'api_user')
BEGIN
    CREATE LOGIN api_user 
    WITH PASSWORD = 'Api@2024!',
         DEFAULT_DATABASE = api_db,
         CHECK_POLICY = ON,
         CHECK_EXPIRATION = OFF;
    
    PRINT N'登入帳號 api_user 建立成功';
END
ELSE
BEGIN
    PRINT N'登入帳號 api_user 已存在';
END
GO

-- ============================================
-- 4. 建立資料庫使用者並授權
-- ============================================
USE api_db;
GO

IF NOT EXISTS (SELECT name FROM sys.database_principals WHERE name = 'api_user')
BEGIN
    CREATE USER api_user FOR LOGIN api_user;
    PRINT N'資料庫使用者 api_user 建立成功';
END
ELSE
BEGIN
    PRINT N'資料庫使用者 api_user 已存在';
END
GO

-- 授予權限
ALTER ROLE db_datareader ADD MEMBER api_user;
ALTER ROLE db_datawriter ADD MEMBER api_user;
ALTER ROLE db_ddladmin ADD MEMBER api_user;
GO

-- 授予額外權限（用於建立表、索引等）
GRANT CREATE TABLE TO api_user;
GRANT CREATE VIEW TO api_user;
GRANT CREATE PROCEDURE TO api_user;
GRANT CREATE FUNCTION TO api_user;
GRANT EXECUTE TO api_user;
GO

PRINT N'使用者權限設定完成';
GO

-- ============================================
-- 5. 建立唯讀使用者（用於報表查詢）
-- ============================================
USE master;
GO

IF NOT EXISTS (SELECT name FROM sys.server_principals WHERE name = 'api_reader')
BEGIN
    CREATE LOGIN api_reader 
    WITH PASSWORD = 'ApiReader@2024!',
         DEFAULT_DATABASE = api_db,
         CHECK_POLICY = ON,
         CHECK_EXPIRATION = OFF;
    
    PRINT N'唯讀登入帳號 api_reader 建立成功';
END
ELSE
BEGIN
    PRINT N'唯讀登入帳號 api_reader 已存在';
END
GO

USE api_db;
GO

IF NOT EXISTS (SELECT name FROM sys.database_principals WHERE name = 'api_reader')
BEGIN
    CREATE USER api_reader FOR LOGIN api_reader;
    PRINT N'唯讀資料庫使用者 api_reader 建立成功';
END
GO

-- 授予唯讀權限
ALTER ROLE db_datareader ADD MEMBER api_reader;
GO

-- ============================================
-- 6. 建立備份使用者
-- ============================================
USE master;
GO

IF NOT EXISTS (SELECT name FROM sys.server_principals WHERE name = 'api_backup')
BEGIN
    CREATE LOGIN api_backup 
    WITH PASSWORD = 'ApiBackup@2024!',
         DEFAULT_DATABASE = api_db,
         CHECK_POLICY = ON,
         CHECK_EXPIRATION = OFF;
    
    PRINT N'備份登入帳號 api_backup 建立成功';
END
GO

USE api_db;
GO

IF NOT EXISTS (SELECT name FROM sys.database_principals WHERE name = 'api_backup')
BEGIN
    CREATE USER api_backup FOR LOGIN api_backup;
    PRINT N'備份資料庫使用者 api_backup 建立成功';
END
GO

-- 授予備份權限
ALTER ROLE db_backupoperator ADD MEMBER api_backup;
GO

-- ============================================
-- 7. 驗證設定
-- ============================================
USE api_db;
GO

PRINT N'';
PRINT N'========================================';
PRINT N'資料庫初始化完成';
PRINT N'========================================';
PRINT N'';

-- 顯示資料庫資訊
SELECT 
    name AS '資料庫名稱',
    state_desc AS '狀態',
    recovery_model_desc AS '復原模式',
    collation_name AS '定序',
    compatibility_level AS '相容層級'
FROM sys.databases 
WHERE name = 'api_db';
GO

-- 顯示使用者資訊
PRINT N'';
PRINT N'使用者清單：';
SELECT 
    dp.name AS '使用者名稱',
    dp.type_desc AS '類型',
    STRING_AGG(drm.role_principal_id, ',') AS '角色'
FROM sys.database_principals dp
LEFT JOIN sys.database_role_members drm ON dp.principal_id = drm.member_principal_id
WHERE dp.type IN ('S', 'U')
    AND dp.name IN ('api_user', 'api_reader', 'api_backup')
GROUP BY dp.name, dp.type_desc;
GO

PRINT N'';
PRINT N'========================================';
PRINT N'下一步：';
PRINT N'1. 執行 01_create_tables_sqlserver.sql';
PRINT N'2. 執行 02_insert_sample_data_sqlserver.sql';
PRINT N'========================================';
GO