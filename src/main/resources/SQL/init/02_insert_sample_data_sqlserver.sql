-- ============================================
-- 銀行 API 平台 - SQL Server 2022 測試資料腳本
-- 版本: v2.1 (修正參數格式)
-- 執行順序: 02
-- 更新日期: 2025-10-21
-- 說明: 包含完整的測試資料與動態查詢配置
-- 重要: 所有 SQL 參數使用 :paramName 格式（JDBC 標準）
-- ============================================

USE api_db;
GO

PRINT N'開始插入測試資料...';
PRINT N'';
GO

-- ============================================
-- 1. 插入測試 API Keys
-- ============================================
PRINT N'[1/10] 插入 API Keys 測試資料...';
GO

SET IDENTITY_INSERT dbo.api_keys ON;
GO

INSERT INTO dbo.api_keys (
    id, app_name, api_key, api_secret, 
    rate_limit, description, created_by, status
) VALUES 
(1, N'財富管理系統', 
 'api_live_1234567890abcdef1234567890abcdef',
 'secret_0987654321fedcba0987654321fedcba0987654321fedcba0987654321fedcba',
 5000, N'生產環境主系統', 'SYSTEM', 'ACTIVE'),
 
(2, N'行動銀行APP',
 'api_mobile_abcdef1234567890abcdef1234567890',
 'secret_fedcba0987654321fedcba0987654321fedcba0987654321fedcba0987654321',
 3000, N'行動銀行應用程式', 'SYSTEM', 'ACTIVE'),
 
(3, N'網路銀行',
 'api_web_xyz123xyz123xyz123xyz123xyz123xyz1',
 'secret_xyz123xyz123xyz123xyz123xyz123xyz123xyz123xyz123xyz123xyz123xyz1',
 2000, N'網路銀行系統', 'SYSTEM', 'ACTIVE'),
 
(4, N'測試系統',
 'api_test_1111111111111111111111111111111111',
 'secret_1111111111111111111111111111111111111111111111111111111111111111',
 1000, N'開發測試用', 'SYSTEM', 'ACTIVE'),
 
(5, N'第三方合作夥伴',
 'api_partner_abcd1234abcd1234abcd1234abcd12',
 'secret_abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234',
 500, N'外部合作夥伴系統', 'SYSTEM', 'ACTIVE');
GO

SET IDENTITY_INSERT dbo.api_keys OFF;
GO

PRINT N'✅ API Keys: 5 筆資料插入完成';
GO

-- ============================================
-- 2. 插入測試客戶資料
-- ============================================
PRINT N'[2/10] 插入客戶測試資料...';
GO

SET IDENTITY_INSERT dbo.customers ON;
GO

INSERT INTO dbo.customers (
    customer_id, customer_code, name, id_number,
    email, phone, risk_level, account_status
) VALUES 
(1, 'C0001', N'張三', 'A123456789', 'zhang.san@example.com', '0912345678', 'MEDIUM', 'ACTIVE'),
(2, 'C0002', N'李四', 'B987654321', 'li.si@example.com', '0923456789', 'LOW', 'ACTIVE'),
(3, 'C0003', N'王五', 'C246813579', 'wang.wu@example.com', '0934567890', 'HIGH', 'ACTIVE'),
(4, 'C0004', N'趙六', 'D135792468', 'zhao.liu@example.com', '0945678901', 'MEDIUM', 'ACTIVE'),
(5, 'C0005', N'錢七', 'E864297531', 'qian.qi@example.com', '0956789012', 'LOW', 'ACTIVE'),
(6, 'C0006', N'孫八', 'F147258369', 'sun.ba@example.com', '0967890123', 'HIGH', 'ACTIVE'),
(7, 'C0007', N'周九', 'G369258147', 'zhou.jiu@example.com', '0978901234', 'MEDIUM', 'ACTIVE'),
(8, 'C0008', N'吳十', 'H258369147', 'wu.shi@example.com', '0989012345', 'LOW', 'ACTIVE'),
(9, 'C0009', N'鄭一一', 'I159753468', 'zheng.yi@example.com', '0990123456', 'MEDIUM', 'ACTIVE'),
(10, 'C0010', N'陳一二', 'J753159486', 'chen.er@example.com', '0901234567', 'HIGH', 'ACTIVE');
GO

SET IDENTITY_INSERT dbo.customers OFF;
GO

PRINT N'✅ Customers: 10 筆資料插入完成';
GO

-- ============================================
-- 3. 插入測試投資組合
-- ============================================
PRINT N'[3/10] 插入投資組合測試資料...';
GO

SET IDENTITY_INSERT dbo.portfolios ON;
GO

INSERT INTO dbo.portfolios (
    portfolio_id, customer_id, portfolio_code, 
    portfolio_name, total_value, currency, status
) VALUES 
(1, 1, 'P0001', N'張三成長型投資組合', 1500000.00, 'TWD', 'ACTIVE'),
(2, 1, 'P0002', N'張三穩健型投資組合', 800000.00, 'TWD', 'ACTIVE'),
(3, 2, 'P0003', N'李四退休準備組合', 2500000.00, 'TWD', 'ACTIVE'),
(4, 3, 'P0004', N'王五積極型組合', 5000000.00, 'TWD', 'ACTIVE'),
(5, 4, 'P0005', N'趙六平衡型組合', 1800000.00, 'TWD', 'ACTIVE'),
(6, 5, 'P0006', N'錢七保守型組合', 1200000.00, 'TWD', 'ACTIVE'),
(7, 6, 'P0007', N'孫八高收益組合', 3500000.00, 'TWD', 'ACTIVE'),
(8, 7, 'P0008', N'周九多元資產組合', 2200000.00, 'TWD', 'ACTIVE'),
(9, 8, 'P0009', N'吳十ESG永續組合', 1600000.00, 'TWD', 'ACTIVE'),
(10, 9, 'P0010', N'鄭一一全球配置組合', 2800000.00, 'TWD', 'ACTIVE'),
(11, 10, 'P0011', N'陳一二科技成長組合', 4200000.00, 'TWD', 'ACTIVE'),
(12, 3, 'P0012', N'王五海外債券組合', 1500000.00, 'USD', 'ACTIVE'),
(13, 7, 'P0013', N'周九黃金避險組合', 900000.00, 'TWD', 'ACTIVE'),
(14, 10, 'P0014', N'陳一二加密貨幣組合', 500000.00, 'USD', 'ACTIVE'),
(15, 1, 'P0015', N'張三美元定存組合', 300000.00, 'USD', 'ACTIVE');
GO

SET IDENTITY_INSERT dbo.portfolios OFF;
GO

PRINT N'✅ Portfolios: 15 筆資料插入完成';
GO

-- ============================================
-- 4. 插入測試交易記錄
-- ============================================
PRINT N'[4/10] 插入交易記錄測試資料...';
GO

SET IDENTITY_INSERT dbo.transactions ON;
GO

INSERT INTO dbo.transactions (
    transaction_id, portfolio_id, transaction_type, 
    instrument_code, instrument_name, quantity, price, amount,
    currency, transaction_date, settlement_date, status, notes
) VALUES 
-- 台股交易
(1, 1, 'BUY', '2330.TW', N'台積電', 100.0000, 580.00, 58000.00, 
 'TWD', '2025-10-01 09:00:00', '2025-10-03', 'COMPLETED', N'開盤買進'),
(2, 1, 'BUY', '0050.TW', N'元大台灣50', 50.0000, 145.00, 7250.00, 
 'TWD', '2025-10-01 10:30:00', '2025-10-03', 'COMPLETED', NULL),
(3, 2, 'BUY', '0056.TW', N'元大高股息', 200.0000, 35.50, 7100.00, 
 'TWD', '2025-10-02 14:15:00', '2025-10-04', 'COMPLETED', NULL),
(4, 3, 'BUY', '2454.TW', N'聯發科', 150.0000, 1050.00, 157500.00, 
 'TWD', '2025-10-02 11:00:00', '2025-10-04', 'COMPLETED', N'科技股佈局'),
(5, 4, 'BUY', '2317.TW', N'鴻海', 300.0000, 108.50, 32550.00, 
 'TWD', '2025-10-03 09:30:00', '2025-10-07', 'COMPLETED', NULL),

-- 基金交易
(6, 5, 'BUY', 'FUND001', N'富蘭克林科技基金', 5000.0000, 25.30, 126500.00, 
 'TWD', '2025-10-03 15:00:00', '2025-10-05', 'COMPLETED', N'定期定額'),
(7, 6, 'BUY', 'FUND002', N'貝萊德世界能源基金', 3000.0000, 45.80, 137400.00, 
 'TWD', '2025-10-04 10:00:00', '2025-10-06', 'COMPLETED', NULL),
(8, 7, 'BUY', 'FUND003', N'摩根新興市場債券基金', 2000.0000, 68.50, 137000.00, 
 'TWD', '2025-10-04 15:00:00', '2025-10-06', 'COMPLETED', NULL),

-- 股利收入
(9, 1, 'DIVIDEND', '2330.TW', N'台積電', 100.0000, 3.00, 300.00, 
 'TWD', '2025-10-05 00:00:00', '2025-10-05', 'COMPLETED', N'現金股利'),
(10, 4, 'DIVIDEND', '2317.TW', N'鴻海', 300.0000, 4.50, 1350.00, 
 'TWD', '2025-10-05 00:00:00', '2025-10-05', 'COMPLETED', N'現金股利'),

-- 賣出交易
(11, 2, 'SELL', '0056.TW', N'元大高股息', 100.0000, 36.00, 3600.00, 
 'TWD', '2025-10-06 13:30:00', '2025-10-10', 'COMPLETED', N'部分獲利了結'),
(12, 3, 'SELL', '2454.TW', N'聯發科', 50.0000, 1080.00, 54000.00, 
 'TWD', '2025-10-07 10:00:00', '2025-10-11', 'COMPLETED', N'調整部位'),

-- 手續費
(13, 1, 'FEE', 'TRADE_FEE', N'交易手續費', 1.0000, 29.00, 29.00, 
 'TWD', '2025-10-01 09:00:00', '2025-10-01', 'COMPLETED', N'買進手續費'),
(14, 2, 'FEE', 'TRADE_FEE', N'交易手續費', 1.0000, 18.00, 18.00, 
 'TWD', '2025-10-06 13:30:00', '2025-10-06', 'COMPLETED', N'賣出手續費'),

-- 待處理交易
(15, 8, 'BUY', '2412.TW', N'中華電', 200.0000, 123.50, 24700.00, 
 'TWD', '2025-10-08 14:00:00', '2025-10-10', 'PENDING', N'待確認');
GO

SET IDENTITY_INSERT dbo.transactions OFF;
GO

PRINT N'✅ Transactions: 15 筆資料插入完成';
GO

-- ============================================
-- 5. 插入系統參數
-- ============================================
PRINT N'[5/10] 插入系統參數...';
GO

INSERT INTO dbo.system_parameters (param_key, param_value, description) 
VALUES 
('API_VERSION', '1.0.0', N'API 版本號'),
('MAX_REQUEST_SIZE', '10485760', N'最大請求大小（10MB）'),
('SESSION_TIMEOUT', '1800', N'Session 逾時秒數（30分鐘）'),
('ENABLE_CACHE', 'true', N'是否啟用快取機制'),
('CACHE_TTL', '300', N'快取存活時間（秒）'),
('DEFAULT_RATE_LIMIT', '1000', N'預設流量限制（每分鐘）'),
('LOG_RETENTION_DAYS', '90', N'日誌保留天數'),
('MAX_QUERY_TIMEOUT', '30', N'查詢最大執行時間（秒）'),
('ENABLE_SQL_LOG', 'true', N'是否記錄 SQL 執行日誌'),
('DEFAULT_PAGE_SIZE', '20', N'預設分頁大小'),
('MAX_PAGE_SIZE', '100', N'最大分頁大小'),
('ENABLE_IP_WHITELIST', 'true', N'是否啟用 IP 白名單'),
('JWT_SECRET', 'bank_api_jwt_secret_key_2024', N'JWT 密鑰'),
('JWT_EXPIRATION', '3600', N'JWT 過期時間（秒）'),
('ENCRYPTION_ALGORITHM', 'AES-256-GCM', N'加密演算法');
GO

PRINT N'✅ System Parameters: 15 筆資料插入完成';
GO

-- ============================================
-- 6. 插入資料來源配置 (新版 api_datasource_config)
-- ============================================
PRINT N'[6/10] 插入資料來源配置...';
GO

INSERT INTO dbo.api_datasource_config (
    datasource_code, datasource_name, db_type, 
    jdbc_url, username, password_encrypted, 
    is_enabled, max_pool_size, min_idle, description
) VALUES 
('PRIMARY_DB', N'主資料庫', 'SQLSERVER',
 'jdbc:sqlserver://localhost:1433;databaseName=api_db;encrypt=true;trustServerCertificate=true',
 'api_user', 'ENCRYPTED_PASSWORD_HERE', 1, 20, 5, N'主要業務資料庫'),

('REPORT_DB', N'報表資料庫', 'SQLSERVER',
 'jdbc:sqlserver://localhost:1433;databaseName=report_db;encrypt=true;trustServerCertificate=true',
 'report_user', 'ENCRYPTED_PASSWORD_HERE', 1, 10, 3, N'報表專用資料庫'),

('ARCHIVE_DB', N'歷史資料庫', 'SQLSERVER',
 'jdbc:sqlserver://archive-server:1433;databaseName=archive_db;encrypt=true;trustServerCertificate=true',
 'archive_user', 'ENCRYPTED_PASSWORD_HERE', 1, 5, 2, N'歷史資料歸檔庫'),

('EXTERNAL_MYSQL', N'外部MySQL系統', 'MYSQL',
 'jdbc:mysql://external-host:3306/external_db?useSSL=false&serverTimezone=Asia/Taipei',
 'external_user', 'ENCRYPTED_PASSWORD_HERE', 0, 10, 3, N'外部合作夥伴MySQL資料庫');
GO

PRINT N'✅ API Datasource Config: 4 筆資料插入完成';
GO

-- ============================================
-- 7. 插入資料來源配置 (舊版 datasource_config 相容)
-- ============================================
PRINT N'[7/10] 插入舊版資料來源配置（相容性）...';
GO

INSERT INTO dbo.datasource_config (
    datasource_code, datasource_name, db_type, 
    jdbc_url, username, password_encrypted, 
    is_enabled, max_pool_size, description
) VALUES 
('PRIMARY_DB', N'主資料庫', 'SQLSERVER',
 'jdbc:sqlserver://localhost:1433;databaseName=api_db;encrypt=true;trustServerCertificate=true',
 'api_user', 'ENCRYPTED_PASSWORD_HERE', 1, 20, N'主要業務資料庫（舊版格式）');
GO

PRINT N'✅ Datasource Config (舊版): 1 筆資料插入完成';
GO

-- ============================================
-- 8. 插入 API 查詢配置 (重要：使用 :paramName 格式)
-- ============================================
PRINT N'[8/10] 插入 API 查詢配置...';
GO

SET IDENTITY_INSERT dbo.api_query_config ON;
GO

INSERT INTO dbo.api_query_config (
    query_id, query_code, query_name, datasource_code, 
    query_sql, description, category, 
    is_enabled, require_api_key, max_page_size, cache_seconds, created_by
) VALUES 
-- 客戶查詢 (使用 :paramName 格式)
(1, 'LIST_CUSTOMERS', N'查詢客戶列表', 'PRIMARY_DB',
 'SELECT * FROM dbo.customers WHERE account_status = :status ORDER BY created_at DESC',
 N'查詢客戶列表（支援分頁）', 'CUSTOMER', 1, 0, 200, 0, 'SYSTEM'),

(2, 'GET_CUSTOMER_BY_CODE', N'根據客戶代碼查詢', 'PRIMARY_DB',
 'SELECT * FROM dbo.customers WHERE customer_code = :customerCode',
 N'查詢單一客戶資料', 'CUSTOMER', 1, 0, 100, 0, 'SYSTEM'),

(3, 'SEARCH_CUSTOMER_BY_NAME', N'依姓名模糊查詢客戶', 'PRIMARY_DB',
 'SELECT * FROM dbo.customers WHERE name LIKE ''%'' + :name + ''%'' ORDER BY updated_at DESC',
 N'關鍵字搜尋姓名', 'CUSTOMER', 1, 0, 100, 0, 'SYSTEM'),

-- 投資組合查詢 (使用 :paramName 格式)
(4, 'GET_PORTFOLIO_BY_CUSTOMER', N'查詢客戶投資組合', 'PRIMARY_DB',
 'SELECT p.*, c.name AS customer_name FROM dbo.portfolios p INNER JOIN dbo.customers c ON p.customer_id = c.customer_id WHERE p.customer_id = :customerId AND p.status = :status',
 N'查詢客戶所有投資組合', 'PORTFOLIO', 1, 0, 100, 0, 'SYSTEM'),

(5, 'GET_PORTFOLIO_SUMMARY', N'投資組合總覽', 'PRIMARY_DB',
 'SELECT currency, COUNT(*) as portfolio_count, SUM(total_value) as total_value FROM dbo.portfolios WHERE status = ''ACTIVE'' GROUP BY currency',
 N'統計各幣別投資組合總額', 'PORTFOLIO', 1, 0, 100, 0, 'SYSTEM'),

-- 交易查詢 (使用 :paramName 格式)
(6, 'GET_TRANSACTIONS_BY_PORTFOLIO', N'查詢投資組合交易', 'PRIMARY_DB',
 'SELECT * FROM dbo.transactions WHERE portfolio_id = :portfolioId AND transaction_date >= :startDate AND transaction_date <= :endDate ORDER BY transaction_date DESC',
 N'查詢特定期間交易記錄', 'TRANSACTION', 1, 0, 500, 0, 'SYSTEM'),

(7, 'GET_TRANSACTION_SUMMARY', N'交易統計報表', 'PRIMARY_DB',
 'SELECT transaction_type, status, COUNT(*) as count, SUM(amount) as total_amount FROM dbo.transactions WHERE transaction_date >= :startDate GROUP BY transaction_type, status',
 N'統計各類型交易數量與金額', 'TRANSACTION', 1, 0, 100, 0, 'SYSTEM'),

-- 複雜查詢 (使用 :paramName 格式)
(8, 'GET_CUSTOMER_WEALTH_REPORT', N'客戶財富報表', 'REPORT',
 'SELECT c.customer_code, c.name, c.risk_level, COUNT(DISTINCT p.portfolio_id) as portfolio_count, SUM(p.total_value) as total_wealth FROM dbo.customers c LEFT JOIN dbo.portfolios p ON c.customer_id = p.customer_id WHERE c.account_status = ''ACTIVE'' AND p.status = ''ACTIVE'' GROUP BY c.customer_code, c.name, c.risk_level HAVING SUM(p.total_value) >= :minWealth ORDER BY total_wealth DESC',
 N'客戶財富統計報表', 'REPORT', 1, 0, 100, 300, 'SYSTEM');
GO

SET IDENTITY_INSERT dbo.api_query_config OFF;
GO

PRINT N'✅ API Query Config: 8 筆資料插入完成';
GO

-- ============================================
-- 9. 插入 API 查詢參數定義
-- ============================================
PRINT N'[9/10] 插入 API 查詢參數定義...';
GO

INSERT INTO dbo.api_query_params (query_id, param_name, param_type, is_required, default_value, validation_regex, description) VALUES
-- LIST_CUSTOMERS
(1, 'status', 'STRING', 0, 'ACTIVE', NULL, N'帳戶狀態'),

-- GET_CUSTOMER_BY_CODE
(2, 'customerCode', 'STRING', 1, NULL, '^[A-Z0-9]{5}$', N'客戶代碼'),

-- SEARCH_CUSTOMER_BY_NAME
(3, 'name', 'STRING', 1, NULL, '^.{1,20}$', N'姓名關鍵字(1-20字)'),

-- GET_PORTFOLIO_BY_CUSTOMER
(4, 'customerId', 'LONG', 1, NULL, '^[0-9]+$', N'客戶ID'),
(4, 'status', 'STRING', 0, 'ACTIVE', NULL, N'組合狀態'),

-- GET_TRANSACTIONS_BY_PORTFOLIO
(6, 'portfolioId', 'LONG', 1, NULL, '^[0-9]+$', N'投資組合ID'),
(6, 'startDate', 'DATETIME', 1, NULL, '^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$', N'開始日期 yyyy-MM-dd HH:mm:ss'),
(6, 'endDate', 'DATETIME', 1, NULL, '^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$', N'結束日期 yyyy-MM-dd HH:mm:ss'),

-- GET_TRANSACTION_SUMMARY
(7, 'startDate', 'DATETIME', 1, NULL, '^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$', N'統計起始日期'),

-- GET_CUSTOMER_WEALTH_REPORT
(8, 'minWealth', 'DECIMAL', 0, '0', '^[0-9]+(\.[0-9]{1,2})?$', N'最低財富門檻');
GO

PRINT N'✅ API Query Params: 10 筆資料插入完成';
GO

-- ============================================
-- 10. 插入 API 訪問日誌（模擬資料）
-- ============================================
PRINT N'[10/10] 插入 API 訪問日誌...';
GO

INSERT INTO dbo.api_access_log (
    api_key, app_name, endpoint, method, 
    status_code, response_time, ip_address, user_agent
) VALUES 
('api_live_1234567890abcdef1234567890abcdef', N'財富管理系統', 
 '/api/v1/customers', 'GET', 200, 125, '192.168.1.100', 
 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)'),
 
('api_mobile_abcdef1234567890abcdef1234567890', N'行動銀行APP', 
 '/api/v1/portfolios', 'GET', 200, 89, '192.168.1.101', 
 'Mobile App Android 12'),
 
('api_web_xyz123xyz123xyz123xyz123xyz123xyz1', N'網路銀行', 
 '/api/v1/transactions', 'POST', 201, 234, '192.168.1.102', 
 'Chrome/120.0.0.0'),
 
('api_live_1234567890abcdef1234567890abcdef', N'財富管理系統', 
 '/api/v1/query/execute', 'POST', 200, 567, '192.168.1.100', 
 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)'),
 
('api_test_1111111111111111111111111111111111', N'測試系統', 
 '/api/v1/customers/C0001', 'GET', 404, 45, '127.0.0.1', 
 'PostmanRuntime/7.32.0');
GO

PRINT N'✅ API Access Log: 5 筆資料插入完成';
GO

-- ============================================
-- 驗證資料插入與參數格式
-- ============================================
PRINT N'';
PRINT N'========================================';
PRINT N'✅ 測試資料插入完成！';
PRINT N'========================================';
PRINT N'';

SELECT 
    '資料表名稱' = t.name,
    '資料筆數' = SUM(p.rows)
FROM sys.tables t
INNER JOIN sys.partitions p ON t.object_id = p.object_id
WHERE t.schema_id = SCHEMA_ID('dbo')
    AND p.index_id IN (0, 1)
    AND t.name IN (
        'api_keys', 'customers', 'portfolios', 'transactions',
        'system_parameters', 'api_datasource_config', 'datasource_config',
        'api_query_config', 'api_query_params', 'api_access_log'
    )
GROUP BY t.name
ORDER BY t.name;
GO

PRINT N'';
PRINT N'========================================';
PRINT N'🔍 驗證 SQL 參數格式';
PRINT N'========================================';

SELECT 
    query_code AS [查詢代碼],
    query_name AS [查詢名稱],
    CASE 
        WHEN query_sql LIKE '%@%' AND query_sql NOT LIKE '%''@%''%' THEN '❌ 錯誤：使用 @ 參數'
        WHEN query_sql LIKE '%:%' THEN '✅ 正確：使用 : 參數'
        ELSE '⚠️ 無參數'
    END AS [參數格式檢查],
    LEN(query_sql) - LEN(REPLACE(query_sql, ':', '')) AS [參數數量]
FROM dbo.api_query_config
WHERE is_enabled = 1
ORDER BY query_id;
GO

PRINT N'';
PRINT N'========================================';
PRINT N'📊 資料統計:';
PRINT N'  • API Keys: 5 筆';
PRINT N'  • Customers: 10 筆';
PRINT N'  • Portfolios: 15 筆';
PRINT N'  • Transactions: 15 筆';
PRINT N'  • System Parameters: 15 筆';
PRINT N'  • API Datasource Config: 4 筆';
PRINT N'  • Datasource Config (舊): 1 筆';
PRINT N