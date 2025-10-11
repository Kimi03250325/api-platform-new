-- ============================================
-- 銀行 API 平台 - SQL Server 2022 初始資料腳本
-- 執行順序：02
-- 日期：2025-01-10
-- ============================================

USE api_db;
GO

SET IDENTITY_INSERT dbo.api_keys ON;
GO

-- ============================================
-- 1. 插入測試 API Keys
-- ============================================
INSERT INTO dbo.api_keys (
    id, app_name, api_key, api_secret, 
    rate_limit, description, created_by
) VALUES 
(
    1,
    N'財富管理系統',
    'api_live_1234567890abcdef1234567890abcdef',
    'secret_0987654321fedcba0987654321fedcba0987654321fedcba0987654321fedcba',
    5000,
    N'生產環境主系統',
    'SYSTEM'
),
(
    2,
    N'行動銀行APP',
    'api_mobile_abcdef1234567890abcdef1234567890',
    'secret_fedcba0987654321fedcba0987654321fedcba0987654321fedcba0987654321',
    3000,
    N'行動銀行應用程式',
    'SYSTEM'
),
(
    3,
    N'測試系統',
    'api_test_1111111111111111111111111111111111',
    'secret_1111111111111111111111111111111111111111111111111111111111111111',
    1000,
    N'開發測試用',
    'SYSTEM'
);
GO

SET IDENTITY_INSERT dbo.api_keys OFF;
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
(5, 'C0005', N'錢七', 'E864297531', 'qian.qi@example.com', '0956789012', 'LOW', 'ACTIVE');
GO

SET IDENTITY_INSERT dbo.customers OFF;
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
(1, 1, 'P0001', N'張三成長型投資組合', 1000000.00, 'TWD', 'ACTIVE'),
(2, 1, 'P0002', N'張三穩健型投資組合', 500000.00, 'TWD', 'ACTIVE'),
(3, 2, 'P0003', N'李四退休準備組合', 2000000.00, 'TWD', 'ACTIVE'),
(4, 3, 'P0004', N'王五積極型組合', 3000000.00, 'TWD', 'ACTIVE'),
(5, 4, 'P0005', N'趙六平衡型組合', 1500000.00, 'TWD', 'ACTIVE');
GO

SET IDENTITY_INSERT dbo.portfolios OFF;
GO

-- ============================================
-- 4. 插入測試交易記錄
-- ============================================
SET IDENTITY_INSERT dbo.transactions ON;
GO

INSERT INTO dbo.transactions (
    transaction_id, portfolio_id, transaction_type, 
    instrument_code, quantity, price, amount,
    currency, transaction_date, settlement_date, status
) VALUES 
(1, 1, 'BUY', '2330.TW', 100.0000, 580.00, 58000.00, 
 'TWD', '2025-01-01 09:00:00', '2025-01-03', 'COMPLETED'),
(2, 1, 'BUY', '0050.TW', 50.0000, 145.00, 7250.00, 
 'TWD', '2025-01-02 10:30:00', '2025-01-04', 'COMPLETED'),
(3, 2, 'BUY', '0056.TW', 200.0000, 35.50, 7100.00, 
 'TWD', '2025-01-03 14:15:00', '2025-01-05', 'COMPLETED'),
(4, 3, 'BUY', '2454.TW', 150.0000, 420.00, 63000.00, 
 'TWD', '2025-01-04 11:00:00', '2025-01-06', 'COMPLETED'),
(5, 4, 'BUY', '2317.TW', 80.0000, 95.50, 7640.00, 
 'TWD', '2025-01-05 09:30:00', '2025-01-07', 'COMPLETED');
GO

SET IDENTITY_INSERT dbo.transactions OFF;
GO

-- ============================================
-- 5. 插入系統參數
-- ============================================
INSERT INTO dbo.system_parameters (param_key, param_value, description) 
VALUES 
('API_VERSION', '1.0.0', N'API 版本'),
('MAX_REQUEST_SIZE', '10485760', N'最大請求大小（10MB）'),
('SESSION_TIMEOUT', '1800', N'Session 逾時秒數（30分鐘）'),
('ENABLE_CACHE', 'true', N'是否啟用快取'),
('DEFAULT_RATE_LIMIT', '1000', N'預設流量限制（每分鐘）'),
('LOG_RETENTION_DAYS', '30', N'日誌保留天數');
GO

-- ============================================
-- 6. 驗證資料插入
-- ============================================
SELECT N'API Keys' AS '資料表', COUNT(*) AS '筆數' FROM dbo.api_keys
UNION ALL
SELECT N'Customers', COUNT(*) FROM dbo.customers
UNION ALL
SELECT N'Portfolios', COUNT(*) FROM dbo.portfolios
UNION ALL
SELECT N'Transactions', COUNT(*) FROM dbo.transactions
UNION ALL
SELECT N'System Parameters', COUNT(*) FROM dbo.system_parameters;
GO

-- ============================================
-- 7. 查看插入的資料
-- ============================================
PRINT N'=== API Keys ===';
SELECT app_name, api_key, status, rate_limit 
FROM dbo.api_keys;
GO

PRINT N'=== Customers ===';
SELECT customer_code, name, email, risk_level, account_status 
FROM dbo.customers;
GO

PRINT N'=== Portfolios ===';
SELECT 
    p.portfolio_code, 
    c.name AS customer_name, 
    p.portfolio_name, 
    p.total_value 
FROM dbo.portfolios p
INNER JOIN dbo.customers c ON p.customer_id = c.customer_id;
GO

PRINT N'=== Transactions ===';
SELECT 
    t.transaction_id, 
    p.portfolio_code, 
    t.transaction_type, 
    t.instrument_code, 
    t.amount, 
    t.status
FROM dbo.transactions t
INNER JOIN dbo.portfolios p ON t.portfolio_id = p.portfolio_id
ORDER BY t.transaction_date DESC;
GO

PRINT N'資料插入完成！';
GO