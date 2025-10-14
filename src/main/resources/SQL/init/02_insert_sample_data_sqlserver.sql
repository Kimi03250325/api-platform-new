-- ============================================
-- 銀行 API 平台 - SQL Server 2022 測試資料腳本
-- 執行順序：02
-- 日期：2025-10-14
-- ============================================

USE api_db;
GO

-- ============================================
-- 1. 插入測試 API Keys
-- ============================================
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

PRINT N'✅ API Keys 測試資料插入完成 (5 筆)';
GO

-- ============================================
-- 2. 插入測試客戶資料
-- ============================================
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

PRINT N'✅ 客戶測試資料插入完成 (10 筆)';
GO

-- ============================================
-- 3. 插入測試投資組合
-- ============================================
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

PRINT N'✅ 投資組合測試資料插入完成 (15 筆)';
GO

-- ============================================
-- 4. 插入測試交易記錄
-- ============================================
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
 'TWD', '2025-10-05 11:30:00', '2025-10-07', 'COMPLETED', N'債券配置'),

-- 美股交易
(9, 12, 'BUY', 'AAPL', N'Apple Inc.', 50.0000, 178.50, 8925.00, 
 'USD', '2025-10-05 22:00:00', '2025-10-08', 'COMPLETED', N'美股夜盤'),
(10, 12, 'BUY', 'MSFT', N'Microsoft Corp.', 30.0000, 375.20, 11256.00, 
 'USD', '2025-10-06 22:30:00', '2025-10-09', 'COMPLETED', NULL),
(11, 14, 'BUY', 'TSLA', N'Tesla Inc.', 25.0000, 248.30, 6207.50, 
 'USD', '2025-10-07 21:45:00', '2025-10-10', 'COMPLETED', N'科技股加碼'),

-- 賣出交易
(12, 1, 'SELL', '2330.TW', N'台積電', 50.0000, 595.00, 29750.00, 
 'TWD', '2025-10-08 13:00:00', '2025-10-10', 'COMPLETED', N'部分獲利了結'),
(13, 4, 'SELL', '2317.TW', N'鴻海', 100.0000, 112.00, 11200.00, 
 'TWD', '2025-10-09 10:15:00', '2025-10-11', 'COMPLETED', N'調整持股'),

-- 股息收益
(14, 1, 'DIVIDEND', '2330.TW', N'台積電', 50.0000, 3.00, 150.00, 
 'TWD', '2025-10-10 00:00:00', '2025-10-10', 'COMPLETED', N'Q3股息'),
(15, 3, 'DIVIDEND', '2454.TW', N'聯發科', 150.0000, 25.00, 3750.00, 
 'TWD', '2025-10-10 00:00:00', '2025-10-10', 'COMPLETED', N'Q3股息'),

-- ETF交易
(16, 8, 'BUY', '00878.TW', N'國泰永續高股息', 400.0000, 21.50, 8600.00, 
 'TWD', '2025-10-10 09:00:00', '2025-10-14', 'COMPLETED', N'高股息ETF'),
(17, 9, 'BUY', '00692.TW', N'富邦公司治理', 150.0000, 38.20, 5730.00, 
 'TWD', '2025-10-11 10:30:00', '2025-10-15', 'PENDING', N'ESG投資'),

-- 債券交易
(18, 12, 'BUY', 'BOND001', N'美國10年期公債', 10000.0000, 95.50, 955000.00, 
 'USD', '2025-10-11 15:00:00', '2025-10-13', 'COMPLETED', N'避險配置'),
(19, 6, 'BUY', 'BOND002', N'台灣政府公債', 50000.0000, 98.20, 4910000.00, 
 'TWD', '2025-10-12 14:00:00', '2025-10-14', 'COMPLETED', N'固定收益'),

-- 最近交易（含待處理）
(20, 11, 'BUY', '2454.TW', N'聯發科', 80.0000, 1080.00, 86400.00, 
 'TWD', '2025-10-13 09:30:00', '2025-10-15', 'PENDING', N'科技股加碼'),
(21, 10, 'BUY', 'NVDA', N'NVIDIA Corp.', 15.0000, 485.00, 7275.00, 
 'USD', '2025-10-13 22:00:00', '2025-10-16', 'PENDING', N'AI晶片龍頭'),

-- 轉帳交易
(22, 2, 'TRANSFER', 'CASH', N'現金轉入', 1.0000, 50000.00, 50000.00, 
 'TWD', '2025-10-14 10:00:00', '2025-10-14', 'COMPLETED', N'資金調度'),
(23, 15, 'TRANSFER', 'CASH', N'現金轉入', 1.0000, 10000.00, 10000.00, 
 'USD', '2025-10-14 11:00:00', '2025-10-14', 'COMPLETED', N'美元帳戶入金'),

-- 失敗交易
(24, 7, 'BUY', '2881.TW', N'富邦金', 200.0000, 85.00, 17000.00, 
 'TWD', '2025-10-14 09:00:00', '2025-10-16', 'FAILED', N'資金不足'),
(25, 13, 'BUY', 'GLD', N'SPDR Gold Shares', 50.0000, 185.00, 9250.00, 
 'USD', '2025-10-14 22:00:00', '2025-10-17', 'CANCELLED', N'用戶取消');
GO

SET IDENTITY_INSERT dbo.transactions OFF;
GO

PRINT N'✅ 交易記錄測試資料插入完成 (25 筆)';
GO

-- ============================================
-- 5. 插入系統參數
-- ============================================
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

PRINT N'✅ 系統參數測試資料插入完成 (15 筆)';
GO

-- ============================================
-- 6. 插入資料來源配置
-- ============================================
INSERT INTO dbo.datasource_config (
    datasource_code, datasource_name, db_type, 
    jdbc_url, username, password_encrypted, 
    is_enabled, max_pool_size, description
) VALUES 
('PRIMARY_DB', N'主資料庫', 'SQLSERVER',
 'jdbc:sqlserver://localhost:1433;databaseName=api_db;encrypt=true;trustServerCertificate=true',
 'api_user', 'ENCRYPTED_PASSWORD_HERE', 1, 20, N'主要業務資料庫'),

('REPORT_DB', N'報表資料庫', 'SQLSERVER',
 'jdbc:sqlserver://localhost:1433;databaseName=report_db;encrypt=true;trustServerCertificate=true',
 'report_user', 'ENCRYPTED_PASSWORD_HERE', 1, 10, N'報表專用資料庫'),

('ARCHIVE_DB', N'歷史資料庫', 'SQLSERVER',
 'jdbc:sqlserver://archive-server:1433;databaseName=archive_db;encrypt=true;trustServerCertificate=true',
 'archive_user', 'ENCRYPTED_PASSWORD_HERE', 1, 5, N'歷史資料歸檔庫'),

('EXTERNAL_MYSQL', N'外部MySQL系統', 'MYSQL',
 'jdbc:mysql://external-host:3306/external_db?useSSL=false&serverTimezone=Asia/Taipei',
 'external_user', 'ENCRYPTED_PASSWORD_HERE', 0, 10, N'外部合作夥伴MySQL資料庫');
GO

PRINT N'✅ 資料來源配置測試資料插入完成 (4 筆)';
GO

-- ============================================
-- 7. 插入 API 查詢配置
-- ============================================
INSERT INTO dbo.api_query_config (
    query_code, query_name, category, 
    datasource_code, db_type, query_sql, 
    param_config, is_enabled, description, created_by
) VALUES 
-- 客戶查詢
('GET_CUSTOMER_BY_CODE', N'根據客戶代碼查詢', 'CUSTOMER',
 'PRIMARY_DB', 'SQLSERVER',
 'SELECT * FROM dbo.customers WHERE customer_code = @customerCode',
 '[{"paramName":"customerCode","paramType":"STRING","required":true,"description":"客戶代碼"}]',
 1, N'查詢單一客戶資料', 'SYSTEM'),

('LIST_CUSTOMERS', N'查詢客戶列表', 'CUSTOMER',
 'PRIMARY_DB', 'SQLSERVER',
 'SELECT * FROM dbo.customers WHERE account_status = @status ORDER BY created_at DESC',
 '[{"paramName":"status","paramType":"STRING","required":false,"defaultValue":"ACTIVE","description":"帳戶狀態"}]',
 1, N'查詢客戶列表（支援分頁）', 'SYSTEM'),

-- 投資組合查詢
('GET_PORTFOLIO_BY_CUSTOMER', N'查詢客戶投資組合', 'PORTFOLIO',
 'PRIMARY_DB', 'SQLSERVER',
 'SELECT p.*, c.name AS customer_name FROM dbo.portfolios p INNER JOIN dbo.customers c ON p.customer_id = c.customer_id WHERE p.customer_id = @customerId AND p.status = @status',
 '[{"paramName":"customerId","paramType":"LONG","required":true,"description":"客戶ID"},{"paramName":"status","paramType":"STRING","required":false,"defaultValue":"ACTIVE","description":"組合狀態"}]',
 1, N'查詢客戶所有投資組合', 'SYSTEM'),

('GET_PORTFOLIO_SUMMARY', N'投資組合總覽', 'PORTFOLIO',
 'PRIMARY_DB', 'SQLSERVER',
 'SELECT currency, COUNT(*) as portfolio_count, SUM(total_value) as total_value FROM dbo.portfolios WHERE status = ''ACTIVE'' GROUP BY currency',
 '[]',
 1, N'統計各幣別投資組合總額', 'SYSTEM'),

-- 交易查詢
('GET_TRANSACTIONS_BY_PORTFOLIO', N'查詢投資組合交易', 'TRANSACTION',
 'PRIMARY_DB', 'SQLSERVER',
 'SELECT * FROM dbo.transactions WHERE portfolio_id = @portfolioId AND transaction_date >= @startDate AND transaction_date <= @endDate ORDER BY transaction_date DESC',
 '[{"paramName":"portfolioId","paramType":"LONG","required":true,"description":"投資組合ID"},{"paramName":"startDate","paramType":"DATETIME","required":true,"description":"開始日期"},{"paramName":"endDate","paramType":"DATETIME","required":true,"description":"結束日期"}]',
 1, N'查詢特定期間交易記錄', 'SYSTEM'),

('GET_TRANSACTION_SUMMARY', N'交易統計報表', 'TRANSACTION',
 'PRIMARY_DB', 'SQLSERVER',
 'SELECT transaction_type, status, COUNT(*) as count, SUM(amount) as total_amount FROM dbo.transactions WHERE transaction_date >= @startDate GROUP BY transaction_type, status',
 '[{"paramName":"startDate","paramType":"DATETIME","required":true,"description":"統計起始日期"}]',
 1, N'統計各類型交易數量與金額', 'SYSTEM'),

-- 複雜查詢
('GET_CUSTOMER_WEALTH_REPORT', N'客戶財富報表', 'REPORT',
 'PRIMARY_DB', 'SQLSERVER',
 'SELECT c.customer_code, c.name, c.risk_level, COUNT(DISTINCT p.portfolio_id) as portfolio_count, SUM(p.total_value) as total_wealth FROM dbo.customers c LEFT JOIN dbo.portfolios p ON c.customer_id = p.customer_id WHERE c.account_status = ''ACTIVE'' AND p.status = ''ACTIVE'' GROUP BY c.customer_code, c.name, c.risk_level HAVING SUM(p.total_value) >= @minWealth ORDER BY total_wealth DESC',
 '[{"paramName":"minWealth","paramType":"DECIMAL","required":false,"defaultValue":"0","description":"最低財富門檻"}]',
 1, N'客戶財富統計報表', 'SYSTEM');
GO

PRINT N'✅ API 查詢配置測試資料插入完成 (7 筆)';
GO

-- ============================================
-- 8. 插入 API 訪問日誌（模擬資料）
-- ============================================
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

PRINT N'✅ API 訪問日誌測試資料插入完成 (5 筆)';
GO

-- ============================================
-- 9. 插入外部系統介接日誌
-- ============================================
INSERT INTO dbo.external_system_log (
    system_name, operation, status, execution_time
) VALUES 
(N'核心銀行系統', N'查詢帳戶餘額', 'SUCCESS', 234),
(N'信用卡系統', N'查詢交易明細', 'SUCCESS', 156),
(N'風險管理系統', N'更新風險評級', 'SUCCESS', 489),
(N'報表系統', N'產生月報表', 'SUCCESS', 2345),
(N'簡訊通知服務', N'發送交易通知', 'FAILED', 5000);
GO

PRINT N'✅ 外部系統介接日誌測試資料插入完成 (5 筆)';
GO

-- ============================================
-- 10. 驗證所有資料插入
-- ============================================
PRINT N'';
PRINT N'========================================';
PRINT N'測試資料插入完成！';
PRINT N'========================================';
PRINT N'';

SELECT N'API Keys' AS [資料表], COUNT(*) AS [筆數] FROM dbo.api_keys
UNION ALL
SELECT N'Customers', COUNT(*) FROM dbo.customers
UNION ALL
SELECT N'Portfolios', COUNT(*) FROM dbo.portfolios
UNION ALL
SELECT N'Transactions', COUNT(*) FROM dbo.transactions
UNION ALL
SELECT N'System Parameters', COUNT(*) FROM dbo.system_parameters
UNION ALL
SELECT N'Datasource Config', COUNT(*) FROM dbo.datasource_config
UNION ALL
SELECT N'API Query Config', COUNT(*) FROM dbo.api_query_config
UNION ALL
SELECT N'API Access Log', COUNT(*) FROM dbo.api_access_log
UNION ALL
SELECT N'External System Log', COUNT(*) FROM dbo.external_system_log;
GO

PRINT N'========================================';
PRINT N'';

-- ============================================
-- 11. 查看關鍵資料摘要
-- ============================================

PRINT N'=== API Keys 摘要 ===';
SELECT app_name AS [應用名稱], api_key AS [API Key], 
       rate_limit AS [流量限制], status AS [狀態]
FROM dbo.api_keys;
GO

PRINT N'';
PRINT N'=== 客戶摘要 ===';
SELECT customer_code AS [客戶代碼], name AS [姓名], 
       risk_level AS [風險等級], account_status AS [狀態]
FROM dbo.customers;
GO

PRINT N'';
PRINT N'=== 投資組合摘要（前10筆）===';
SELECT TOP 10
    p.portfolio_code AS [組合代碼], 
    c.name AS [客戶姓名], 
    p.portfolio_name AS [組合名稱], 
    p.total_value AS [總價值],
    p.currency AS [幣別]
FROM dbo.portfolios p
INNER JOIN dbo.customers c ON p.customer_id = c.customer_id
ORDER BY p.total_value DESC;
GO

PRINT N'';
PRINT N'=== 最近交易記錄（前10筆）===';
SELECT TOP 10
    t.transaction_id AS [交易ID], 
    p.portfolio_code AS [組合代碼], 
    t.transaction_type AS [類型], 
    t.instrument_name AS [商品名稱], 
    t.amount AS [金額], 
    t.status AS [狀態],
    t.transaction_date AS [交易日期]
FROM dbo.transactions t
INNER JOIN dbo.portfolios p ON t.portfolio_id = p.portfolio_id
ORDER BY t.transaction_date DESC;
GO

PRINT N'';
PRINT N'========================================';
PRINT N'所有測試資料已成功插入！';
PRINT N'========================================';
GO