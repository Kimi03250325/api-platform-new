package com.bank.apiplatform.repository;

import com.bank.apiplatform.entity.ApiQueryConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * API 查詢配置 Repository
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
     * 查詢所有已啟用的查詢配置
     */
    List<ApiQueryConfig> findByIsEnabled(Boolean isEnabled);
    
    /**
     * 根據分類查詢
     */
    List<ApiQueryConfig> findByCategory(String category);
    
    /**
     * 根據分類和啟用狀態查詢
     */
    List<ApiQueryConfig> findByCategoryAndIsEnabled(String category, Boolean isEnabled);
    
    /**
     * 根據資料源代碼查詢
     */
    List<ApiQueryConfig> findByDatasourceCode(String datasourceCode);
    
    /**
     * 根據資料源代碼和啟用狀態查詢
     */
    List<ApiQueryConfig> findByDatasourceCodeAndIsEnabled(String datasourceCode, Boolean isEnabled);
    
    /**
     * 檢查查詢代碼是否存在
     */
    boolean existsByQueryCode(String queryCode);
}