-- ============================================
-- 銀行 API 平台 - 資料表建立腳本
-- 資料庫：MySQL 8.0
-- 執行順序：01
-- ============================================

USE api_db;

-- ============================================
-- 1. API Keys 管理表
-- ============================================
DROP TABLE IF EXISTS api_keys;

CREATE TABLE api_keys (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主鍵',
    app_name VARCHAR(100) NOT NULL COMMENT '應用程式名稱',
    api_key VARCHAR(64) NOT NULL UNIQUE COMMENT 'API Key',
    api_secret VARCHAR(128) NOT NULL COMMENT 'API Secret',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '狀態: ACTIVE, INACTIVE, SUSPENDED',
    ip_whitelist TEXT COMMENT 'IP 白名單（JSON 格式）',
    rate_limit INT NOT NULL DEFAULT 1000 COMMENT '流量限制（每分鐘請求數）',
    description VARCHAR(500) COMMENT '描述',
    created_by VARCHAR(50) COMMENT '建立者',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    expired_at DATETIME COMMENT '過期時間',
    last_used_at DATETIME COMMENT '最後使用時間',
    INDEX idx_api_keys_status (status),
    INDEX idx_api_keys_expired (expired_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='API Key 管理表';

-- ============================================
-- 2. 客戶資料表
-- ============================================
DROP TABLE IF EXISTS customers;

CREATE TABLE customers (
    customer_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '客戶 ID',
    customer_code VARCHAR(50) NOT NULL UNIQUE COMMENT '客戶代碼',
    name VARCHAR(100) NOT NULL COMMENT '客戶姓名',
    id_number VARCHAR(20) COMMENT '身分證字號',
    email VARCHAR(100) COMMENT '電子郵件',
    phone VARCHAR(20) COMMENT '電話',
    risk_level VARCHAR(20) COMMENT '風險等級: LOW, MEDIUM, HIGH',
    account_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '帳戶狀態: ACTIVE, INACTIVE, SUSPENDED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    INDEX idx_customers_code (customer_code),
    INDEX idx_customers_status (account_status),
    INDEX idx_customers_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客戶資料表';

-- ============================================
-- 3. 投資組合表
-- ============================================
DROP TABLE IF EXISTS portfolios;

CREATE TABLE portfolios (
    portfolio_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '投資組合 ID',
    customer_id BIGINT NOT NULL COMMENT '客戶 ID',
    portfolio_code VARCHAR(50) NOT NULL UNIQUE COMMENT '投資組合代碼',
    portfolio_name VARCHAR(100) COMMENT '投資組合名稱',
    total_value DECIMAL(18, 2) COMMENT '總價值',
    currency VARCHAR(3) NOT NULL DEFAULT 'TWD' COMMENT '幣別',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '狀態: ACTIVE, INACTIVE, CLOSED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    INDEX idx_portfolios_customer (customer_id),
    INDEX idx_portfolios_code (portfolio_code),
    INDEX idx_portfolios_status (status),
    CONSTRAINT fk_portfolios_customer 
        FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投資組合表';

-- ============================================
-- 4. 交易記錄表
-- ============================================
DROP TABLE IF EXISTS transactions;

CREATE TABLE transactions (
    transaction_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '交易 ID',
    portfolio_id BIGINT NOT NULL COMMENT '投資組合 ID',
    transaction_type VARCHAR(20) NOT NULL COMMENT '交易類型: BUY, SELL, DIVIDEND',
    instrument_code VARCHAR(50) COMMENT '商品代碼',
    quantity DECIMAL(18, 4) COMMENT '數量',
    price DECIMAL(18, 2) COMMENT '價格',
    amount DECIMAL(18, 2) COMMENT '金額',
    currency VARCHAR(3) COMMENT '幣別',
    transaction_date DATETIME NOT NULL COMMENT '交易日期',
    settlement_date DATE COMMENT '交割日期',
    status VARCHAR(20) NOT NULL COMMENT '狀態: PENDING, COMPLETED, CANCELLED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    INDEX idx_transactions_portfolio (portfolio_id),
    INDEX idx_transactions_date (transaction_date),
    INDEX idx_transactions_status (status),
    INDEX idx_transactions_type (transaction_type),
    CONSTRAINT fk_transactions_portfolio 
        FOREIGN KEY (portfolio_id) REFERENCES portfolios(portfolio_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交易記錄表';

-- ============================================
-- 5. API 訪問日誌表（分區表）
-- ============================================
DROP TABLE IF EXISTS api_access_log;

CREATE TABLE api_access_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日誌 ID',
    api_key VARCHAR(64) COMMENT 'API Key',
    app_name VARCHAR(100) COMMENT '應用程式名稱',
    endpoint VARCHAR(255) COMMENT 'API 端點',
    method VARCHAR(10) COMMENT 'HTTP 方法',
    status_code INT COMMENT 'HTTP 狀態碼',
    response_time INT COMMENT '回應時間（毫秒）',
    ip_address VARCHAR(45) COMMENT 'IP 位址',
    user_agent VARCHAR(500) COMMENT 'User Agent',
    error_message TEXT COMMENT '錯誤訊息',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    INDEX idx_api_log_key (api_key),
    INDEX idx_api_log_time (created_at),
    INDEX idx_api_log_endpoint (endpoint)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='API 訪問日誌表';

-- ============================================
-- 6. 外部系統介接日誌表
-- ============================================
DROP TABLE IF EXISTS external_system_log;

CREATE TABLE external_system_log (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日誌 ID',
    system_name VARCHAR(50) NOT NULL COMMENT '系統名稱',
    operation VARCHAR(100) COMMENT '操作',
    request_data TEXT COMMENT '請求資料',
    response_data TEXT COMMENT '回應資料',
    status VARCHAR(20) COMMENT '狀態',
    error_message TEXT COMMENT '錯誤訊息',
    execution_time INT COMMENT '執行時間（毫秒）',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    INDEX idx_external_log_system (system_name),
    INDEX idx_external_log_time (created_at),
    INDEX idx_external_log_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='外部系統介接日誌表';

-- ============================================
-- 7. 系統參數表
-- ============================================
DROP TABLE IF EXISTS system_parameters;

CREATE TABLE system_parameters (
    param_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '參數 ID',
    param_key VARCHAR(100) NOT NULL UNIQUE COMMENT '參數鍵',
    param_value TEXT COMMENT '參數值',
    description VARCHAR(500) COMMENT '說明',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系統參數表';

-- ============================================
-- 驗證資料表建立
-- ============================================
SELECT 
    TABLE_NAME AS '資料表名稱',
    TABLE_ROWS AS '資料筆數',
    TABLE_COMMENT AS '說明'
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'api_db'
ORDER BY TABLE_NAME;