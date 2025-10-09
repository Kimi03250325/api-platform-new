package com.bank.apiplatform.service;

import com.bank.apiplatform.entity.ApiKey;

import java.util.List;

/**
 * API Key 服務介面
 */
public interface ApiKeyService {
    
    /**
     * 建立新的 API Key
     */
    ApiKey createApiKey(ApiKey apiKey);
    
    /**
     * 產生隨機 API Key 和 Secret
     */
    ApiKey generateApiKey(String appName, String description, Integer rateLimit);
    
    /**
     * 根據 API Key 字串查詢
     */
    ApiKey getByApiKey(String apiKey);
    
    /**
     * 根據 ID 查詢
     */
    ApiKey getById(Long id);
    
    /**
     * 查詢所有 API Keys
     */
    List<ApiKey> getAllApiKeys();
    
    /**
     * 更新 API Key
     */
    ApiKey updateApiKey(Long id, ApiKey apiKey);
    
    /**
     * 停用 API Key
     */
    void deactivateApiKey(Long id);
    
    /**
     * 啟用 API Key
     */
    void activateApiKey(Long id);
    
    /**
     * 刪除 API Key
     */
    void deleteApiKey(Long id);
}