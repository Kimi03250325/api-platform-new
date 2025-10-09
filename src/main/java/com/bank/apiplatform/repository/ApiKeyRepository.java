package com.bank.apiplatform.repository;

import com.bank.apiplatform.entity.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * API Key Repository
 */
@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    
    /**
     * 根據 API Key 查詢
     */
    Optional<ApiKey> findByApiKey(String apiKey);
    
    /**
     * 檢查 API Key 是否存在
     */
    boolean existsByApiKey(String apiKey);
    
    /**
     * 更新最後使用時間
     */
    @Modifying
    @Transactional
    @Query("UPDATE ApiKey a SET a.lastUsedAt = :timestamp WHERE a.apiKey = :apiKey")
    void updateLastUsedAt(String apiKey, LocalDateTime timestamp);
}