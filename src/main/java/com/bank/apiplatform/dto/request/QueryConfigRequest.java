package com.bank.apiplatform.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * 查詢配置請求 DTO
 * 
 * 路徑: src/main/java/com/bank/apiplatform/dto/request/QueryConfigRequest.java
 * 
 * 建立方法:
 * 1. 在 Eclipse 右鍵點擊 com.bank.apiplatform.dto.request 套件
 * 2. New > Class
 * 3. Name: QueryConfigRequest
 * 4. Finish
 * 5. 複製此檔案內容，取代原內容
 * 6. Ctrl + S 儲存
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueryConfigRequest {
    
    /**
     * 查詢代碼（唯一識別碼）
     * 例: LIST_CUSTOMERS, GET_PORTFOLIO_DETAIL, SEARCH_TRANSACTIONS
     * 規則: 大寫英文字母 + 底線
     */
    @NotBlank(message = "查詢代碼不能為空")
    private String queryCode;
    
    /**
     * 查詢名稱
     * 例: 查詢客戶列表, 取得投資組合詳情, 搜尋交易記錄
     */
    @NotBlank(message = "查詢名稱不能為空")
    private String queryName;
    
    /**
     * 資料源代碼（關聯到資料源配置）
     * 例: PRIMARY_DB, REPORT_DB, ARCHIVE_DB
     * 必須是已存在且已啟用的資料源
     */
    @NotBlank(message = "資料源代碼不能為空")
    private String datasourceCode;
    
    /**
     * SQL 查詢語句
     * 使用 :paramName 格式的具名參數（JDBC 標準）
     * 
     * 範例:
     * SELECT * FROM customers WHERE account_status = :status
     * SELECT * FROM portfolios WHERE customer_id = :customerId AND status = :status
     * 
     * 注意: 
     * - 必須使用 :paramName 格式（冒號開頭）
     * - 不可使用 @paramName 格式（T-SQL 原生格式）
     */
    @NotBlank(message = "SQL 查詢語句不能為空")
    private String querySql;
    
    /**
     * 查詢描述
     * 例: 根據帳戶狀態查詢客戶列表，支援分頁，依建立時間降序排列
     */
    private String description;
    
    /**
     * 查詢分類
     * 可選值: CUSTOMER, PORTFOLIO, TRANSACTION, REPORT, OTHER
     * 
     * CUSTOMER - 客戶相關查詢
     * PORTFOLIO - 投資組合查詢
     * TRANSACTION - 交易記錄查詢
     * REPORT - 報表查詢
     * OTHER - 其他查詢
     */
    @NotBlank(message = "查詢分類不能為空")
    private String category;
    
    /**
     * 是否啟用
     * true: 啟用（可執行）
     * false: 停用（不可執行）
     */
    @NotNull(message = "啟用狀態不能為空")
    private Boolean isEnabled;
    
    /**
     * 是否需要 API Key 驗證
     * true: 需要驗證（預設）
     * false: 不需要驗證（公開查詢）
     */
    @NotNull(message = "API Key 驗證設定不能為空")
    private Boolean requireApiKey;
    
    /**
     * 最大分頁大小
     * 限制單次查詢最多可以返回的記錄數
     * 預設: 100
     * 範圍: 1 - 1000
     */
    @Min(value = 1, message = "最大分頁大小不能小於 1")
    @Max(value = 1000, message = "最大分頁大小不能大於 1000")
    private Integer maxPageSize;
    
    /**
     * 快取秒數
     * 查詢結果的快取時間（秒）
     * 0 表示不快取（預設）
     * 適用於變動較少的查詢（如參數表、設定檔）
     * 範圍: 0 - 86400 (24 小時)
     */
    @Min(value = 0, message = "快取秒數不能小於 0")
    @Max(value = 86400, message = "快取秒數不能大於 86400")
    private Integer cacheSeconds;
    
    /**
     * 建立者（可選）
     * 記錄是誰建立了這個查詢配置
     */
    private String createdBy;
    
    /**
     * 查詢超時時間（秒）（可選）
     * 預設: 30 秒
     */
    @Min(value = 1, message = "超時時間不能小於 1 秒")
    @Max(value = 300, message = "超時時間不能大於 300 秒")
    private Integer timeout;
}