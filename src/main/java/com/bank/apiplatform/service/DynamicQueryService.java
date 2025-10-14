package com.bank.apiplatform.service;

import com.bank.apiplatform.entity.ApiDataSourceConfig;
import com.bank.apiplatform.entity.ApiQueryConfig;
import com.bank.apiplatform.entity.ApiQueryParam;
import com.bank.apiplatform.repository.ApiDataSourceConfigRepository;
import com.bank.apiplatform.repository.ApiQueryConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 動態查詢服務
 * 支援多資料源、參數化查詢、自動分頁
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DynamicQueryService {
    
    private final ApiQueryConfigRepository queryConfigRepository;
    private final ApiDataSourceConfigRepository dataSourceConfigRepository;
    private final DynamicDataSourceManager dataSourceManager;
    
    /**
     * 執行動態查詢
     * 
     * @param queryCode 查詢代碼
     * @param params 查詢參數
     * @param pageNum 頁碼 (從1開始)
     * @param pageSize 每頁筆數
     * @return 查詢結果
     */
    public Map<String, Object> executeQuery(String queryCode, Map<String, Object> params,
                                           Integer pageNum, Integer pageSize) {
        
        log.info("執行動態查詢: queryCode={}, params={}, page={}/{}", 
                 queryCode, params, pageNum, pageSize);
        
        // 1. 查詢配置
        ApiQueryConfig queryConfig = queryConfigRepository
            .findByQueryCodeAndIsEnabled(queryCode, true)
            .orElseThrow(() -> new RuntimeException("查詢配置不存在或未啟用: " + queryCode));
        
        // 2. 取得資料源配置
        ApiDataSourceConfig dsConfig = dataSourceConfigRepository
            .findByDatasourceCodeAndIsEnabled(queryConfig.getDatasourceCode(), true)
            .orElseThrow(() -> new RuntimeException("資料源不存在或未啟用: " + queryConfig.getDatasourceCode()));
        
        // 3. 驗證參數
        validateParams(queryConfig, params);
        
        // 4. 處理參數
        Map<String, Object> processedParams = processParams(params, queryConfig);
        
        // 5. 處理分頁
        int actualPageSize = Math.min(
            pageSize != null ? pageSize : 20,
            queryConfig.getMaxPageSize() != null ? queryConfig.getMaxPageSize() : 100
        );
        int actualPageNum = pageNum != null && pageNum > 0 ? pageNum : 1;
        int offset = (actualPageNum - 1) * actualPageSize;
        
        // 6. 取得對應的資料源
        DataSource dataSource = dataSourceManager.getDataSource(dsConfig);
        NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        
        // 7. 建立分頁 SQL
        String pagedSql = buildPagedSql(queryConfig.getQuerySql(), dsConfig.getDbType(), 
                                       offset, actualPageSize);
        
        // 8. 執行查詢
        List<Map<String, Object>> resultList = jdbcTemplate.queryForList(pagedSql, processedParams);
        
        // 9. 查詢總筆數
        String countSql = buildCountSql(queryConfig.getQuerySql());
        Integer totalCount = jdbcTemplate.queryForObject(countSql, processedParams, Integer.class);
        
        // 10. 組裝結果
        Map<String, Object> result = new HashMap<>();
        result.put("queryCode", queryCode);
        result.put("queryName", queryConfig.getQueryName());
        result.put("dataSource", dsConfig.getDatasourceName());
        result.put("data", resultList);
        result.put("pagination", Map.of(
            "pageNum", actualPageNum,
            "pageSize", actualPageSize,
            "totalCount", totalCount != null ? totalCount : 0,
            "totalPages", totalCount != null ? (int) Math.ceil((double) totalCount / actualPageSize) : 0
        ));
        
        log.info("查詢完成: dataSource={}, 結果筆數={}, 總筆數={}", 
                 dsConfig.getDatasourceName(), resultList.size(), totalCount);
        
        return result;
    }
    
    /**
     * 驗證參數
     */
    private void validateParams(ApiQueryConfig queryConfig, Map<String, Object> params) {
        if (queryConfig.getParams() == null || queryConfig.getParams().isEmpty()) {
            return;
        }
        
        for (ApiQueryParam paramDef : queryConfig.getParams()) {
            // 檢查必填參數
            if (Boolean.TRUE.equals(paramDef.getIsRequired())) {
                if (params == null || !params.containsKey(paramDef.getParamName()) 
                    || params.get(paramDef.getParamName()) == null 
                    || params.get(paramDef.getParamName()).toString().trim().isEmpty()) {
                    throw new RuntimeException("缺少必填參數: " + paramDef.getParamName());
                }
            }
            
            // 正則表達式驗證
            if (paramDef.getValidationRegex() != null && params != null 
                && params.containsKey(paramDef.getParamName())) {
                Object value = params.get(paramDef.getParamName());
                if (value != null && !value.toString().matches(paramDef.getValidationRegex())) {
                    throw new RuntimeException("參數格式錯誤: " + paramDef.getParamName() 
                        + ", 必須符合格式: " + paramDef.getValidationRegex());
                }
            }
        }
    }
    
    /**
     * 處理參數 (型別轉換、預設值)
     */
    private Map<String, Object> processParams(Map<String, Object> params, ApiQueryConfig queryConfig) {
        Map<String, Object> processed = new HashMap<>();
        
        if (queryConfig.getParams() == null || queryConfig.getParams().isEmpty()) {
            return processed;
        }
        
        for (ApiQueryParam paramDef : queryConfig.getParams()) {
            String paramName = paramDef.getParamName();
            Object value = params != null ? params.get(paramName) : null;
            
            // 使用預設值
            if (value == null && paramDef.getDefaultValue() != null) {
                value = paramDef.getDefaultValue();
            }
            
            // 型別轉換
            if (value != null) {
                value = convertParamType(value, paramDef.getParamType());
            }
            
            processed.put(paramName, value);
        }
        
        return processed;
    }
    
    /**
     * 參數型別轉換
     */
    private Object convertParamType(Object value, String paramType) {
        if (value == null) {
            return null;
        }
        
        try {
            switch (paramType.toUpperCase()) {
                case "INT":
                case "INTEGER":
                    return Integer.valueOf(value.toString());
                    
                case "LONG":
                    return Long.valueOf(value.toString());
                    
                case "DECIMAL":
                case "DOUBLE":
                    return Double.valueOf(value.toString());
                    
                case "DATE":
                    // 支援 yyyy-MM-dd 格式
                    return LocalDate.parse(value.toString(), DateTimeFormatter.ISO_LOCAL_DATE);
                    
                case "DATETIME":
                    // 支援 yyyy-MM-dd HH:mm:ss 格式
                    return LocalDateTime.parse(value.toString().replace(" ", "T"));
                    
                case "STRING":
                default:
                    return value.toString();
            }
        } catch (Exception e) {
            throw new RuntimeException("參數型別轉換失敗: " + value + " -> " + paramType + ", 錯誤: " + e.getMessage());
        }
    }
    
    /**
     * 建立分頁 SQL (依資料庫類型)
     */
    private String buildPagedSql(String baseSql, String dbType, int offset, int pageSize) {
        switch (dbType.toUpperCase()) {
            case "MYSQL":
            case "POSTGRES":
                return baseSql + " LIMIT " + pageSize + " OFFSET " + offset;
                
            case "ORACLE":
                return "SELECT * FROM ( " +
                       "SELECT ROWNUM rn, t.* FROM (" + baseSql + ") t " +
                       "WHERE ROWNUM <= " + (offset + pageSize) + " ) " +
                       "WHERE rn > " + offset;
                
            case "SQLSERVER":
            default:
                return baseSql + " OFFSET " + offset + " ROWS FETCH NEXT " + pageSize + " ROWS ONLY";
        }
    }
    
    /**
     * 建立計數 SQL
     */
    private String buildCountSql(String baseSql) {
        // 移除 ORDER BY 子句 (提升效能)
        String cleanSql = baseSql.replaceAll("(?i)ORDER\\s+BY[^)]*$", "").trim();
        return "SELECT COUNT(*) FROM (" + cleanSql + ") AS count_query";
    }
    
    /**
     * 取得可用的查詢配置列表
     */
    public List<Map<String, Object>> getAvailableQueries(String category) {
        List<ApiQueryConfig> configs;
        
        if (category != null && !category.isEmpty()) {
            configs = queryConfigRepository.findByCategoryAndIsEnabled(category, true);
        } else {
            configs = queryConfigRepository.findByIsEnabled(true);
        }
        
        return configs.stream().map(config -> {
            Map<String, Object> info = new HashMap<>();
            info.put("queryCode", config.getQueryCode());
            info.put("queryName", config.getQueryName());
            info.put("category", config.getCategory());
            info.put("description", config.getDescription());
            info.put("dataSource", config.getDatasourceCode());
            info.put("requireApiKey", config.getRequireApiKey());
            info.put("maxPageSize", config.getMaxPageSize());
            return info;
        }).collect(Collectors.toList());
    }
    
    /**
     * 取得查詢配置詳情
     */
    public Map<String, Object> getQueryConfig(String queryCode) {
        ApiQueryConfig config = queryConfigRepository
            .findByQueryCodeAndIsEnabled(queryCode, true)
            .orElseThrow(() -> new RuntimeException("查詢配置不存在: " + queryCode));
        
        Map<String, Object> result = new HashMap<>();
        result.put("queryCode", config.getQueryCode());
        result.put("queryName", config.getQueryName());
        result.put("category", config.getCategory());
        result.put("description", config.getDescription());
        result.put("dataSource", config.getDatasourceCode());
        result.put("requireApiKey", config.getRequireApiKey());
        result.put("maxPageSize", config.getMaxPageSize());
        result.put("cacheSeconds", config.getCacheSeconds());
        
        // 參數定義
        if (config.getParams() != null && !config.getParams().isEmpty()) {
            List<Map<String, Object>> paramsList = config.getParams().stream().map(param -> {
                Map<String, Object> p = new HashMap<>();
                p.put("paramName", param.getParamName());
                p.put("paramType", param.getParamType());
                p.put("isRequired", param.getIsRequired());
                p.put("defaultValue", param.getDefaultValue());
                p.put("description", param.getDescription());
                return p;
            }).collect(Collectors.toList());
            result.put("params", paramsList);
        } else {
            result.put("params", Collections.emptyList());
        }
        
        return result;
    }
}