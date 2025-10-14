-- ============================================
-- 銀行 API 平台 - 動態查詢初始資料 (SQL Server)
-- 執行順序: 04
-- ============================================

USE api_db;
GO

-- ============================================
-- 1. 插入資料源配置
-- ============================================

-- 主資料庫 (當前 SQL Server)
INSERT INTO api_datasource_config (
    datasource_code, datasource_name, db_type, jdbc_url,
    username, password_encrypted, driver_class, description
) VALUES (
    'PRIMARY_DB',
    N'財富管理主資料庫',
    'SQLSERVER',
    'jdbc:sqlserver://localhost:1433;databaseName=api_db;trustServerCertificate=true',
    'api_user',
    'Api@2024!',  -- 實際使用需加密
    'com.microsoft.sqlserver.jdbc.SQLServerDriver',
    N'財富管理系統主資料庫 (SQL Server 2022)'
);

-- 範例: 外部 MySQL 資料庫 (CRM系統)
INSERT INTO api_datasource_config (
    datasource_code, datasource_name, db_type, jdbc_url,
    username, password_encrypted, driver_class, max_pool_size, description
) VALUES (
    'CRM_DB',
    N'CRM客戶資料庫',
    'MYSQL',
    'jdbc:mysql://192.168.1.101:3306/crm_db?useSSL=false&serverTimezone=Asia/Taipei',
    'crm_user',
    'CRM@2024!',
    'com.mysql.cj.jdbc.Driver',
    5,
    N'CRM系統客戶資料庫 (MySQL)'
);

-- 範例: 外部 Oracle 資料庫 (交易系統)
INSERT INTO api_datasource_config (
    datasource_code, datasource_name, db_type, jdbc_url,
    username, password_encrypted, driver_class, is_enabled, description
) VALUES (
    'TRADING_DB',
    N'交易資料庫',
    'ORACLE',
    'jdbc:oracle:thin:@192.168.1.102:1521:TRADDB',
    'trade_user',
    'Trade@2024!',
    'oracle.jdbc.OracleDriver',
    0,  -- 先停用,需要時再啟用
    N'交易系統資料庫 (Oracle) - 未連線'
);

-- ============================================
-- 2. 插入查詢配置範例
-- ============================================

-- 範例1: 從主資料庫查詢客戶 (支援多條件)
INSERT INTO api_query_config (
    query_code, query_name, datasource_code, query_sql, 
    category, description, max_page_size
) VALUES (
    'customer.search',
    N'客戶多條件搜尋',
    'PRIMARY_DB',
    'SELECT customer_code, name, email, phone, risk_level, account_status, created_at
     FROM customers
     WHERE 1=1
       AND (@name IS NULL OR name LIKE ''%'' + @name + ''%'')
       AND (@email IS NULL OR email LIKE ''%'' + @email + ''%'')
       AND (@riskLevel IS NULL OR risk_level = @riskLevel)
       AND (@accountStatus IS NULL OR account_status = @accountStatus)
     ORDER BY created_at DESC',
    'CUSTOMER',
    N'支援姓名、Email、風險等級、帳戶狀態等多條件搜尋',
    200
);

-- 定義 customer.search 的參數
DECLARE @queryId INT = (SELECT query_id FROM api_query_config WHERE query_code = 'customer.search');

INSERT INTO api_query_params (query_id, param_name, param_type, is_required, description)
VALUES
    (@queryId, 'name', 'STRING', 0, N'客戶姓名 (模糊查詢)'),
    (@queryId, 'email', 'STRING', 0, N'電子郵件 (模糊查詢)'),
    (@queryId, 'riskLevel', 'STRING', 0, N'風險等級 (LOW/MEDIUM/HIGH)'),
    (@queryId, 'accountStatus', 'STRING', 0, N'帳戶狀態 (ACTIVE/INACTIVE/SUSPENDED)');

-- 範例2: 查詢客戶的投資組合
INSERT INTO api_query_config (
    query_code, query_name, datasource_code, query_sql,
    category, description
) VALUES (
    'portfolio.by.customer',
    N'客戶投資組合查詢',
    'PRIMARY_DB',
    'SELECT p.portfolio_code, p.portfolio_name, p.total_value, p.currency,
            p.status, c.name AS customer_name, p.updated_at
     FROM portfolios p
     INNER JOIN customers c ON p.customer_id = c.customer_id
     WHERE c.customer_code = @customerCode
       AND (@status IS NULL OR p.status = @status)
     ORDER BY p.total_value DESC',
    'PORTFOLIO',
    N'查詢指定客戶的所有投資組合'
);

SET @queryId = (SELECT query_id FROM api_query_config WHERE query_code = 'portfolio.by.customer');

INSERT INTO api_query_params (query_id, param_name, param_type, is_required, description)
VALUES
    (@queryId, 'customerCode', 'STRING', 1, N'客戶代碼 (必填)'),
    (@queryId, 'status', 'STRING', 0, N'投資組合狀態 (ACTIVE/INACTIVE)');

-- 範例3: 交易歷史查詢
INSERT INTO api_query_config (
    query_code, query_name, datasource_code, query_sql,
    category, description, max_page_size
) VALUES (
    'transaction.history',
    N'交易歷史查詢',
    'PRIMARY_DB',
    'SELECT t.transaction_id, p.portfolio_code, t.transaction_type,
            t.instrument_code, t.quantity, t.price, t.amount, t.currency,
            t.transaction_date, t.status
     FROM transactions t
     INNER JOIN portfolios p ON t.portfolio_id = p.portfolio_id
     WHERE p.portfolio_code = @portfolioCode
       AND (@startDate IS NULL OR t.transaction_date >= @startDate)
       AND (@endDate IS NULL OR t.transaction_date <= @endDate)
       AND (@transactionType IS NULL OR t.transaction_type = @transactionType)
     ORDER BY t.transaction_date DESC',
    'TRANSACTION',
    N'查詢投資組合的交易歷史記錄',
    500
);

SET @queryId = (SELECT query_id FROM api_query_config WHERE query_code = 'transaction.history');

INSERT INTO api_query_params (query_id, param_name, param_type, is_required, description)
VALUES
    (@queryId, 'portfolioCode', 'STRING', 1, N'投資組合代碼 (必填)'),
    (@queryId, 'startDate', 'DATETIME', 0, N'起始日期時間 (yyyy-MM-dd HH:mm:ss)'),
    (@queryId, 'endDate', 'DATETIME', 0, N'結束日期時間 (yyyy-MM-dd HH:mm:ss)'),
    (@queryId, 'transactionType', 'STRING', 0, N'交易類型 (BUY/SELL)');

-- 範例4: 客戶風險等級統計
INSERT INTO api_query_config (
    query_code, query_name, datasource_code, query_sql,
    category, description, max_page_size, cache_seconds
) VALUES (
    'customer.risk.statistics',
    N'客戶風險等級統計',
    'PRIMARY_DB',
    'SELECT risk_level AS riskLevel,
            COUNT(*) AS customerCount,
            CAST(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER() AS DECIMAL(5,2)) AS percentage
     FROM customers
     WHERE account_status = ''ACTIVE''
     GROUP BY risk_level
     ORDER BY customerCount DESC',
    'CUSTOMER',
    N'統計各風險等級的客戶數量與佔比',
    10,
    300  -- 快取5分鐘
);

GO

-- ============================================
-- 3. 驗證資料插入
-- ============================================
SELECT '=== 資料源配置 ===' AS '';
SELECT datasource_code, datasource_name, db_type, is_enabled FROM api_datasource_config;

SELECT '=== 查詢配置 ===' AS '';
SELECT query_code, query_name, category, datasource_code FROM api_query_config;

SELECT '=== 參數定義統計 ===' AS '';
SELECT 
    qc.query_code,
    COUNT(qp.param_id) AS param_count
FROM api_query_config qc
LEFT JOIN api_query_params qp ON qc.query_id = qp.query_id
GROUP BY qc.query_code
ORDER BY qc.query_code;
GO