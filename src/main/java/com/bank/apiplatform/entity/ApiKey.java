package com.bank.apiplatform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * API Key 實體類別
 * 
 * 對應資料表：api_keys
 * 資料庫：SQL Server 2022
 */
@Entity
@Table(name = "api_keys")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiKey {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "app_name", nullable = false, length = 100)
    private String appName;
    
    @Column(name = "api_key", nullable = false, unique = true, length = 64)
    private String apiKey;
    
    @Column(name = "api_secret", nullable = false, length = 128)
    private String apiSecret;
    
    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE";
    
    @Column(name = "ip_whitelist", columnDefinition = "NVARCHAR(MAX)")
    private String ipWhitelist;
    
    @Column(name = "rate_limit")
    private Integer rateLimit = 1000;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "created_by", length = 50)
    private String createdBy;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "expired_at")
    private LocalDateTime expiredAt;
    
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
    
    /**
     * 插入前自動設定時間
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * 更新前自動更新時間
     * 注意：SQL Server 已有觸發器處理 updated_at，這裡是雙重保險
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * 檢查 API Key 是否有效
     * 
     * @return true 有效, false 無效
     */
    public boolean isActive() {
        return "ACTIVE".equals(status) && 
               (expiredAt == null || expiredAt.isAfter(LocalDateTime.now()));
    }
    
    /**
     * 檢查 API Key 是否過期
     * 
     * @return true 已過期, false 未過期
     */
    public boolean isExpired() {
        return expiredAt != null && expiredAt.isBefore(LocalDateTime.now());
    }
}