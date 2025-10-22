package com.bank.apiplatform.service;

import com.bank.apiplatform.dto.request.QueryParamRequest;
import com.bank.apiplatform.entity.ApiQueryParam;
import com.bank.apiplatform.entity.ApiQueryConfig;
import com.bank.apiplatform.repository.ApiQueryParamRepository;
import com.bank.apiplatform.repository.ApiQueryConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 查詢參數服務（修正版 - 配合實際 Entity 結構）
 * 
 * 修正內容：
 * - paramId 類型從 Long 改為 Integer
 * - ApiQueryParam 使用 @ManyToOne 關聯，需要設定 queryConfig 而不是 queryId
 * 
 * 路徑: src/main/java/com/bank/apiplatform/service/QueryParamService.java
 * 
 * 請用此檔案內容完全取代原有的 QueryParamService.java
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QueryParamService {
    
    private final ApiQueryParamRepository queryParamRepository;
    private final ApiQueryConfigRepository queryConfigRepository;
    
    // 支援的參數類型
    private static final List<String> VALID_PARAM_TYPES = Arrays.asList(
        "STRING", "INTEGER", "LONG", "DECIMAL", "DATETIME", "DATE", "BOOLEAN"
    );
    
    /**
     * 查詢指定查詢的所有參數
     */
    public List<ApiQueryParam> findByQueryId(Integer queryId) {
        log.debug("查詢參數: queryId={}", queryId);
        return queryParamRepository.findByQueryId(queryId);
    }
    
    /**
     * 建立查詢參數
     */
    @Transactional
    public ApiQueryParam create(QueryParamRequest request) {
        log.info("建立查詢參數: queryId={}, paramName={}", request.getQueryId(), request.getParamName());
        
        Integer queryId = request.getQueryId().intValue();
        
        // 檢查參數名稱是否已存在（同一查詢內）
        boolean exists = queryParamRepository.existsByQueryIdAndParamName(queryId, request.getParamName());
        
        if (exists) {
            log.error("參數名稱已存在: queryId={}, paramName={}", queryId, request.getParamName());
            throw new RuntimeException("參數名稱已存在: " + request.getParamName());
        }
        
        // 驗證參數類型
        validateParamType(request.getParamType());
        
        // 驗證預設值格式（如果有提供）
        if (request.getDefaultValue() != null && !request.getDefaultValue().isEmpty()) {
            validateDefaultValue(request.getParamType(), request.getDefaultValue());
        }
        
        // 查詢 ApiQueryConfig
        ApiQueryConfig queryConfig = queryConfigRepository.findById(queryId)
            .orElseThrow(() -> new RuntimeException("查詢配置不存在: " + queryId));
        
        // 建立新的參數
        ApiQueryParam param = new ApiQueryParam();
        copyProperties(request, param, queryConfig);
        
        ApiQueryParam saved = queryParamRepository.save(param);
        log.info("查詢參數建立成功: paramId={}, paramName={}", saved.getParamId(), saved.getParamName());
        
        return saved;
    }
    
    /**
     * 更新查詢參數
     */
    @Transactional
    public ApiQueryParam update(Long paramId, QueryParamRequest request) {
        log.info("更新查詢參數: paramId={}, paramName={}", paramId, request.getParamName());
        
        // ← 修正：paramId 從 Long 轉為 Integer
        ApiQueryParam param = queryParamRepository.findById(paramId)
            .orElseThrow(() -> new RuntimeException("參數不存在: " + paramId));
        
        // 取得 queryId（從關聯的 queryConfig）
        Integer queryId = param.getQueryConfig().getQueryId();
        
        // 如果修改了參數名稱，檢查新名稱是否已存在
        if (!param.getParamName().equals(request.getParamName())) {
            boolean exists = queryParamRepository.existsByQueryIdAndParamName(
                queryId, 
                request.getParamName()
            );
            
            if (exists) {
                log.error("參數名稱已存在: queryId={}, paramName={}", queryId, request.getParamName());
                throw new RuntimeException("參數名稱已存在: " + request.getParamName());
            }
        }
        
        // 驗證參數類型
        validateParamType(request.getParamType());
        
        // 驗證預設值格式（如果有提供）
        if (request.getDefaultValue() != null && !request.getDefaultValue().isEmpty()) {
            validateDefaultValue(request.getParamType(), request.getDefaultValue());
        }
        
        // 更新屬性（不需要重新設定 queryConfig，保持原有關聯）
        copyProperties(request, param, param.getQueryConfig());
        ApiQueryParam updated = queryParamRepository.save(param);
        
        log.info("查詢參數更新成功: paramId={}, paramName={}", updated.getParamId(), updated.getParamName());
        
        return updated;
    }
    
    /**
     * 刪除查詢參數
     */
    @Transactional
    public void delete(Long paramId) {
        log.info("刪除查詢參數: paramId={}", paramId);
        
        ApiQueryParam param = queryParamRepository.findById(paramId)
            .orElseThrow(() -> new RuntimeException("參數不存在: " + paramId));
        
        Integer queryId = param.getQueryConfig().getQueryId();
        String paramName = param.getParamName();
        
        queryParamRepository.delete(param);
        log.info("查詢參數刪除成功: paramId={}, queryId={}, paramName={}", paramId, queryId, paramName);
    }
    
    /**
     * 批次建立參數
     */
    @Transactional
    public List<ApiQueryParam> batchCreate(Integer queryId, List<QueryParamRequest> requests) {
        log.info("批次建立參數: queryId={}, count={}", queryId, requests.size());
        
        List<ApiQueryParam> created = new ArrayList<>();
        
        for (QueryParamRequest request : requests) {
            request.setQueryId(queryId.longValue());
            created.add(create(request));
        }
        
        log.info("批次建立參數完成: queryId={}, successCount={}", queryId, created.size());
        return created;
    }
    
    /**
     * 刪除指定查詢的所有參數
     */
    @Transactional
    public void deleteByQueryId(Integer queryId) {
        log.info("刪除查詢的所有參數: queryId={}", queryId);
        
        List<ApiQueryParam> params = queryParamRepository.findByQueryId(queryId);
        int count = params.size();
        
        if (count > 0) {
            queryParamRepository.deleteByQueryId(queryId);
            log.info("刪除參數完成: queryId={}, count={}", queryId, count);
        } else {
            log.info("查詢無參數: queryId={}", queryId);
        }
    }
    
    /**
     * 複製屬性從 Request 到 Entity
     * 
     * ← 修正：設定 queryConfig 關聯而不是 queryId
     */
    private void copyProperties(QueryParamRequest request, ApiQueryParam param, ApiQueryConfig queryConfig) {
        // 設定關聯
        param.setQueryConfig(queryConfig);
        
        // 設定其他屬性
        param.setParamName(request.getParamName());
        param.setParamType(request.getParamType());
        param.setIsRequired(request.getIsRequired() != null ? request.getIsRequired() : false);
        param.setDefaultValue(request.getDefaultValue());
        param.setValidationRegex(request.getValidationRegex());
        param.setDescription(request.getDescription());
    }
    
    /**
     * 驗證參數類型是否有效
     */
    private void validateParamType(String paramType) {
        if (paramType == null || paramType.isEmpty()) {
            throw new RuntimeException("參數類型不能為空");
        }
        
        if (!VALID_PARAM_TYPES.contains(paramType.toUpperCase())) {
            throw new RuntimeException(
                "無效的參數類型: " + paramType + 
                "，有效值為: " + String.join(", ", VALID_PARAM_TYPES)
            );
        }
    }
    
    /**
     * 驗證預設值格式是否符合參數類型
     */
    private void validateDefaultValue(String paramType, String defaultValue) {
        try {
            switch (paramType.toUpperCase()) {
                case "INTEGER":
                    Integer.parseInt(defaultValue);
                    break;
                case "LONG":
                    Long.parseLong(defaultValue);
                    break;
                case "DECIMAL":
                    Double.parseDouble(defaultValue);
                    break;
                case "BOOLEAN":
                    if (!defaultValue.equalsIgnoreCase("true") && !defaultValue.equalsIgnoreCase("false")) {
                        throw new IllegalArgumentException("布林值必須是 true 或 false");
                    }
                    break;
                case "DATE":
                    if (!defaultValue.matches("\\d{4}-\\d{2}-\\d{2}")) {
                        throw new IllegalArgumentException("日期格式必須是 YYYY-MM-DD");
                    }
                    break;
                case "DATETIME":
                    if (!defaultValue.matches("\\d{4}-\\d{2}-\\d{2}[T ]\\d{2}:\\d{2}:\\d{2}.*")) {
                        throw new IllegalArgumentException("日期時間格式必須是 YYYY-MM-DD HH:mm:ss");
                    }
                    break;
                case "STRING":
                    // 字串類型不需要特別驗證
                    break;
            }
        } catch (Exception e) {
            throw new RuntimeException(
                "預設值格式不正確: " + defaultValue + 
                " 不符合參數類型 " + paramType + " 的格式要求。" +
                " 錯誤: " + e.getMessage()
            );
        }
    }
    
    /**
     * 統計指定查詢的參數數量
     */
    public long countByQueryId(Integer queryId) {
        return queryParamRepository.countByQueryId(queryId);
    }
    
    /**
     * 統計指定查詢的必填參數數量
     */
    public long countRequiredByQueryId(Integer queryId) {
        return queryParamRepository.countByQueryIdAndIsRequired(queryId, true);
    }
    
    /**
     * 檢查參數名稱是否存在
     */
    public boolean existsByQueryIdAndParamName(Integer queryId, String paramName) {
        return queryParamRepository.existsByQueryIdAndParamName(queryId, paramName);
    }
    
    /**
     * 查詢指定查詢的必填參數
     */
    public List<ApiQueryParam> findRequiredParams(Integer queryId) {
        List<ApiQueryParam> allParams = queryParamRepository.findByQueryId(queryId);
        List<ApiQueryParam> requiredParams = new ArrayList<>();
        
        for (ApiQueryParam param : allParams) {
            if (param.getIsRequired()) {
                requiredParams.add(param);
            }
        }
        
        return requiredParams;
    }
    
    /**
     * 查詢指定查詢的選填參數
     */
    public List<ApiQueryParam> findOptionalParams(Integer queryId) {
        List<ApiQueryParam> allParams = queryParamRepository.findByQueryId(queryId);
        List<ApiQueryParam> optionalParams = new ArrayList<>();
        
        for (ApiQueryParam param : allParams) {
            if (!param.getIsRequired()) {
                optionalParams.add(param);
            }
        }
        
        return optionalParams;
    }
}