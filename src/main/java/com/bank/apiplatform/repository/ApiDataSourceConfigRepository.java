package com.bank.apiplatform.repository;

import com.bank.apiplatform.entity.ApiDataSourceConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 資料源配置 Repository
 */
@Repository
public interface ApiDataSourceConfigRepository extends JpaRepository<ApiDataSourceConfig, Integer> {
    
    /**
     * 根據資料源代碼查詢
     */
    Optional<ApiDataSourceConfig> findByDatasourceCode(String datasourceCode);
    
    /**
     * 根據資料源代碼和啟用狀態查詢
     */
    Optional<ApiDataSourceConfig> findByDatasourceCodeAndIsEnabled(String datasourceCode, Boolean isEnabled);
    
    /**
     * 查詢所有已啟用的資料源
     */
    List<ApiDataSourceConfig> findByIsEnabled(Boolean isEnabled);
    
    /**
     * 根據資料庫類型查詢
     */
    List<ApiDataSourceConfig> findByDbType(String dbType);
    
    /**
     * 根據資料庫類型和啟用狀態查詢
     */
    List<ApiDataSourceConfig> findByDbTypeAndIsEnabled(String dbType, Boolean isEnabled);
    
    /**
     * 檢查資料源代碼是否存在
     */
    boolean existsByDatasourceCode(String datasourceCode);
}