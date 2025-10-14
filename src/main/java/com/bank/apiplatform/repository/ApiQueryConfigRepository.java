package com.bank.apiplatform.repository;

import com.bank.apiplatform.entity.ApiQueryConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 查詢配置 Repository
 */
@Repository
public interface ApiQueryConfigRepository extends JpaRepository<ApiQueryConfig, Integer> {
    
    /**
     * 根據查詢代碼查詢
     */
    Optional<ApiQueryConfig> findByQueryCode(String queryCode);
    
    /**
     * 根據查詢代碼和啟用狀態查詢
     */
    Optional<ApiQueryConfig> findByQueryCodeAndIsEnabled(String queryCode, Boolean isEnabled);
    
    /**
     * 根據分類查詢所有啟用的查詢
     */
    List<ApiQueryConfig> findByCategoryAndIsEnabled(String category, Boolean isEnabled);
    
    /**
     * 查詢所有啟用的查詢
     */
    List<ApiQueryConfig> findByIsEnabled(Boolean isEnabled);
}
