package com.bank.apiplatform.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 流量限制工具類別
 * 使用滑動視窗算法實作 Rate Limiting
 */
@Component
@Slf4j
public class RateLimiter {
    
    // 儲存每個 API Key 的請求計數
    private final Map<String, RequestCounter> requestCounters = new ConcurrentHashMap<>();
    
    /**
     * 檢查是否允許請求
     * 
     * @param apiKey API Key
     * @param maxRequests 每分鐘最大請求數
     * @return true 允許, false 拒絕
     */
    public boolean allowRequest(String apiKey, int maxRequests) {
        RequestCounter counter = requestCounters.computeIfAbsent(
            apiKey, 
            k -> new RequestCounter()
        );
        
        return counter.allowRequest(maxRequests);
    }
    
    /**
     * 取得當前請求數
     */
    public int getCurrentRequestCount(String apiKey) {
        RequestCounter counter = requestCounters.get(apiKey);
        return counter != null ? counter.getCount() : 0;
    }
    
    /**
     * 清除過期的計數器（定時任務調用）
     */
    public void cleanupExpiredCounters() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(2);
        requestCounters.entrySet().removeIf(
            entry -> entry.getValue().isExpired(threshold)
        );
        log.debug("清除過期的流量計數器，剩餘：{}", requestCounters.size());
    }
    
    /**
     * 請求計數器內部類別
     */
    private static class RequestCounter {
        private LocalDateTime windowStart;
        private final AtomicInteger count;
        
        public RequestCounter() {
            this.windowStart = LocalDateTime.now();
            this.count = new AtomicInteger(0);
        }
        
        public synchronized boolean allowRequest(int maxRequests) {
            LocalDateTime now = LocalDateTime.now();
            
            // 如果超過 1 分鐘，重置計數器
            if (windowStart.plusMinutes(1).isBefore(now)) {
                windowStart = now;
                count.set(0);
            }
            
            // 檢查是否超過限制
            if (count.get() >= maxRequests) {
                return false;
            }
            
            count.incrementAndGet();
            return true;
        }
        
        public int getCount() {
            return count.get();
        }
        
        public boolean isExpired(LocalDateTime threshold) {
            return windowStart.isBefore(threshold);
        }
    }
}