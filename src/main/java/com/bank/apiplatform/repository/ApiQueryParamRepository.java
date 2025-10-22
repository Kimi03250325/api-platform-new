package com.bank.apiplatform.repository;

import com.bank.apiplatform.entity.ApiQueryParam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * API 查詢參數 Repository (修正版)
 * 
 * 修正內容：
 * - ApiQueryParam 使用 @ManyToOne 關聯到 queryConfig
 * - 需要使用 queryConfig.queryId 來查詢，而不是直接的 queryId
 * 
 * 路徑: src/main/java/com/bank/apiplatform/repository/ApiQueryParamRepository.java
 * 
 * 請用此檔案內容完全取代原有的 ApiQueryParamRepository.java
 */
@Repository
public interface ApiQueryParamRepository extends JpaRepository<ApiQueryParam, Long> {
    
    /**
     * 根據查詢 ID 查詢所有參數
     * 使用 queryConfig.queryId 路徑
     */
    @Query("SELECT p FROM ApiQueryParam p WHERE p.queryConfig.queryId = :queryId")
    List<ApiQueryParam> findByQueryId(@Param("queryId") Integer queryId);
    
    /**
     * 根據查詢 ID 和參數名稱查詢
     */
    @Query("SELECT p FROM ApiQueryParam p WHERE p.queryConfig.queryId = :queryId AND p.paramName = :paramName")
    List<ApiQueryParam> findByQueryIdAndParamName(@Param("queryId") Integer queryId, 
                                                   @Param("paramName") String paramName);
    
    /**
     * 刪除指定查詢的所有參數
     */
    @Query("DELETE FROM ApiQueryParam p WHERE p.queryConfig.queryId = :queryId")
    void deleteByQueryId(@Param("queryId") Integer queryId);
    
    /**
     * 檢查參數是否存在
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM ApiQueryParam p " +
           "WHERE p.queryConfig.queryId = :queryId AND p.paramName = :paramName")
    boolean existsByQueryIdAndParamName(@Param("queryId") Integer queryId, 
                                       @Param("paramName") String paramName);
    
    /**
     * 統計指定查詢的參數數量
     */
    @Query("SELECT COUNT(p) FROM ApiQueryParam p WHERE p.queryConfig.queryId = :queryId")
    long countByQueryId(@Param("queryId") Integer queryId);
    
    /**
     * 統計指定查詢的必填參數數量
     */
    @Query("SELECT COUNT(p) FROM ApiQueryParam p WHERE p.queryConfig.queryId = :queryId AND p.isRequired = :isRequired")
    long countByQueryIdAndIsRequired(@Param("queryId") Integer queryId, 
                                     @Param("isRequired") Boolean isRequired);
}