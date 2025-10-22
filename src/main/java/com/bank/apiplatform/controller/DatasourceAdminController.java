package com.bank.apiplatform.controller;

import com.bank.apiplatform.dto.request.DatasourceConfigRequest;
import com.bank.apiplatform.dto.response.ApiResponse;
import com.bank.apiplatform.entity.ApiDataSourceConfig;
import com.bank.apiplatform.service.DatasourceConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 資料源管理 API 控制器
 * 
 * 路徑: src/main/java/com/bank/apiplatform/controller/DatasourceAdminController.java
 * 
 * 建立方法:
 * 1. 在 Eclipse 右鍵點擊 com.bank.apiplatform.controller 套件
 * 2. New > Class
 * 3. Name: DatasourceAdminController
 * 4. Finish
 * 5. 複製此檔案內容，取代原內容
 * 6. Ctrl + S 儲存
 */
@RestController
@RequestMapping("/api/datasource")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "資料源管理", description = "資料源配置的 CRUD 操作")
public class DatasourceAdminController {
    
    private final DatasourceConfigService datasourceConfigService;
    
    /**
     * 查詢所有資料源
     */
    @GetMapping("/list")
    @Operation(
        summary = "📋 查詢所有資料源",
        description = "取得系統中所有的資料源配置列表，包含已啟用和已停用的資料源"
    )
    public ApiResponse<List<ApiDataSourceConfig>> listAll() {
        try {
            log.info("查詢所有資料源配置");
            List<ApiDataSourceConfig> list = datasourceConfigService.findAll();
            log.info("查詢成功，共 {} 筆資料源", list.size());
            return ApiResponse.success("查詢成功", list);
        } catch (Exception e) {
            log.error("查詢資料源失敗", e);
            return ApiResponse.error(500, "查詢失敗: " + e.getMessage());
        }
    }
    
    /**
     * 查詢已啟用的資料源
     */
    @GetMapping("/list/enabled")
    @Operation(
        summary = "✅ 查詢已啟用的資料源",
        description = "取得所有已啟用的資料源配置列表"
    )
    public ApiResponse<List<ApiDataSourceConfig>> listEnabled() {
        try {
            log.info("查詢已啟用的資料源配置");
            List<ApiDataSourceConfig> list = datasourceConfigService.findAllEnabled();
            log.info("查詢成功，共 {} 筆已啟用的資料源", list.size());
            return ApiResponse.success("查詢成功", list);
        } catch (Exception e) {
            log.error("查詢已啟用資料源失敗", e);
            return ApiResponse.error(500, "查詢失敗: " + e.getMessage());
        }
    }
    
    /**
     * 根據 ID 查詢資料源
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "🔍 查詢單個資料源",
        description = "根據資料源 ID 查詢資料源配置詳情"
    )
    public ApiResponse<ApiDataSourceConfig> getById(
            @Parameter(description = "資料源 ID", required = true, example = "1")
            @PathVariable Integer id) {
        try {
            log.info("查詢資料源: id={}", id);
            ApiDataSourceConfig config = datasourceConfigService.findById(id);
            log.info("查詢成功: datasourceCode={}", config.getDatasourceCode());
            return ApiResponse.success("查詢成功", config);
        } catch (Exception e) {
            log.error("查詢資料源失敗: id={}", id, e);
            return ApiResponse.error(500, "查詢失敗: " + e.getMessage());
        }
    }
    
    /**
     * 根據代碼查詢資料源
     */
    @GetMapping("/code/{datasourceCode}")
    @Operation(
        summary = "🔍 根據代碼查詢資料源",
        description = "根據資料源代碼查詢資料源配置詳情"
    )
    public ApiResponse<ApiDataSourceConfig> getByCode(
            @Parameter(description = "資料源代碼", required = true, example = "PRIMARY_DB")
            @PathVariable String datasourceCode) {
        try {
            log.info("查詢資料源: datasourceCode={}", datasourceCode);
            ApiDataSourceConfig config = datasourceConfigService.findByCode(datasourceCode);
            log.info("查詢成功: id={}", config.getDatasourceId());
            return ApiResponse.success("查詢成功", config);
        } catch (Exception e) {
            log.error("查詢資料源失敗: datasourceCode={}", datasourceCode, e);
            return ApiResponse.error(500, "查詢失敗: " + e.getMessage());
        }
    }
    
    /**
     * 建立資料源
     */
    @PostMapping("/create")
    @Operation(
        summary = "➕ 建立資料源",
        description = "建立新的資料源配置。資料源代碼必須唯一，建議使用大寫英文字母和底線命名，例如：PRIMARY_DB"
    )
    public ApiResponse<ApiDataSourceConfig> create(
            @Valid @RequestBody DatasourceConfigRequest request) {
        try {
            log.info("建立資料源: datasourceCode={}", request.getDatasourceCode());
            ApiDataSourceConfig created = datasourceConfigService.create(request);
            log.info("建立成功: id={}, datasourceCode={}", 
                    created.getDatasourceId(), created.getDatasourceCode());
            return ApiResponse.success("建立成功", created);
        } catch (Exception e) {
            log.error("建立資料源失敗: datasourceCode={}", request.getDatasourceCode(), e);
            return ApiResponse.error(500, "建立失敗: " + e.getMessage());
        }
    }
    
    /**
     * 更新資料源
     */
    @PutMapping("/update/{id}")
    @Operation(
        summary = "✏️ 更新資料源",
        description = "更新現有的資料源配置。如果不提供密碼，則保持原密碼不變"
    )
    public ApiResponse<ApiDataSourceConfig> update(
            @Parameter(description = "資料源 ID", required = true, example = "1")
            @PathVariable Integer id,
            @Valid @RequestBody DatasourceConfigRequest request) {
        try {
            log.info("更新資料源: id={}, datasourceCode={}", id, request.getDatasourceCode());
            ApiDataSourceConfig updated = datasourceConfigService.update(id, request);
            log.info("更新成功: id={}, datasourceCode={}", 
                    updated.getDatasourceId(), updated.getDatasourceCode());
            return ApiResponse.success("更新成功", updated);
        } catch (Exception e) {
            log.error("更新資料源失敗: id={}", id, e);
            return ApiResponse.error(500, "更新失敗: " + e.getMessage());
        }
    }
    
    /**
     * 刪除資料源
     */
    @DeleteMapping("/delete/{id}")
    @Operation(
        summary = "🗑️ 刪除資料源",
        description = "刪除指定的資料源配置。注意：如果有查詢配置使用此資料源，建議先停用而非刪除"
    )
    public ApiResponse<Void> delete(
            @Parameter(description = "資料源 ID", required = true, example = "1")
            @PathVariable Integer id) {
        try {
            log.info("刪除資料源: id={}", id);
            datasourceConfigService.delete(id);
            log.info("刪除成功: id={}", id);
            return ApiResponse.success("刪除成功", null);
        } catch (Exception e) {
            log.error("刪除資料源失敗: id={}", id, e);
            return ApiResponse.error(500, "刪除失敗: " + e.getMessage());
        }
    }
    
    /**
     * 測試資料庫連線（新建立時）
     */
    @PostMapping("/test-connection")
    @Operation(
        summary = "🔌 測試資料庫連線",
        description = "測試資料庫連線是否正常。在建立或修改資料源前，建議先測試連線"
    )
    public ApiResponse<Boolean> testConnection(
            @Valid @RequestBody DatasourceConfigRequest request) {
        try {
            log.info("測試資料庫連線: jdbcUrl={}", request.getJdbcUrl());
            boolean result = datasourceConfigService.testConnection(request);
            log.info("測試完成，結果: {}", result ? "成功" : "失敗");
            
            if (result) {
                return ApiResponse.success("✅ 連線測試成功！資料庫連線正常", result);
            } else {
                return ApiResponse.error(500, "❌ 連線測試失敗！無法連接資料庫");
            }
        } catch (Exception e) {
            log.error("測試連線失敗: jdbcUrl={}", request.getJdbcUrl(), e);
            return ApiResponse.error(500, "❌ 測試失敗: " + e.getMessage());
        }
    }
    
    /**
     * 測試現有資料源連線
     */
    @GetMapping("/test/{datasourceCode}")
    @Operation(
        summary = "🔌 測試現有資料源連線",
        description = "測試已存在的資料源連線是否正常"
    )
    public ApiResponse<Boolean> testExistingConnection(
            @Parameter(description = "資料源代碼", required = true, example = "PRIMARY_DB")
            @PathVariable String datasourceCode) {
        try {
            log.info("測試資料源連線: datasourceCode={}", datasourceCode);
            boolean result = datasourceConfigService.testExistingConnection(datasourceCode);
            log.info("測試完成，結果: {}", result ? "成功" : "失敗");
            
            if (result) {
                return ApiResponse.success("✅ 連線測試成功！資料源 [" + datasourceCode + "] 連線正常", result);
            } else {
                return ApiResponse.error(500, "❌ 連線測試失敗！資料源 [" + datasourceCode + "] 無法連接");
            }
        } catch (Exception e) {
            log.error("測試連線失敗: datasourceCode={}", datasourceCode, e);
            return ApiResponse.error(500, "❌ 測試失敗: " + e.getMessage());
        }
    }
    
    /**
     * 根據資料庫類型查詢資料源
     */
    @GetMapping("/type/{dbType}")
    @Operation(
        summary = "🔍 根據資料庫類型查詢",
        description = "查詢指定資料庫類型的所有資料源"
    )
    public ApiResponse<List<ApiDataSourceConfig>> getByDbType(
            @Parameter(description = "資料庫類型", required = true, example = "SQLSERVER")
            @PathVariable String dbType) {
        try {
            log.info("查詢資料源: dbType={}", dbType);
            List<ApiDataSourceConfig> list = datasourceConfigService.findByDbType(dbType);
            log.info("查詢成功，共 {} 筆資料源", list.size());
            return ApiResponse.success("查詢成功", list);
        } catch (Exception e) {
            log.error("查詢資料源失敗: dbType={}", dbType, e);
            return ApiResponse.error(500, "查詢失敗: " + e.getMessage());
        }
    }
}