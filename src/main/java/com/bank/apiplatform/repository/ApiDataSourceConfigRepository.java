package com.bank.apiplatform.repository;

import com.bank.apiplatform.entity.ApiDataSourceConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}