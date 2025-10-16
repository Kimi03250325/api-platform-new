package com.bank.apiplatform.controller;

import com.bank.apiplatform.dto.response.ApiResponse;
import com.bank.apiplatform.dto.request.DynamicQueryRequest;
import com.bank.apiplatform.dto.request.LegacyDynamicQueryRequest;
import com.bank.apiplatform.service.DynamicQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 動態查詢 API Controller
 */
@RestController
@RequestMapping("/api/query")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "動態查詢API", description = "配置化通用查詢端點 - 支援多資料源")
public class DynamicQueryController {
    
    private final DynamicQueryService dynamicQueryService;
    
    /**
     * 查詢可用的查詢配置列表
     */
    @GetMapping("/available")
    @Operation(
        summary = "📋 查詢可用的查詢代碼列表",
        description = "列出所有已啟用的查詢配置，包含查詢代碼、名稱、分類、描述等資訊。\n\n" +
                     "**使用流程：**\n" +
                     "1. 先呼叫此端點查看所有可用的 queryCode\n" +
                     "2. 使用 `/api/query/config/{queryCode}` 查看該查詢的參數定義\n" +
                     "3. 使用 `/api/query/execute/{queryCode}` 執行查詢"
    )
    public ApiResponse<List<Map<String, Object>>> getAvailableQueries(
            @Parameter(description = "分類篩選 (CUSTOMER/PORTFOLIO/TRANSACTION)", required = false)
            @RequestParam(required = false) String category
    ) {
        try {
            List<Map<String, Object>> queries = dynamicQueryService.getAvailableQueries(category);
            return ApiResponse.success("查詢成功", queries);
        } catch (Exception e) {
            log.error("查詢可用配置失敗: {}", e.getMessage(), e);
            return ApiResponse.error(500, "查詢失敗: " + e.getMessage());
        }
    }
    
    /**
     * 查詢配置詳細資訊
     */
    @GetMapping("/config/{queryCode}")
    @Operation(
        summary = "📖 查詢配置詳情",
        description = "取得指定查詢代碼的詳細資訊，包含參數定義、類型、是否必填等。"
    )
    public ApiResponse<Map<String, Object>> getQueryConfig(
            @Parameter(description = "查詢代碼", required = true, example = "LIST_CUSTOMERS")
            @PathVariable String queryCode
    ) {
        try {
            Map<String, Object> config = dynamicQueryService.getQueryConfig(queryCode);
            return ApiResponse.success("查詢成功", config);
        } catch (Exception e) {
            log.error("查詢配置詳情失敗: queryCode={}, 錯誤: {}", queryCode, e.getMessage(), e);
            return ApiResponse.error(500, "查詢失敗: " + e.getMessage());
        }
    }
    
    /**
     * GET 方式執行動態查詢 - 通用版本
     */
    @GetMapping("/execute/{queryCode}")
    @Operation(
        summary = "🔍 執行動態查詢 (GET)",
        description = "透過查詢代碼執行預先配置的SQL查詢。不同的查詢代碼需要不同的參數。"
    )
    public ApiResponse<Map<String, Object>> executeQuery(
            @Parameter(description = "查詢代碼", required = true, example = "LIST_CUSTOMERS")
            @PathVariable String queryCode,
            
            @Parameter(description = "查詢參數（動態）", required = false)
            @RequestParam(required = false) Map<String, Object> params,
            
            @Parameter(description = "頁碼", required = false, example = "1")
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            
            @Parameter(description = "每頁筆數", required = false, example = "20")
            @RequestParam(required = false, defaultValue = "20") Integer pageSize
    ) {
        try {
            if (pageNum != null && pageNum < 1) {
                return ApiResponse.error(400, "pageNum 必須大於等於 1");
            }
            
            if (pageSize != null && pageSize < 1) {
                return ApiResponse.error(400, "pageSize 必須大於等於 1");
            }
            
            if (pageSize != null && pageSize > 1000) {
                return ApiResponse.error(400, "pageSize 不能超過 1000");
            }
            
            log.info("動態查詢請求(GET): queryCode={}, params={}, page={}/{}", 
                     queryCode, params, pageNum, pageSize);
            
            Map<String, Object> result = dynamicQueryService.executeQuery(
                queryCode, params, pageNum, pageSize
            );
            
            return ApiResponse.success("查詢成功", result);
            
        } catch (IllegalArgumentException e) {
            log.warn("參數驗證失敗: queryCode={}, 錯誤: {}", queryCode, e.getMessage());
            return ApiResponse.error(400, "參數錯誤: " + e.getMessage());
        } catch (Exception e) {
            log.error("動態查詢執行失敗(GET): queryCode={}, 錯誤: {}", queryCode, e.getMessage(), e);
            return ApiResponse.error(500, "查詢失敗: " + e.getMessage());
        }
    }
    
    /**
     * GET 方式執行 - GET_CUSTOMER_DETAIL 專用端點
     */
    @GetMapping("/execute-customer-detail")
    @Operation(
        summary = "🔍 查詢客戶詳情（專用端點）",
        description = "查詢單一客戶的完整資料。"
    )
    public ApiResponse<Map<String, Object>> getCustomerDetail(
            @Parameter(description = "客戶ID", required = true, example = "1")
            @RequestParam Integer customer_id,
            
            @Parameter(description = "頁碼", required = false, example = "1")
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            
            @Parameter(description = "每頁筆數", required = false, example = "20")
            @RequestParam(required = false, defaultValue = "20") Integer pageSize
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("customer_id", customer_id);
            
            Map<String, Object> result = dynamicQueryService.executeQuery(
                "GET_CUSTOMER_DETAIL", params, pageNum, pageSize
            );
            
            return ApiResponse.success("查詢成功", result);
            
        } catch (Exception e) {
            log.error("查詢客戶詳情失敗: customer_id={}, 錯誤: {}", customer_id, e.getMessage(), e);
            return ApiResponse.error(500, "查詢失敗: " + e.getMessage());
        }
    }
    
    /**
     * GET 方式執行 - LIST_CUSTOMERS 專用端點
     */
    @GetMapping("/execute-customers")
    @Operation(
        summary = "🔍 查詢客戶列表（專用端點）",
        description = "查詢客戶列表，支援狀態篩選和分頁。\n\n" +
                     "**使用說明：**\n" +
                     "- status 參數為選填\n" +
                     "- 不填寫 status 將返回所有狀態的客戶\n" +
                     "- 填寫 status 只返回該狀態的客戶"
    )
    public ApiResponse<Map<String, Object>> listCustomers(
            @Parameter(description = "帳戶狀態 (ACTIVE/INACTIVE/SUSPENDED)，選填", required = false, example = "ACTIVE")
            @RequestParam(required = false) String status,
            
            @Parameter(description = "頁碼", required = false, example = "1")
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            
            @Parameter(description = "每頁筆數", required = false, example = "20")
            @RequestParam(required = false, defaultValue = "20") Integer pageSize
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            
            // 🔧 只有當 status 不為空時才加入
            if (status != null && !status.trim().isEmpty()) {
                params.put("status", status.trim());
            }
            
log.info("查詢客戶列表: status={}", status);
            
            Map<String, Object> result = dynamicQueryService.executeQuery(
                "LIST_CUSTOMERS", params, pageNum, pageSize
            );
            
            return ApiResponse.success("查詢成功", result);
            
        } catch (Exception e) {
            log.error("查詢客戶列表失敗: status={}, 錯誤: {}", status, e.getMessage(), e);
            return ApiResponse.error(500, "查詢失敗: " + e.getMessage());
        }
    }
    
    /**
     * GET 方式執行 - SEARCH_CUSTOMERS 專用端點
     */
    @GetMapping("/execute-search-customers")
    @Operation(
        summary = "🔍 搜尋客戶（專用端點）",
        description = "根據姓名、電話或身分證號搜尋客戶。\n\n" +
                     "**使用說明：**\n" +
                     "- 所有參數都是選填的\n" +
                     "- 可以只填寫一個或多個條件\n" +
                     "- 未填寫的參數不會加入查詢條件\n" +
                     "- 如果所有參數都不填，將返回所有客戶\n\n" +
                     "**範例：**\n" +
                     "- 只搜尋姓名：`?name=張`\n" +
                     "- 只搜尋電話：`?phone=0912`\n" +
                     "- 組合搜尋：`?name=張&phone=0912`"
    )
    public ApiResponse<Map<String, Object>> searchCustomers(
            @Parameter(description = "客戶姓名（模糊搜尋，選填）", required = false, example = "張")
            @RequestParam(required = false) String name,
            
            @Parameter(description = "電話號碼（模糊搜尋，選填）", required = false, example = "0912")
            @RequestParam(required = false) String phone,
            
            @Parameter(description = "身分證號（精確搜尋，選填）", required = false, example = "A123456789")
            @RequestParam(required = false) String id_number,
            
            @Parameter(description = "頁碼", required = false, example = "1")
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            
            @Parameter(description = "每頁筆數", required = false, example = "10")
            @RequestParam(required = false, defaultValue = "10") Integer pageSize
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            
            // 🔧 只有當參數不為空時才加入
            if (name != null && !name.trim().isEmpty()) {
                params.put("name", name.trim());
            }
            if (phone != null && !phone.trim().isEmpty()) {
                params.put("phone", phone.trim());
            }
            if (id_number != null && !id_number.trim().isEmpty()) {
                params.put("id_number", id_number.trim());
            }
            
            log.info("搜尋客戶: params={}", params);
            
            Map<String, Object> result = dynamicQueryService.executeQuery(
                "SEARCH_CUSTOMERS", params, pageNum, pageSize
            );
            
            return ApiResponse.success("查詢成功", result);
            
        } catch (Exception e) {
            log.error("搜尋客戶失敗: 錯誤: {}", e.getMessage(), e);
            return ApiResponse.error(500, "查詢失敗: " + e.getMessage());
        }
    }
    
    /**
     * POST 方式執行動態查詢
     */
    @PostMapping("/execute/{queryCode}")
    @Operation(
        summary = "🔍 執行動態查詢 (POST)",
        description = "透過查詢代碼執行預先配置的SQL查詢（使用 POST 方式傳遞複雜參數）。",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = false,
            content = @Content(
                schema = @Schema(implementation = DynamicQueryRequest.class),
                examples = {
                    @ExampleObject(
                        name = "查詢客戶列表",
                        summary = "LIST_CUSTOMERS",
                        value = "{\n  \"params\": {\n    \"status\": \"ACTIVE\"\n  },\n  \"pageNum\": 1,\n  \"pageSize\": 20\n}"
                    ),
                    @ExampleObject(
                        name = "查詢客戶詳情",
                        summary = "GET_CUSTOMER_DETAIL",
                        value = "{\n  \"params\": {\n    \"customer_id\": 1\n  },\n  \"pageNum\": 1,\n  \"pageSize\": 20\n}"
                    ),
                    @ExampleObject(
                        name = "搜尋客戶",
                        summary = "SEARCH_CUSTOMERS",
                        value = "{\n  \"params\": {\n    \"name\": \"張\",\n    \"phone\": \"0912\"\n  },\n  \"pageNum\": 1,\n  \"pageSize\": 10\n}"
                    )
                }
            )
        )
    )
    public ApiResponse<Map<String, Object>> executeQueryPost(
            @Parameter(description = "查詢代碼", required = true, example = "LIST_CUSTOMERS")
            @PathVariable String queryCode,
            
            @RequestBody(required = false) DynamicQueryRequest request
    ) {
        try {
            Integer pageNum = request != null ? request.getPageNum() : null;
            Integer pageSize = request != null ? request.getPageSize() : null;
            
            if (pageNum != null && pageNum < 1) {
                return ApiResponse.error(400, "pageNum 必須大於等於 1");
            }
            
            if (pageSize != null && (pageSize < 1 || pageSize > 1000)) {
                return ApiResponse.error(400, "pageSize 必須在 1-1000 之間");
            }
            
            log.info("動態查詢請求(POST): queryCode={}, request={}", queryCode, request);
            
            Map<String, Object> result = dynamicQueryService.executeQuery(
                queryCode,
                request != null ? request.getParams() : null,
                pageNum,
                pageSize
            );
            
            return ApiResponse.success("查詢成功", result);
            
        } catch (IllegalArgumentException e) {
            log.warn("參數驗證失敗: queryCode={}, 錯誤: {}", queryCode, e.getMessage());
            return ApiResponse.error(400, "參數錯誤: " + e.getMessage());
        } catch (Exception e) {
            log.error("動態查詢執行失敗(POST): queryCode={}, 錯誤: {}", queryCode, e.getMessage(), e);
            return ApiResponse.error(500, "查詢失敗: " + e.getMessage());
        }
    }

    /**
     * 舊版相容端點
     */
    @PostMapping("/dynamic/query")
    @Operation(
        summary = "[相容] 舊版動態查詢入口",
        description = "為相容既有前端/工具，保留 /api/dynamic/query。建議改用 `/api/query/execute/{queryCode}`"
    )
    public ApiResponse<Map<String, Object>> executeLegacy(
            @RequestBody LegacyDynamicQueryRequest request
    ) {
        try {
            log.info("舊版動態查詢請求: {}", request);
            
            Map<String, Object> result = dynamicQueryService.executeQuery(
                request.getQueryCode(),
                request.getParams(),
                request.getPageNum(),
                request.getPageSize()
            );
            
            return ApiResponse.success("查詢成功", result);
            
        } catch (Exception e) {
            log.error("舊版動態查詢執行失敗: queryCode={}, 錯誤: {}", 
                     request != null ? request.getQueryCode() : null, e.getMessage(), e);
            return ApiResponse.error(500, "查詢失敗: " + e.getMessage());
        }
    }
}