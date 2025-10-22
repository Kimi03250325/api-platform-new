package com.bank.apiplatform.controller;

import com.bank.apiplatform.dto.request.QueryConfigRequest;
import com.bank.apiplatform.dto.response.ApiResponse;
import com.bank.apiplatform.entity.ApiQueryConfig;
import com.bank.apiplatform.service.QueryConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 查詢配置管理 API 控制器（修正路徑衝突版）
 * 
 * 修正內容：
 * - 將基礎路徑從 /api/query/config 改為 /api/query/admin
 * - 避免與 DynamicQueryController 的路徑衝突
 * 
 * 路徑: src/main/java/com/bank/apiplatform/controller/QueryConfigAdminController.java
 */
@RestController
@RequestMapping("/api/query/admin")  // ← 修正：改為 /admin 避免衝突
@RequiredArgsConstructor
@Slf4j
@Tag(name = "查詢配置管理", description = "查詢配置的 CRUD 操作")
public class QueryConfigAdminController {
    
    private final QueryConfigService queryConfigService;
    
    /**
     * 查詢所有查詢配置
     */
    @GetMapping("/list")
    @Operation(
        summary = "📋 查詢所有查詢配置",
        description = "取得系統中所有的查詢配置列表，可選擇性依分類篩選"
    )
    public ApiResponse<List<ApiQueryConfig>> listAll(
            @Parameter(description = "分類篩選 (CUSTOMER/PORTFOLIO/TRANSACTION/REPORT)", required = false)
            @RequestParam(required = false) String category) {
        try {
            log.info("查詢配置列表: category={}", category);
            
            List<ApiQueryConfig> list;
            if (category != null && !category.trim().isEmpty()) {
                list = queryConfigService.findByCategory(category);
            } else {
                list = queryConfigService.findAll();
            }
            
            log.info("查詢成功，共 {} 筆配置", list.size());
            return ApiResponse.success("查詢成功", list);
        } catch (Exception e) {
            log.error("查詢配置失敗", e);
            return ApiResponse.error(500, "查詢失敗: " + e.getMessage());
        }
    }
    
    /**
     * 查詢所有已啟用的配置
     */
    @GetMapping("/list/enabled")
    @Operation(
        summary = "✅ 查詢已啟用的配置",
        description = "取得所有已啟用的查詢配置列表"
    )
    public ApiResponse<List<ApiQueryConfig>> listEnabled() {
        try {
            log.info("查詢已啟用的配置");
            List<ApiQueryConfig> list = queryConfigService.findAllEnabled();
            log.info("查詢成功，共 {} 筆已啟用的配置", list.size());
            return ApiResponse.success("查詢成功", list);
        } catch (Exception e) {
            log.error("查詢已啟用配置失敗", e);
            return ApiResponse.error(500, "查詢失敗: " + e.getMessage());
        }
    }
    
    /**
     * 根據查詢代碼查詢配置
     */
    @GetMapping("/detail/{queryCode}")  // ← 修正：改為 /detail/{queryCode}
    @Operation(
        summary = "🔍 查詢配置詳情",
        description = "根據查詢代碼取得查詢配置的詳細資訊，包含參數定義"
    )
    public ApiResponse<ApiQueryConfig> getByCode(
            @Parameter(description = "查詢代碼", required = true, example = "LIST_CUSTOMERS")
            @PathVariable String queryCode) {
        try {
            log.info("查詢配置: queryCode={}", queryCode);
            ApiQueryConfig config = queryConfigService.findByQueryCode(queryCode);
            log.info("查詢成功: id={}, queryName={}", config.getQueryId(), config.getQueryName());
            return ApiResponse.success("查詢成功", config);
        } catch (Exception e) {
            log.error("查詢配置失敗: queryCode={}", queryCode, e);
            return ApiResponse.error(500, "查詢失敗: " + e.getMessage());
        }
    }
    
    /**
     * 建立查詢配置
     */
    @PostMapping("/create")
    @Operation(
        summary = "➕ 建立查詢配置",
        description = "建立新的查詢配置。" +
                     "\n\n**重要提醒：**" +
                     "\n- 查詢代碼必須唯一，建議使用大寫英文字母和底線" +
                     "\n- SQL 參數必須使用 `:paramName` 格式（JDBC 標準）" +
                     "\n- 不可使用 `@paramName` 格式（T-SQL 原生格式）" +
                     "\n\n**範例：**" +
                     "\n```sql" +
                     "\nSELECT * FROM customers WHERE status = :status" +
                     "\n```"
    )
    public ApiResponse<ApiQueryConfig> create(
            @Valid @RequestBody QueryConfigRequest request) {
        try {
            log.info("建立查詢配置: queryCode={}", request.getQueryCode());
            ApiQueryConfig created = queryConfigService.create(request);
            log.info("建立成功: id={}, queryCode={}", 
                    created.getQueryId(), created.getQueryCode());
            return ApiResponse.success("建立成功", created);
        } catch (Exception e) {
            log.error("建立查詢配置失敗: queryCode={}", request.getQueryCode(), e);
            return ApiResponse.error(500, "建立失敗: " + e.getMessage());
        }
    }
    
    /**
     * 更新查詢配置
     */
    @PutMapping("/update/{queryCode}")
    @Operation(
        summary = "✏️ 更新查詢配置",
        description = "更新現有的查詢配置"
    )
    public ApiResponse<ApiQueryConfig> update(
            @Parameter(description = "查詢代碼", required = true, example = "LIST_CUSTOMERS")
            @PathVariable String queryCode,
            @Valid @RequestBody QueryConfigRequest request) {
        try {
            log.info("更新查詢配置: queryCode={}", queryCode);
            ApiQueryConfig updated = queryConfigService.update(queryCode, request);
            log.info("更新成功: id={}, queryCode={}", 
                    updated.getQueryId(), updated.getQueryCode());
            return ApiResponse.success("更新成功", updated);
        } catch (Exception e) {
            log.error("更新查詢配置失敗: queryCode={}", queryCode, e);
            return ApiResponse.error(500, "更新失敗: " + e.getMessage());
        }
    }
    
    /**
     * 刪除查詢配置
     */
    @DeleteMapping("/delete/{queryCode}")
    @Operation(
        summary = "🗑️ 刪除查詢配置",
        description = "刪除指定的查詢配置。注意：會同時刪除該查詢的所有參數定義（級聯刪除）"
    )
    public ApiResponse<Void> delete(
            @Parameter(description = "查詢代碼", required = true, example = "LIST_CUSTOMERS")
            @PathVariable String queryCode) {
        try {
            log.info("刪除查詢配置: queryCode={}", queryCode);
            queryConfigService.delete(queryCode);
            log.info("刪除成功: queryCode={}", queryCode);
            return ApiResponse.success("刪除成功", null);
        } catch (Exception e) {
            log.error("刪除查詢配置失敗: queryCode={}", queryCode, e);
            return ApiResponse.error(500, "刪除失敗: " + e.getMessage());
        }
    }
    
    /**
     * 驗證 SQL 語法
     */
    @PostMapping("/validate-sql")
    @Operation(
        summary = "✅ 驗證 SQL 語法",
        description = "驗證 SQL 語句的參數格式是否正確。" +
                     "\n\n**檢查項目：**" +
                     "\n- 是否使用了錯誤的 `@paramName` 格式" +
                     "\n- 是否正確使用 `:paramName` 格式" +
                     "\n- 偵測所有參數名稱"
    )
    public ApiResponse<Map<String, Object>> validateSql(
            @Parameter(description = "SQL 語句", required = true)
            @RequestBody String sql) {
        try {
            log.info("驗證 SQL 語法");
            String validationResult = queryConfigService.validateSql(sql);
            
            // 提取參數名稱列表
            List<String> paramNames = queryConfigService.extractParamNames(sql);
            
            Map<String, Object> result = new HashMap<>();
            result.put("isValid", true);
            result.put("message", validationResult);
            result.put("paramNames", paramNames);
            result.put("paramCount", paramNames.size());
            
            log.info("驗證完成: {} 個參數", paramNames.size());
            return ApiResponse.success("驗證完成", result);
        } catch (Exception e) {
            log.error("SQL 驗證失敗", e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("isValid", false);
            result.put("message", e.getMessage());
            result.put("paramNames", List.of());
            result.put("paramCount", 0);
            
            return ApiResponse.error(400, "驗證失敗: " + e.getMessage(), result);
        }
    }
    
    /**
     * 根據資料源代碼查詢配置
     */
    @GetMapping("/datasource/{datasourceCode}")
    @Operation(
        summary = "🔍 根據資料源查詢配置",
        description = "查詢使用指定資料源的所有查詢配置"
    )
    public ApiResponse<List<ApiQueryConfig>> getByDatasourceCode(
            @Parameter(description = "資料源代碼", required = true, example = "PRIMARY_DB")
            @PathVariable String datasourceCode) {
        try {
            log.info("查詢配置: datasourceCode={}", datasourceCode);
            List<ApiQueryConfig> list = queryConfigService.findByDatasourceCode(datasourceCode);
            log.info("查詢成功，共 {} 筆配置", list.size());
            return ApiResponse.success("查詢成功", list);
        } catch (Exception e) {
            log.error("查詢配置失敗: datasourceCode={}", datasourceCode, e);
            return ApiResponse.error(500, "查詢失敗: " + e.getMessage());
        }
    }
    
    /**
     * 檢查查詢代碼是否存在
     */
    @GetMapping("/exists/{queryCode}")
    @Operation(
        summary = "🔍 檢查查詢代碼是否存在",
        description = "檢查指定的查詢代碼是否已被使用"
    )
    public ApiResponse<Boolean> existsByQueryCode(
            @Parameter(description = "查詢代碼", required = true, example = "LIST_CUSTOMERS")
            @PathVariable String queryCode) {
        try {
            log.info("檢查查詢代碼: queryCode={}", queryCode);
            boolean exists = queryConfigService.existsByQueryCode(queryCode);
            log.info("檢查完成: exists={}", exists);
            
            return ApiResponse.success("檢查完成", exists);
        } catch (Exception e) {
            log.error("檢查失敗: queryCode={}", queryCode, e);
            return ApiResponse.error(500, "檢查失敗: " + e.getMessage());
        }
    }
    
    /**
     * 統計各分類的查詢數量
     */
    @GetMapping("/stats/category")
    @Operation(
        summary = "📊 統計各分類查詢數量",
        description = "統計每個分類下的查詢配置數量"
    )
    public ApiResponse<Map<String, Long>> getStatsByCategory() {
        try {
            log.info("統計各分類查詢數量");
            
            Map<String, Long> stats = new HashMap<>();
            stats.put("CUSTOMER", queryConfigService.countByCategory("CUSTOMER"));
            stats.put("PORTFOLIO", queryConfigService.countByCategory("PORTFOLIO"));
            stats.put("TRANSACTION", queryConfigService.countByCategory("TRANSACTION"));
            stats.put("REPORT", queryConfigService.countByCategory("REPORT"));
            stats.put("OTHER", queryConfigService.countByCategory("OTHER"));
            
            long total = stats.values().stream().mapToLong(Long::longValue).sum();
            stats.put("TOTAL", total);
            
            log.info("統計完成: 共 {} 筆配置", total);
            return ApiResponse.success("統計完成", stats);
        } catch (Exception e) {
            log.error("統計失敗", e);
            return ApiResponse.error(500, "統計失敗: " + e.getMessage());
        }
    }
}