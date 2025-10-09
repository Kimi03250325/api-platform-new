package com.bank.apiplatform.filter;

import com.bank.apiplatform.entity.ApiKey;
import com.bank.apiplatform.repository.ApiKeyRepository;
import com.bank.apiplatform.util.RateLimiter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * API Key 認證過濾器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ApiKeyAuthFilter extends OncePerRequestFilter {
    
    private final ApiKeyRepository apiKeyRepository;
    private final RateLimiter rateLimiter;
    private final ObjectMapper objectMapper;
    
    // 白名單路徑（不需要認證）
    private static final String[] WHITELIST_PATHS = {
        "/actuator/health",
        "/swagger-ui",
        "/v3/api-docs",
        "/api/health",
        "/api/db-test",
        "/api/admin/api-keys"  // API Key 管理端點
    };
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) 
            throws ServletException, IOException {
        
        String path = request.getRequestURI();
        
        // 檢查是否在白名單中
        if (isWhitelisted(path)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // 取得 API Key
        String apiKey = request.getHeader("X-API-Key");
        
        if (apiKey == null || apiKey.trim().isEmpty()) {
            sendErrorResponse(response, 401, "缺少 API Key");
            return;
        }
        
        // 驗證 API Key
        ApiKey apiKeyEntity = apiKeyRepository.findByApiKey(apiKey).orElse(null);
        
        if (apiKeyEntity == null) {
            log.warn("無效的 API Key：{}", apiKey);
            sendErrorResponse(response, 401, "無效的 API Key");
            return;
        }
        
        // 檢查 API Key 狀態
        if (!apiKeyEntity.isActive()) {
            log.warn("API Key 已停用或過期：{}", apiKey);
            sendErrorResponse(response, 401, "API Key 已停用或過期");
            return;
        }
        
        // 驗證 IP 白名單
        if (!validateIpWhitelist(apiKeyEntity, request.getRemoteAddr())) {
            log.warn("IP 不在白名單中：{} - {}", apiKey, request.getRemoteAddr());
            sendErrorResponse(response, 403, "IP 位址未授權");
            return;
        }
        
        // 檢查流量限制
        if (!rateLimiter.allowRequest(apiKey, apiKeyEntity.getRateLimit())) {
            log.warn("超過流量限制：{}", apiKey);
            sendErrorResponse(response, 429, "超過流量限制，請稍後再試");
            return;
        }
        
        // 更新最後使用時間（非同步）
        updateLastUsedTime(apiKey);
        
        // 將 API Key 資訊加入 request attribute
        request.setAttribute("apiKey", apiKeyEntity);
        request.setAttribute("appName", apiKeyEntity.getAppName());
        
        log.info("API Key 認證成功：{} - {}", apiKeyEntity.getAppName(), path);
        
        // 繼續過濾鏈
        filterChain.doFilter(request, response);
    }
    
    /**
     * 檢查路徑是否在白名單中
     */
    private boolean isWhitelisted(String path) {
        for (String whitelistPath : WHITELIST_PATHS) {
            if (path.startsWith(whitelistPath)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 驗證 IP 白名單
     */
    private boolean validateIpWhitelist(ApiKey apiKey, String clientIp) {
        String ipWhitelist = apiKey.getIpWhitelist();
        
        // 如果沒有設定白名單，則允許所有 IP
        if (ipWhitelist == null || ipWhitelist.trim().isEmpty()) {
            return true;
        }
        
        // 解析 IP 白名單（JSON 格式）
        try {
            String[] allowedIps = objectMapper.readValue(ipWhitelist, String[].class);
            for (String allowedIp : allowedIps) {
                if (allowedIp.equals(clientIp) || allowedIp.equals("*")) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("解析 IP 白名單失敗：{}", ipWhitelist, e);
            return false;
        }
        
        return false;
    }
    
    /**
     * 非同步更新最後使用時間
     */
    private void updateLastUsedTime(String apiKey) {
        try {
            apiKeyRepository.updateLastUsedAt(apiKey, LocalDateTime.now());
        } catch (Exception e) {
            log.error("更新 API Key 最後使用時間失敗：{}", apiKey, e);
        }
    }
    
    /**
     * 發送錯誤回應
     */
    private void sendErrorResponse(HttpServletResponse response, int statusCode, String message) 
            throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json;charset=UTF-8");
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("code", statusCode);
        errorResponse.put("message", message);
        errorResponse.put("timestamp", LocalDateTime.now());
        
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}