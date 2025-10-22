package com.bank.apiplatform.controller;

import com.bank.apiplatform.dto.request.QueryParamRequest;
import com.bank.apiplatform.dto.response.ApiResponse;
import com.bank.apiplatform.entity.ApiQueryParam;
import com.bank.apiplatform.service.QueryParamService;
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
 * 查詢參數管理 API 控制器
 * 
 * 路徑: src/main/java/com/bank/apiplatform/controller/QueryParamAdminController.java
 * 
 * 建立方法:
 * 1. 打開現有的 QueryParamAdminController.java
 * 2. 刪除所有內容
 * 3. 複製此檔案的完整內容
 * 4. Ctrl + S 儲存
 */
@RestController
@RequestMapping("/api/query/param")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "查詢參數管理", description = "查詢參數的 CRUD 操作")
public class QueryParamAdminController {
    
    private final QueryParamService queryParamService;
    
    /**
     * 查詢指定查詢的所有參數
     */
    @GetMapping("/list/{queryId}")
    @Operation(
        summary = "📋 查詢參數列表",
        description = "取得指定查詢配置的所有參數定義"
    )
    public ApiResponse<List<ApiQueryParam>> listByQueryId(
            @Parameter(description = "查詢配置 ID", required = true, example = "1")
            @PathVariable Integer queryId) {
        try {
            log.info("查詢參數: queryId={}", queryId);
            List<ApiQueryParam> list = queryParamService.findByQueryId(queryId);
            log.info("查詢成功，共 {} 個參數", list.size());
            return ApiResponse.success("查詢成功", list);
        } catch (Exception e) {
            log.error("查詢參數失敗: queryId={}", queryId, e);
            return ApiResponse.error(500, "查詢失敗: " + e.getMessage());
        }
    }
    
    /**
     * 查詢必填參數
     */
    @GetMapping("/list/{queryId}/required")
    @Operation(
        summary = "⚠️ 查詢必填參數",
        description = "取得指定查詢配置的所有必填參數"
    )
    public ApiResponse<List<ApiQueryParam>> listRequiredParams(
            @Parameter(description = "查詢配置 ID", required = true, example = "1")
            @PathVariable Integer queryId) {
        try {
            log.info("查詢必填參數: queryId={}", queryId);
            List<ApiQueryParam> list = queryParamService.findRequiredParams(queryId);
            log.info("查詢成功，共 {} 個必填參數", list.size());
            return ApiResponse.success("查詢成功", list);
        } catch (Exception e) {
            log.error("查詢必填參數失敗: queryId={}", queryId, e);
            return ApiResponse.error(500, "查詢失敗: " + e.getMessage());
        }
    }
    
    /**
     * 查詢選填參數
     */
    @GetMapping("/list/{queryId}/optional")
    @Operation(
        summary = "📝 查詢選填參數",
        description = "取得指定查詢配置的所有選填參數"
    )
    public ApiResponse<List<ApiQueryParam>> listOptionalParams(
            @Parameter(description = "查詢配置 ID", required = true, example = "1")
            @PathVariable Integer queryId) {
        try {
            log.info("查詢選填參數: queryId={}", queryId);
            List<ApiQueryParam> list = queryParamService.findOptionalParams(queryId);
            log.info("查詢成功，共 {} 個選填參數", list.size());
            return ApiResponse.success("查詢成功", list);
        } catch (Exception e) {
            log.error("查詢選填參數失敗: queryId={}", queryId, e);
            return ApiResponse.error(500, "查詢失敗: " + e.getMessage());
        }
    }
    
    /**
     * 建立查詢參數
     */
    @PostMapping("/create")
    @Operation(
        summary = "➕ 建立查詢參數",
        description = "為指定的查詢配置建立新的參數定義。" +
                     "\n\n**參數類型：**" +
                     "\n- STRING: 字串" +
                     "\n- INTEGER: 整數" +
                     "\n- LONG: 長整數" +
                     "\n- DECIMAL: 小數" +
                     "\n- DATETIME: 日期時間" +
                     "\n- DATE: 日期" +
                     "\n- BOOLEAN: 布林值" +
                     "\n\n**注意：**參數名稱必須與 SQL 中的 `:paramName` 對應"
    )
    public ApiResponse<ApiQueryParam> create(
            @Valid @RequestBody QueryParamRequest request) {
        try {
            log.info("建立查詢參數: queryId={}, paramName={}", 
                    request.getQueryId(), request.getParamName());
            ApiQueryParam created = queryParamService.create(request);
            log.info("建立成功: paramId={}, paramName={}", 
                    created.getParamId(), created.getParamName());
            return ApiResponse.success("建立成功", created);
        } catch (Exception e) {
            log.error("建立參數失敗: paramName={}", request.getParamName(), e);
            return ApiResponse.error(500, "建立失敗: " + e.getMessage());
        }
    }
    
    /**
     * 更新查詢參數
     */
    @PutMapping("/update/{paramId}")
    @Operation(
        summary = "✏️ 更新查詢參數",
        description = "更新現有的參數定義"
    )
    public ApiResponse<ApiQueryParam> update(
            @Parameter(description = "參數 ID", required = true, example = "1")
            @PathVariable Long paramId,
            @Valid @RequestBody QueryParamRequest request) {
        try {
            log.info("更新查詢參數: paramId={}, paramName={}", paramId, request.getParamName());
            ApiQueryParam updated = queryParamService.update(paramId, request);
            log.info("更新成功: paramId={}, paramName={}", 
                    updated.getParamId(), updated.getParamName());
            return ApiResponse.success("更新成功", updated);
        } catch (Exception e) {
            log.error("更新參數失敗: paramId={}", paramId, e);
            return ApiResponse.error(500, "更新失敗: " + e.getMessage());
        }
    }
    
    /**
     * 刪除查詢參數
     */
    @DeleteMapping("/delete/{paramId}")
    @Operation(
        summary = "🗑️ 刪除查詢參數",
        description = "刪除指定的參數定義"
    )
    public ApiResponse<Void> delete(
            @Parameter(description = "參數 ID", required = true, example = "1")
            @PathVariable Long paramId) {
        try {
            log.info("刪除查詢參數: paramId={}", paramId);
            queryParamService.delete(paramId);
            log.info("刪除成功: paramId={}", paramId);
            return ApiResponse.success("刪除成功", null);
        } catch (Exception e) {
            log.error("刪除參數失敗: paramId={}", paramId, e);
            return ApiResponse.error(500, "刪除失敗: " + e.getMessage());
        }
    }
    
    /**
     * 批次建立參數
     */
    @PostMapping("/batch-create/{queryId}")
    @Operation(
        summary = "📦 批次建立參數",
        description = "一次為指定的查詢配置建立多個參數定義"
    )
    public ApiResponse<List<ApiQueryParam>> batchCreate(
            @Parameter(description = "查詢配置 ID", required = true, example = "1")
            @PathVariable Integer queryId,
            @Valid @RequestBody List<QueryParamRequest> requests) {
        try {
            log.info("批次建立參數: queryId={}, count={}", queryId, requests.size());
            List<ApiQueryParam> created = queryParamService.batchCreate(queryId, requests);
            log.info("批次建立成功: queryId={}, successCount={}", queryId, created.size());
            return ApiResponse.success("批次建立成功", created);
        } catch (Exception e) {
            log.error("批次建立參數失敗: queryId={}", queryId, e);
            return ApiResponse.error(500, "批次建立失敗: " + e.getMessage());
        }
    }
    
    /**
     * 刪除指定查詢的所有參數
     */
    @DeleteMapping("/delete-all/{queryId}")
    @Operation(
        summary = "🗑️ 刪除所有參數",
        description = "刪除指定查詢配置的所有參數定義"
    )
    public ApiResponse<Void> deleteByQueryId(
            @Parameter(description = "查詢配置 ID", required = true, example = "1")
            @PathVariable Integer queryId) {
        try {
            log.info("刪除查詢的所有參數: queryId={}", queryId);
            queryParamService.deleteByQueryId(queryId);
            log.info("刪除成功: queryId={}", queryId);
            return ApiResponse.success("刪除成功", null);
        } catch (Exception e) {
            log.error("刪除參數失敗: queryId={}", queryId, e);
            return ApiResponse.error(500, "刪除失敗: " + e.getMessage());
        }
    }
    
    /**
     * 統計參數數量
     */
    @GetMapping("/stats/{queryId}")
    @Operation(
        summary = "📊 統計參數數量",
        description = "統計指定查詢的參數數量（總數、必填、選填）"
    )
    public ApiResponse<Map<String, Object>> getParamStats(
            @Parameter(description = "查詢配置 ID", required = true, example = "1")
            @PathVariable Integer queryId) {
        try {
            log.info("統計參數數量: queryId={}", queryId);
            
            long totalCount = queryParamService.countByQueryId(queryId);
            long requiredCount = queryParamService.countRequiredByQueryId(queryId);
            long optionalCount = totalCount - requiredCount;
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("queryId", queryId);
            stats.put("totalCount", totalCount);
            stats.put("requiredCount", requiredCount);
            stats.put("optionalCount", optionalCount);
            
            log.info("統計完成: total={}, required={}, optional={}", 
                    totalCount, requiredCount, optionalCount);
            return ApiResponse.success("統計完成", stats);
        } catch (Exception e) {
            log.error("統計參數數量失敗: queryId={}", queryId, e);
            return ApiResponse.error(500, "統計失敗: " + e.getMessage());
        }
    }
    
    /**
     * 檢查參數名稱是否存在
     */
    @GetMapping("/exists/{queryId}/{paramName}")
    @Operation(
        summary = "🔍 檢查參數名稱是否存在",
        description = "檢查指定查詢中是否已存在該參數名稱"
    )
    public ApiResponse<Boolean> existsByQueryIdAndParamName(
            @Parameter(description = "查詢配置 ID", required = true, example = "1")
            @PathVariable Integer queryId,
            @Parameter(description = "參數名稱", required = true, example = "status")
            @PathVariable String paramName) {
        try {
            log.info("檢查參數名稱: queryId={}, paramName={}", queryId, paramName);
            boolean exists = queryParamService.existsByQueryIdAndParamName(queryId, paramName);
            log.info("檢查完成: exists={}", exists);
            
            Map<String, Object> result = new HashMap<>();
            result.put("queryId", queryId);
            result.put("paramName", paramName);
            result.put("exists", exists);
            
            return ApiResponse.success("檢查完成", exists);
        } catch (Exception e) {
            log.error("檢查失敗: queryId={}, paramName={}", queryId, paramName, e);
            return ApiResponse.error(500, "檢查失敗: " + e.getMessage());
        }
    }
}