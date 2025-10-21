# 銀行 API 平台 - SQL 腳本使用說明

## 📋 目錄結構

```
SQL/init/
├── 01_create_tables_sqlserver.sql       # 建立所有資料表
├── 02_insert_sample_data_sqlserver.sql  # 插入測試資料
└── README.md                             # 本說明文件
```

---

## 🎯 執行順序

### **全新安裝流程**

1. **建立資料庫**（如果尚未建立）
   ```sql
   CREATE DATABASE api_db;
   GO
   ```

2. **執行建表腳本**
   ```bash
   sqlcmd -S localhost -U sa -P YourPassword -d api_db -i 01_create_tables_sqlserver.sql
   ```

3. **執行測試資料腳本**
   ```bash
   sqlcmd -S localhost -U sa -P YourPassword -d api_db -i 02_insert_sample_data_sqlserver.sql
   ```

4. **驗證參數格式**（重要！）
   ```sql
   -- 確認所有查詢使用 :paramName 格式
   SELECT query_code, 
          CASE 
              WHEN query_sql LIKE '%@%' AND query_sql NOT LIKE '%''@%''%' 
              THEN '❌ 錯誤格式' 
              ELSE '✅ 正確格式' 
          END AS status
   FROM dbo.api_query_config WHERE is_enabled = 1;
   ```

---

## ⚠️ 重要：SQL 參數格式規範

### **必須使用 `:paramName` 格式**

本專案使用 Spring 的 `NamedParameterJdbcTemplate`，必須使用 JDBC 標準的具名參數格式：

| ❌ 錯誤格式 | ✅ 正確格式 | 說明 |
|-----------|-----------|------|
| `@status` | `:status` | T-SQL 原生格式不適用 |
| `@customerCode` | `:customerCode` | JDBC 需要冒號開頭 |
| `@startDate` | `:startDate` | 所有參數都要用冒號 |

### **範例對照：**

```sql
-- ❌ 錯誤：使用 @ 會導致「必須宣告純量變數」錯誤
SELECT * FROM dbo.customers WHERE account_status = @status

-- ✅ 正確：使用 : 可正常執行
SELECT * FROM dbo.customers WHERE account_status = :status
```

### **為什麼不能用 @？**

- `@paramName` 是 T-SQL 原生格式，只能在 SQL Server 內部使用
- Spring 的 `NamedParameterJdbcTemplate` 是 JDBC 標準，使用 `:paramName`
- 兩者不相容，必須統一使用 JDBC 格式

---

## 📊 資料表清單

### **業務資料表（8 張）**

| 資料表名稱 | 說明 | 主要用途 |
|-----------|------|---------|
| `api_keys` | API 金鑰管理表 | 管理 API Key、權限、流量限制 |
| `customers` | 客戶資料表 | 儲存客戶基本資料 |
| `portfolios` | 投資組合表 | 客戶投資組合資料 |
| `transactions` | 交易記錄表 | 所有投資交易記錄 |
| `api_access_log` | API 訪問日誌表 | 記錄所有 API 請求 |
| `external_system_log` | 外部系統日誌表 | 外部系統介接記錄 |
| `system_parameters` | 系統參數表 | 系統配置參數 |
| `datasource_config` | 資料源配置表(舊) | 相容性保留 |

### **動態查詢表（3 張）**

| 資料表名稱 | 說明 | 主要用途 |
|-----------|------|---------|
| `api_datasource_config` | 資料源配置表 | 多資料源連線配置 |
| `api_query_config` | 查詢配置表 | 動態 SQL 查詢定義 |
| `api_query_params` | 查詢參數表 | SQL 參數定義與驗證 |

---

## 🔑 重要資料表關係

```
customers (客戶)
    ↓ 1:N
portfolios (投資組合)
    ↓ 1:N
transactions (交易記錄)

api_query_config (查詢配置)
    ↓ 1:N
api_query_params (查詢參數)
```

---

## 📝 測試資料摘要

執行 `02_insert_sample_data_sqlserver.sql` 後會插入：

| 資料類型 | 數量 | 說明 |
|---------|------|------|
| API Keys | 5 筆 | 包含生產/測試/行動/網銀/合作夥伴 |
| 客戶資料 | 10 筆 | 張三~陳一二，涵蓋各風險等級 |
| 投資組合 | 15 筆 | TWD/USD 多幣別組合 |
| 交易記錄 | 15 筆 | 買賣/股利/手續費等交易 |
| 系統參數 | 15 筆 | API 版本、限制、快取等配置 |
| 資料源配置 | 4 筆 | 主庫/報表/歸檔/外部資料庫 |
| 查詢配置 | 8 筆 | 客戶/組合/交易/報表查詢 |
| 查詢參數 | 10 筆 | 各查詢所需參數定義 |
| API 日誌 | 5 筆 | 模擬 API 訪問記錄 |

---

## 🔧 常用驗證 SQL

### **檢查所有資料表**
```sql
SELECT 
    t.name AS 資料表名稱,
    SUM(p.rows) AS 資料筆數
FROM sys.tables t
LEFT JOIN sys.partitions p ON t.object_id = p.object_id
WHERE t.schema_id = SCHEMA_ID('dbo')
    AND p.index_id IN (0, 1)
GROUP BY t.name
ORDER BY t.name;
```

### **檢查外鍵關聯**
```sql
SELECT 
    fk.name AS 外鍵名稱,
    OBJECT_NAME(fk.parent_object_id) AS 子表,
    OBJECT_NAME(fk.referenced_object_id) AS 父表
FROM sys.foreign_keys fk
ORDER BY 子表;
```

### **檢查索引**
```sql
SELECT 
    t.name AS 資料表名稱,
    i.name AS 索引名稱,
    i.type_desc AS 索引類型
FROM sys.indexes i
INNER JOIN sys.tables t ON i.object_id = t.object_id
WHERE t.schema_id = SCHEMA_ID('dbo')
    AND i.name IS NOT NULL
ORDER BY t.name, i.name;
```

---

## ⚠️ 重要注意事項

### **1. 關於 datasource_config 和 api_datasource_config**

- **`api_datasource_config`** ← ✅ **新版，JPA 實體對應**
- **`datasource_config`** ← ⚠️ **舊版，僅供相容**

**建議：**
- 新開發請使用 `api_datasource_config`
- 舊系統可同時保留兩張表進行過渡
- 最終可刪除 `datasource_config`

### **2. 自動更新觸發器**

以下資料表已建立 `updated_at` 自動更新觸發器：
- `api_keys`
- `customers`
- `portfolios`
- `transactions`
- `system_parameters`
- `api_query_config`
- `api_datasource_config`
- `datasource_config`

### **3. 預設資料源配置**

測試資料中的 `PRIMARY_DB` 必須與 `application.yml` 中的主資料庫配置一致：
```yaml
spring:
  datasource:
    url: jdbc:sqlserver://localhost:1433;databaseName=api_db;...
    username: api_user
```

---

## 🗑️ 舊版 SQL 檔案處理建議

### **可以刪除的檔案：**

| 檔案名稱 | 原因 | 替代方案 |
|---------|------|---------|
| `05_rebuild_dynamic_query_sqlserver.sql` | 功能已整合至 `01_create_tables_sqlserver.sql` | 直接使用 01 腳本 |
| `06_migrate_dynamic_query_sqlserver.sql` | 僅用於舊系統升級，全新安裝不需要 | 舊系統才需要此遷移腳本 |

### **保留條件：**

如果您的系統是從舊版本升級，且已有生產資料：
1. **保留** `06_migrate_dynamic_query_sqlserver.sql` 用於資料遷移
2. **執行順序：** 01 → 06（遷移） → 02（測試資料可選）

如果是全新安裝：
1. **刪除** 05 和 06 兩個檔案
2. **只需執行：** 01 → 02

---

## 🔄 資料庫遷移策略

### **情境一：全新安裝**
```bash
# 1. 建立資料庫
sqlcmd -S localhost -U sa -P YourPassword -Q "CREATE DATABASE api_db"

# 2. 執行建表
sqlcmd -S localhost -U sa -P YourPassword -d api_db -i 01_create_tables_sqlserver.sql

# 3. 插入測試資料
sqlcmd -S localhost -U sa -P YourPassword -d api_db -i 02_insert_sample_data_sqlserver.sql
```

### **情境二：舊系統升級（有 datasource_config 表）**
```bash
# 1. 備份資料庫
sqlcmd -S localhost -U sa -P YourPassword -Q "BACKUP DATABASE api_db TO DISK='C:\backup\api_db.bak'"

# 2. 執行遷移腳本（需要保留 06 檔案）
sqlcmd -S localhost -U sa -P YourPassword -d api_db -i 06_migrate_dynamic_query_sqlserver.sql

# 3. 驗證遷移結果
sqlcmd -S localhost -U sa -P YourPassword -d api_db -Q "SELECT * FROM api_datasource_config"
```

---

## 📌 快速測試查詢

### **測試 API Key 認證**
```sql
-- 驗證有效的 API Key
SELECT * FROM dbo.api_keys 
WHERE api_key = 'api_live_1234567890abcdef1234567890abcdef' 
  AND status = 'ACTIVE';
```

### **測試客戶查詢**
```sql
-- 查詢客戶代碼
SELECT * FROM dbo.customers WHERE customer_code = 'C0001';

-- 查詢客戶的投資組合
SELECT p.*, c.name AS customer_name 
FROM dbo.portfolios p
INNER JOIN dbo.customers c ON p.customer_id = c.customer_id
WHERE c.customer_code = 'C0001';
```

### **測試動態查詢配置**
```sql
-- 查看所有可用的查詢配置
SELECT query_code, query_name, category, is_enabled 
FROM dbo.api_query_config 
WHERE is_enabled = 1;

-- 查看查詢的參數定義
SELECT 
    qc.query_code,
    qp.param_name,
    qp.param_type,
    qp.is_required,
    qp.default_value
FROM dbo.api_query_config qc
INNER JOIN dbo.api_query_params qp ON qc.query_id = qp.query_id
WHERE qc.query_code = 'LIST_CUSTOMERS';
```

---

## 🐛 常見問題排查

### **問題 1: 外鍵約束錯誤**
```sql
-- 檢查外鍵依賴
SELECT 
    fk.name,
    OBJECT_NAME(fk.parent_object_id) AS 子表,
    OBJECT_NAME(fk.referenced_object_id) AS 父表
FROM sys.foreign_keys fk;

-- 解決方案: 按順序刪除表（先子表後父表）
```

### **問題 2: IDENTITY 插入錯誤**
```sql
-- 確認 IDENTITY_INSERT 已開啟
SET IDENTITY_INSERT dbo.api_keys ON;
-- 插入資料...
SET IDENTITY_INSERT dbo.api_keys OFF;
```

### **問題 3: 觸發器未自動更新 updated_at**
```sql
-- 檢查觸發器是否存在
SELECT name, is_disabled 
FROM sys.triggers 
WHERE parent_id = OBJECT_ID('dbo.customers');

-- 手動啟用觸發器
ALTER TABLE dbo.customers ENABLE TRIGGER trg_customers_update;
```

---

## 📞 技術支援

如有問題，請檢查：
1. SQL Server 版本（建議 2022）
2. 資料庫編碼（UTF-8）
3. 使用者權限（需要 DDL 權限）
4. 外鍵約束是否正確

---

## 📅 版本歷史

| 版本 | 日期 | 說明 |
|------|------|------|
| v2.0 | 2025-10-21 | 整合動態查詢表，新增完整測試資料 |
| v1.5 | 2025-10-14 | 新增 api_datasource_config 表 |
| v1.0 | 2025-01-08 | 初始版本 |

---

**✅ 腳本已整合完成，舊版 05、06 檔案可以安全刪除！**