package com.bank.apiplatform.service.impl;

import com.bank.apiplatform.entity.ApiKey;
import com.bank.apiplatform.repository.ApiKeyRepository;
import com.bank.apiplatform.service.ApiKeyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

/**
 * API Key 服務實作
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ApiKeyServiceImpl implements ApiKeyService {
    
    private final ApiKeyRepository apiKeyRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    
    @Override
    public ApiKey createApiKey(ApiKey apiKey) {
        log.info("建立 API Key：{}", apiKey.getAppName());
        
        // 檢查 API Key 是否已存在
        if (apiKeyRepository.existsByApiKey(apiKey.getApiKey())) {
            throw new RuntimeException("API Key 已存在");
        }
        
        // 設定預設值
        if (apiKey.getStatus() == null) {
            apiKey.setStatus("ACTIVE");
        }
        if (apiKey.getRateLimit() == null) {
            apiKey.setRateLimit(1000);
        }
        
        ApiKey savedApiKey = apiKeyRepository.save(apiKey);
        log.info("API Key 建立成功，ID：{}", savedApiKey.getId());
        
        return savedApiKey;
    }
    
    @Override
    public ApiKey generateApiKey(String appName, String description, Integer rateLimit) {
        log.info("產生新的 API Key：{}", appName);
        
        // 產生 API Key（格式：api_xxxx）
        String apiKey = "api_" + generateRandomString(59);
        
        // 產生 API Secret（使用 SHA-256）
        String apiSecret = generateSecureSecret();
        
        ApiKey newApiKey = new ApiKey();
        newApiKey.setAppName(appName);
        newApiKey.setApiKey(apiKey);
        newApiKey.setApiSecret(apiSecret);
        newApiKey.setDescription(description);
        newApiKey.setRateLimit(rateLimit != null ? rateLimit : 1000);
        newApiKey.setStatus("ACTIVE");
        newApiKey.setCreatedBy("SYSTEM");
        
        return createApiKey(newApiKey);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ApiKey getByApiKey(String apiKey) {
        return apiKeyRepository.findByApiKey(apiKey)
                .orElseThrow(() -> new RuntimeException("API Key 不存在：" + apiKey));
    }
    
    @Override
    @Transactional(readOnly = true)
    public ApiKey getById(Long id) {
        return apiKeyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("API Key 不存在，ID：" + id));
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ApiKey> getAllApiKeys() {
        return apiKeyRepository.findAll();
    }
    
    @Override
    public ApiKey updateApiKey(Long id, ApiKey apiKey) {
        log.info("更新 API Key，ID：{}", id);
        
        ApiKey existingApiKey = getById(id);
        
        // 更新欄位
        if (apiKey.getAppName() != null) {
            existingApiKey.setAppName(apiKey.getAppName());
        }
        if (apiKey.getDescription() != null) {
            existingApiKey.setDescription(apiKey.getDescription());
        }
        if (apiKey.getRateLimit() != null) {
            existingApiKey.setRateLimit(apiKey.getRateLimit());
        }
        if (apiKey.getIpWhitelist() != null) {
            existingApiKey.setIpWhitelist(apiKey.getIpWhitelist());
        }
        if (apiKey.getExpiredAt() != null) {
            existingApiKey.setExpiredAt(apiKey.getExpiredAt());
        }
        
        ApiKey updated = apiKeyRepository.save(existingApiKey);
        log.info("API Key 更新成功，ID：{}", id);
        
        return updated;
    }
    
    @Override
    public void deactivateApiKey(Long id) {
        log.info("停用 API Key，ID：{}", id);
        
        ApiKey apiKey = getById(id);
        apiKey.setStatus("INACTIVE");
        apiKeyRepository.save(apiKey);
        
        log.info("API Key 已停用，ID：{}", id);
    }
    
    @Override
    public void activateApiKey(Long id) {
        log.info("啟用 API Key，ID：{}", id);
        
        ApiKey apiKey = getById(id);
        apiKey.setStatus("ACTIVE");
        apiKeyRepository.save(apiKey);
        
        log.info("API Key 已啟用，ID：{}", id);
    }
    
    @Override
    public void deleteApiKey(Long id) {
        log.info("刪除 API Key，ID：{}", id);
        
        if (!apiKeyRepository.existsById(id)) {
            throw new RuntimeException("API Key 不存在，ID：" + id);
        }
        
        apiKeyRepository.deleteById(id);
        log.info("API Key 已刪除，ID：{}", id);
    }
    
    /**
     * 產生隨機字串
     */
    private String generateRandomString(int length) {
        byte[] randomBytes = new byte[length];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes)
                .substring(0, length);
    }
    
    /**
     * 產生安全的 Secret（使用 SHA-256）
     */
    private String generateSecureSecret() {
        String randomString = generateRandomString(64);
        return DigestUtils.sha256Hex(randomString + System.currentTimeMillis());
    }
}