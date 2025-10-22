package com.bank.apiplatform.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 查詢參數請求 DTO
 * 
 * 路徑: src/main/java/com/bank/apiplatform/dto/request/QueryParamRequest.java
 * 
 * 建立方法:
 * 1. 在 Eclipse 右鍵點擊 com.bank.apiplatform.dto.request 套件
 * 2. New > Class
 * 3. Name: QueryParamRequest
 * 4. Finish
 * 5. 複製此檔案內容，取代原內容
 * 6. Ctrl + S 儲存
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueryParamRequest {
    
    /**
     * 查詢配置 ID（外鍵）
     * 關聯到 api_query_config 表的 query_id
     */
    @NotNull(message = "查詢 ID 不能為空")
    private Long queryId;
    
    /**
     * 參數名稱
     * 對應 SQL 中的 :paramName
     * 
     * 例如:
     * SQL 中使用 :status，則此處填 status
     * SQL 中使用 :customerId，則此處填 customerId
     * SQL 中使用 :startDate，則此處填 startDate
     * 
     * 規則:
     * - 小寫駝峰命名法（推薦）: customerId, startDate, accountStatus
     * - 或底線命名法: customer_id, start_date, account_status
     * - 只能包含字母、數字、底線
     */
    @NotBlank(message = "參數名稱不能為空")
    private String paramName;
    
    /**
     * 參數類型
     * 可選值:
     * - STRING: 字串類型（例: 狀態、名稱、代碼）
     * - INTEGER: 整數類型（例: ID、數量、年齡）
     * - LONG: 長整數類型（例: 大數值 ID）
     * - DECIMAL: 小數類型（例: 金額、比率、百分比）
     * - DATETIME: 日期時間類型（例: 2025-01-08T10:30:00）
     * - DATE: 日期類型（例: 2025-01-08）
     * - BOOLEAN: 布林類型（例: true, false）
     */
    @NotBlank(message = "參數類型不能為空")
    private String paramType;
    
    /**
     * 是否必填
     * true: 必填參數（使用者必須提供）
     * false: 選填參數（使用者可以不提供）
     */
    @NotNull(message = "是否必填不能為空")
    private Boolean isRequired;
    
    /**
     * 預設值
     * 當使用者未提供參數時，使用此預設值
     * 
     * 範例:
     * - 狀態參數預設為 "ACTIVE"
     * - 頁碼預設為 "1"
     * - 每頁筆數預設為 "20"
     * 
     * 注意:
     * - 預設值必須符合參數類型
     * - 如果是必填參數，通常不需要預設值
     */
    private String defaultValue;
    
    /**
     * 驗證正則表達式（選填）
     * 用於驗證參數值的格式是否正確
     * 
     * 常用範例:
     * - 客戶代碼: ^C\d{4}$ (例: C0001, C0002)
     * - 日期格式: ^\d{4}-\d{2}-\d{2}$ (例: 2025-01-08)
     * - 狀態值: ^(ACTIVE|INACTIVE|SUSPENDED)$ (只允許這三個值)
     * - Email: ^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$
     * - 手機號碼: ^09\d{8}$ (台灣手機格式)
     * - 身分證: ^[A-Z]\d{9}$ (台灣身分證格式)
     */
    private String validationRegex;
    
    /**
     * 參數描述
     * 說明此參數的用途、格式、可選值等
     * 
     * 範例:
     * - "客戶帳戶狀態，可選值：ACTIVE（正常）、INACTIVE（停用）、SUSPENDED（凍結）"
     * - "查詢開始日期，格式：YYYY-MM-DD，例如：2025-01-08"
     * - "客戶 ID，必須是有效的客戶編號"
     */
    private String description;
    
    /**
     * 參數順序（可選）
     * 在 UI 介面上顯示參數的順序
     * 數字越小越靠前
     */
    private Integer displayOrder;
    
    /**
     * 參數標籤（可選）
     * 在 UI 介面上顯示的友善名稱
     * 例如: paramName 是 customerId，標籤可以是 "客戶編號"
     */
    private String label;
    
    /**
     * 範例值（可選）
     * 提供一個範例值供參考
     * 例如: "ACTIVE", "2025-01-08", "C0001"
     */
    private String exampleValue;
}