-- ============================================
-- 銀行 API 平台 - 初始資料腳本
-- 執行順序：02
-- ============================================

USE api_db;

-- ============================================
-- 1. 插入測試 API Keys
-- ============================================
INSERT INTO api_keys (
    app_name, 
    api_key, 
    api_secret, 
    rate_limit, 
    description,
    created_by
) VALUES 
(
    '財富管理系統',
    'api_live_1234567890abcdef1234567890abcdef',
    'secret_0987654321fedcba0987654321fedcba0987654321fedcba0987654321fedcba',
    5000,
    '生產環境主系統',
    'SYSTEM'
),
(
    '行動銀行APP',
    'api_mobile_abcdef1234567890abcdef1234567890',
    'secret_fedcba0987654321fedcba0987654321fedcba0987654321fedcba0987654321',
    3000,
    '行動銀行應用程式',
    'SYSTEM'
),
(
    '測試系統',
    'api_test_1111111111111111111111111111111111',
    'secret_1111111111111111111111111111111111111111111111111111111111111111',
    1000,
    '開發測試用',
    'SYSTEM'
);

-- ============================================
-- 2. 插入測試客戶資料
-- ============================================
INSERT INTO customers (
    customer_code,
    name,
    id_number,
    email,
    phone,
    risk_level,
    account_status
) VALUES 
('C0001', '張三', 'A123456789', 'zhang.san@example.com', '0912345678', 'MEDIUM', 'ACTIVE'),
('C0002', '李四', 'B987654321', 'li.si@example.com', '0923456789', 'LOW', 'ACTIVE'),
('C0003', '王五', 'C246813579', 'wang.wu@example.com', '0934567890', 'HIGH', 'ACTIVE');

-- ============================================
-- 3. 插入測試投資組合
-- ============================================
INSERT INTO portfolios (
    customer_id,
    portfolio_code,
    portfolio_name,
    total_value,
    currency,
    status
) VALUES 
(1, 'P0001', '張三成長型投資組合', 1000000.00, 'TWD', 'ACTIVE'),
(1, 'P0002', '張三穩健型投資組合', 500000.00, 'TWD', 'ACTIVE'),
(2, 'P0003', '李四退休準備組合', 2000000.00, 'TWD', 'ACTIVE'),
(3, 'P0004', '王五積極型組合', 3000000.00, 'TWD', 'ACTIVE');

-- ============================================
-- 4. 插入測試交易記錄
-- ============================================
INSERT INTO transactions (
    portfolio_id,
    transaction_type,
    instrument_code,
    quantity,
    price,
    amount,
    currency,
    transaction_date,
    settlement_date,
    status
) VALUES 
(1, 'BUY', '2330.TW', 100.0000, 580.00, 58000.00, 'TWD', '2025-01-01 09:00:00', '2025-01-03', 'COMPLETED'),
(1, 'BUY', '0050.TW', 50.0000, 145.00, 7250.00, 'TWD', '2025-01-02 10:30:00', '2025-01-04', 'COMPLETED'),
(2, 'BUY', '0056.TW', 200.0000, 35.50, 7100.00, 'TWD', '2025-01-03 14:15:00', '2025-01-05', 'COMPLETED'),
(3, 'BUY', '2454.TW', 150.0000, 420.00, 63000.00, 'TWD', '2025-01-04 11:00:00', '2025-01-06', 'COMPLETED');

-- ============================================
-- 5. 插入系統參數
-- ============================================
INSERT INTO system_parameters (param_key, param_value, description) VALUES 
('API_VERSION', '1.0', 'API 版本'),
('MAX_REQUEST_SIZE', '10485760', '最大請求大小（10MB）'),
('SESSION_TIMEOUT', '1800', 'Session 逾時秒數（30分鐘）'),
('ENABLE_CACHE', 'true', '是否啟用快取'),
('DEFAULT_RATE_LIMIT', '1000', '預設流量限制（每分鐘）');

-- ============================================
-- 6. 驗證資料插入
-- ============================================
SELECT 'API Keys' AS '資料表', COUNT(*) AS '筆數' FROM api_keys
UNION ALL
SELECT 'Customers', COUNT(*) FROM customers
UNION ALL
SELECT 'Portfolios', COUNT(*) FROM portfolios
UNION ALL
SELECT 'Transactions', COUNT(*) FROM transactions
UNION ALL
SELECT 'System Parameters', COUNT(*) FROM system_parameters;

-- ============================================
-- 查看插入的資料
-- ============================================
SELECT '=== API Keys ===' AS '';
SELECT app_name, api_key, status, rate_limit FROM api_keys;

SELECT '=== Customers ===' AS '';
SELECT customer_code, name, email, account_status FROM customers;

SELECT '=== Portfolios ===' AS '';
SELECT p.portfolio_code, c.name AS customer_name, p.portfolio_name, p.total_value 
FROM portfolios p
JOIN customers c ON p.customer_id = c.customer_id;

SELECT '=== Transactions ===' AS '';
SELECT t.transaction_id, p.portfolio_code, t.transaction_type, 
       t.instrument_code, t.amount, t.status
FROM transactions t
JOIN portfolios p ON t.portfolio_id = p.portfolio_id
ORDER BY t.transaction_date DESC;