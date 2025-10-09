package com.bank.apiplatform.controller;

import com.bank.apiplatform.dto.response.ApiResponse;
import com.bank.apiplatform.entity.ApiKey;
import com.bank.apiplatform.service.ApiKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API Key 管理控制器
 */
@RestController
@RequestMapping("/api/admin/api-keys")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "API Key 管理", description = "API Key 管理相關 API（管理員專用）")
public class ApiKeyController {
    
    private final ApiKeyService apiKeyService;
    
    /**
     * 產生新的 API Key
     */
    @PostMapping("/generate")
    @Operation(summary = "產生 API Key", description = "自動產生新的 API Key 和 Secret")
    public ApiResponse<ApiKey> generateApiKey(
            @RequestParam String appName,
            @RequestParam(required = false) String description,
            @RequestParam(required = false, defaultValue = "1000") Integer rateLimit) {
        
        log.info("收到產生 API Key 請求：{}", appName);
        ApiKey apiKey = apiKeyService.generateApiKey(appName, description, rateLimit);
        return ApiResponse.success("API Key 產生成功", apiKey);
    }
    
    /**
     * 查詢所有 API Keys
     */
    @GetMapping
    @Operation(summary = "查詢所有 API Keys", description = "取得所有 API Key 清單")
    public ApiResponse<List<ApiKey>> getAllApiKeys() {
        log.info("收到查詢所有 API Keys 請求");
        List<ApiKey> apiKeys = apiKeyService.getAllApiKeys();
        return ApiResponse.success("查詢成功", apiKeys);
    }
    
    /**
     * 根據 ID 查詢 API Key
     */
    @GetMapping("/{id}")
    @Operation(summary = "查詢 API Key", description = "根據 ID 查詢 API Key 詳細資訊")
    public ApiResponse<ApiKey> getApiKeyById(@PathVariable Long id) {
        log.info("收到查詢 API Key 請求，ID：{}", id);
        ApiKey apiKey = apiKeyService.getById(id);
        return ApiResponse.success("查詢成功", apiKey);
    }
    
    /**
     * 更新 API Key
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新 API Key", description = "更新 API Key 資訊（不包含 apiKey 和 apiSecret）")
    public ApiResponse<ApiKey> updateApiKey(
            @PathVariable Long id,
            @RequestBody ApiKey apiKey) {
        
        log.info("收到更新 API Key 請求，ID：{}", id);
        ApiKey updatedApiKey = apiKeyService.updateApiKey(id, apiKey);
        return ApiResponse.success("更新成功", updatedApiKey);
    }
    
    /**
     * 停用 API Key
     */
    @PostMapping("/{id}/deactivate")
    @Operation(summary = "停用 API Key", description = "停用指定的 API Key，停用後無法使用")
    public ApiResponse<Void> deactivateApiKey(@PathVariable Long id) {
        log.info("收到停用 API Key 請求，ID：{}", id);
        apiKeyService.deactivateApiKey(id);
        return ApiResponse.success("API Key 已停用", null);
    }
    
    /**
     * 啟用 API Key
     */
    @PostMapping("/{id}/activate")
    @Operation(summary = "啟用 API Key", description = "啟用指定的 API Key")
    public ApiResponse<Void> activateApiKey(@PathVariable Long id) {
        log.info("收到啟用 API Key 請求，ID：{}", id);
        apiKeyService.activateApiKey(id);
        return ApiResponse.success("API Key 已啟用", null);
    }
    
    /**
     * 刪除 API Key
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "刪除 API Key", description = "永久刪除指定的 API Key")
    public ApiResponse<Void> deleteApiKey(@PathVariable Long id) {
        log.info("收到刪除 API Key 請求，ID：{}", id);
        apiKeyService.deleteApiKey(id);
        return ApiResponse.success("刪除成功", null);
    }
}