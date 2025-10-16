package com.bank.apiplatform.controller;

import com.bank.apiplatform.dto.response.ApiResponse;
import com.bank.apiplatform.dto.request.DynamicQueryRequest;
import com.bank.apiplatform.dto.request.LegacyDynamicQueryRequest;
import com.bank.apiplatform.service.DynamicQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
     * 統一動態查詢端點
     * 
     * 使用範例:
     * GET /api/query/execute/customer.search?name=張三&pageNum=1&pageSize=20
     */
    @GetMapping("/execute/{queryCode}")
    @Operation(
        summary = "執行動態查詢",
        description = "透過查詢代碼執行預先配置的SQL查詢，支援多資料源、分頁、參數驗證"
    )
    public ApiResponse<Map<String, Object>> executeQuery(
            @Parameter(description = "查詢代碼 (如: customer.search)", required = true)
            @PathVariable String queryCode,
            
            @Parameter(description = "查詢參數 (動態,根據查詢配置)", required = false)
            @RequestParam(required = false) Map<String, Object> params,
            
            @Parameter(description = "頁碼 (預設1)", required = false)
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            
            @Parameter(description = "每頁筆數 (預設20)", required = false)
            @RequestParam(required = false, defaultValue = "20") Integer pageSize
    ) {
        try {
            log.info("動態查詢請求: queryCode={}, params={}, page={}/{}", 
                     queryCode, params, pageNum, pageSize);
            
            Map<String, Object> result = dynamicQueryService.executeQuery(
                queryCode, params, pageNum, pageSize
            );
            
            return ApiResponse.success("查詢成功", result);
            
        } catch (Exception e) {
            log.error("動態查詢執行失敗: queryCode={}, 錯誤: {}", queryCode, e.getMessage(), e);
            return ApiResponse.error(500, "查詢失敗: " + e.getMessage());
        }
    }

    /**
     * 相容舊版入口：POST /api/dynamic/query
     * body: { queryCode, params, pageNum, pageSize }
     */
    @PostMapping("/dynamic/query")
    @Operation(
        summary = "[相容] 舊版動態查詢入口",
        description = "為相容既有前端/工具，保留 /api/dynamic/query，建議改用 /api/query/execute/{queryCode}"
    )
    public ApiResponse<Map<String, Object>> executeLegacy(@RequestBody LegacyDynamicQueryRequest request) {
        try {
            Map<String, Object> result = dynamicQueryService.executeQuery(
                request.getQueryCode(), request.getParams(), request.getPageNum(), request.getPageSize()
            );
            return ApiResponse.success("查詢成功", result);
        } catch (Exception e) {
            log.error("舊版動態查詢執行失敗: queryCode={}, 錯誤: {}", request != null ? request.getQueryCode() : null, e.getMessage(), e);
            return ApiResponse.error(500, "查詢失敗: " + e.getMessage());
        }
    }
    /**
     * 統一動態查詢端點 (POST)
     * body: { params: {...}, pageNum, pageSize }
     */
    @PostMapping("/execute/{queryCode}")
    @Operation(
        summary = "執行動態查詢 (POST)",
        description = "透過查詢代碼執行預先配置的SQL查詢，支援多資料源、分頁、參數驗證"
    )
    public ApiResponse<Map<String, Object>> executeQueryPost(
            @Parameter(description = "查詢代碼 (如: customer.search)", required = true)
            @PathVariable String queryCode,
            @RequestBody(required = false)
            @RequestBody(
                required = false,
                content = @Content(
                    schema = @Schema(implementation = DynamicQueryRequest.class),
                    examples = {
                        @ExampleObject(
                            name = "LIST_CUSTOMERS",
                            summary = "查詢客戶列表",
                            value = "{\n  \"params\": { \"status\": \"ACTIVE\" },\n  \"pageNum\": 1,\n  \"pageSize\": 20\n}"
                        ),
                        @ExampleObject(
                            name = "GET_CUSTOMER_BY_CODE",
                            summary = "依代碼查單一客戶",
                            value = "{\n  \"params\": { \"customerCode\": \"C0001\" }\n}"
                        ),
                        @ExampleObject(
                            name = "GET_TRANSACTIONS_BY_PORTFOLIO",
                            summary = "查投資組合交易",
                            value = "{\n  \"params\": { \"portfolioId\": 1, \"startDate\": \"2025-10-01 00:00:00\", \"endDate\": \"2025-10-31 23:59:59\" },\n  \"pageNum\": 1,\n  \"pageSize\": 50\n}"
                        )
                    }
                )
            ) DynamicQueryRequest request
    ) {
        try {
            Map<String, Object> result = dynamicQueryService.executeQuery(
                queryCode,
                request != null ? request.getParams() : null,
                request != null ? request.getPageNum() : null,
                request != null ? request.getPageSize() : null
            );
            return ApiResponse.success("查詢成功", result);
        } catch (Exception e) {
            log.error("動態查詢執行失敗(POST): queryCode={}, 錯誤: {}", queryCode, e.getMessage(), e);
            return ApiResponse.error(500, "查詢失敗: " + e.getMessage());
        }
    }
    
    /**
     * 查詢可用的查詢配置列表
     */
    @GetMapping("/available")
    @Operation(summary = "查詢可用的查詢配置", description = "列出所有已啟用的查詢配置")
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
     * 查詢配置詳細資訊 (包含參數定義)
     */
    @GetMapping("/config/{queryCode}")
    @Operation(summary = "查詢配置詳情", description = "取得查詢配置的詳細資訊及參數定義")
    public ApiResponse<Map<String, Object>> getQueryConfig(
            @Parameter(description = "查詢代碼", required = true)
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
}