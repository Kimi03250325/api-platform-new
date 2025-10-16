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
        
        // 5. 處理分頁參數（防止負值和異常值）
        int maxPageSize = queryConfig.getMaxPageSize() != null ? queryConfig.getMaxPageSize() : 100;
        
        // 確保 pageSize 在合理範圍內
        int actualPageSize = pageSize != null && pageSize > 0 ? 
                             Math.min(pageSize, maxPageSize) : 20;
        
        // 確保 pageNum 至少為 1
        int actualPageNum = pageNum != null && pageNum > 0 ? pageNum : 1;
        
        // 計算 offset（確保不會為負數）
        int offset = Math.max(0, (actualPageNum - 1) * actualPageSize);
        
        log.debug("分頁參數: pageNum={}, pageSize={}, offset={}, maxPageSize={}", 
                  actualPageNum, actualPageSize, offset, maxPageSize);
        
        // 6. 取得對應的資料源
        DataSource dataSource = dataSourceManager.getDataSource(dsConfig);
        NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        
        // 7. 構建完整的查詢 SQL（包含分頁）
        String finalSql = buildPagedQuery(
            queryConfig.getQuerySql(), 
            dsConfig.getDbType(), 
            offset,
            actualPageSize
        );
        
        log.debug("執行 SQL: {}", finalSql);
        log.debug("參數: {}", processedParams);
        
        try {
            // 8. 執行查詢
            List<Map<String, Object>> records = jdbcTemplate.queryForList(finalSql, processedParams);
            
            // 9. 查詢總筆數（用於分頁資訊）
            int total = getTotalCount(jdbcTemplate, queryConfig.getQuerySql(), processedParams);
            
            // 10. 組裝回應
            Map<String, Object> result = new HashMap<>();
            result.put("records", records);
            result.put("total", total);
            result.put("pageNum", actualPageNum);
            result.put("pageSize", actualPageSize);
            result.put("totalPages", (int) Math.ceil((double) total / actualPageSize));
            
            log.info("查詢成功: queryCode={}, 返回 {} 筆資料，共 {} 筆", 
                     queryCode, records.size(), total);
            
            return result;
            
        } catch (Exception e) {
            log.error("執行查詢失敗: queryCode={}, SQL={}, 錯誤: {}", 
                      queryCode, finalSql, e.getMessage(), e);
            throw new RuntimeException("查詢執行失敗: " + e.getMessage(), e);
        }
    }
    
    /**
     * 根據資料庫類型構建分頁查詢
     */
    private String buildPagedQuery(String baseSql, String dbType, int offset, int pageSize) {
        // 確保 offset 不為負數
        offset = Math.max(0, offset);
        pageSize = Math.max(1, pageSize);
        
        if (dbType == null) {
            dbType = "SQLSERVER";
        }
        
        switch (dbType.toUpperCase()) {
            case "MYSQL":
            case "MARIADB":
                // MySQL 風格: LIMIT offset, size
                return baseSql + " LIMIT " + offset + ", " + pageSize;
                
            case "POSTGRES":
            case "POSTGRESQL":
                // PostgreSQL 風格: LIMIT size OFFSET offset
                return baseSql + " LIMIT " + pageSize + " OFFSET " + offset;
                
            case "SQLSERVER":
            case "MSSQL":
                // SQL Server 2012+ 風格: OFFSET ... ROWS FETCH NEXT ... ROWS ONLY
                // 注意：SQL Server 的 OFFSET 必須配合 ORDER BY
                if (!baseSql.toUpperCase().contains("ORDER BY")) {
                    log.warn("SQL Server OFFSET 需要 ORDER BY 子句，自動添加預設排序");
                    baseSql += " ORDER BY (SELECT NULL)";
                }
                return baseSql + " OFFSET " + offset + " ROWS FETCH NEXT " + pageSize + " ROWS ONLY";
                
            case "ORACLE":
                // Oracle 12c+ 風格
                if (!baseSql.toUpperCase().contains("ORDER BY")) {
                    baseSql += " ORDER BY 1";
                }
                return baseSql + " OFFSET " + offset + " ROWS FETCH NEXT " + pageSize + " ROWS ONLY";
                
            default:
                // 預設使用 SQL Server 風格
                if (!baseSql.toUpperCase().contains("ORDER BY")) {
                    baseSql += " ORDER BY (SELECT NULL)";
                }
                return baseSql + " OFFSET " + offset + " ROWS FETCH NEXT " + pageSize + " ROWS ONLY";
        }
    }
    
    /**
     * 查詢總筆數
     */
    private int getTotalCount(NamedParameterJdbcTemplate jdbcTemplate, 
                              String baseSql, 
                              Map<String, Object> params) {
        try {
            // 移除 ORDER BY 子句（提高效能）
            String countSql = baseSql;
            int orderByIndex = countSql.toUpperCase().lastIndexOf("ORDER BY");
            if (orderByIndex > 0) {
                countSql = countSql.substring(0, orderByIndex);
            }
            
            // 構建 COUNT 查詢
            countSql = "SELECT COUNT(*) FROM (" + countSql + ") AS count_query";
            
            Integer count = jdbcTemplate.queryForObject(countSql, params, Integer.class);
            return count != null ? count : 0;
            
        } catch (Exception e) {
            log.warn("查詢總筆數失敗，返回 0: {}", e.getMessage());
            return 0;
        }
    }
    
    /**
     * 驗證必要參數
     */
    private void validateParams(ApiQueryConfig queryConfig, Map<String, Object> params) {
        List<ApiQueryParam> requiredParams = queryConfig.getParams().stream()
            .filter(ApiQueryParam::getIsRequired)
            .collect(Collectors.toList());
        
        for (ApiQueryParam param : requiredParams) {
            if (params == null || !params.containsKey(param.getParamName()) 
                || params.get(param.getParamName()) == null) {
                throw new IllegalArgumentException("缺少必要參數: " + param.getParamName());
            }
        }
    }
    
    /**
     * 處理參數：類型轉換、預設值、格式化
     */
    private Map<String, Object> processParams(Map<String, Object> inputParams, 
                                              ApiQueryConfig queryConfig) {
        Map<String, Object> processed = new HashMap<>();
        
        if (inputParams != null) {
            processed.putAll(inputParams);
        }
        
        // 處理參數定義
        for (ApiQueryParam paramDef : queryConfig.getParams()) {
            String paramName = paramDef.getParamName();
            Object value = processed.get(paramName);
            
            // 使用預設值
            if (value == null && paramDef.getDefaultValue() != null) {
                value = paramDef.getDefaultValue();
            }
            
            // 類型轉換
            if (value != null) {
                value = convertParamType(value, paramDef.getParamType());
            }
            
            processed.put(paramName, value);
        }
        
        return processed;
    }
    
    /**
     * 參數類型轉換
     */
    private Object convertParamType(Object value, String paramType) {
        if (value == null || paramType == null) {
            return value;
        }
        
        try {
            switch (paramType.toUpperCase()) {
                case "INTEGER":
                case "INT":
                    if (value instanceof Number) {
                        return ((Number) value).intValue();
                    }
                    return Integer.parseInt(value.toString());
                    
                case "LONG":
                    if (value instanceof Number) {
                        return ((Number) value).longValue();
                    }
                    return Long.parseLong(value.toString());
                    
                case "DOUBLE":
                case "DECIMAL":
                    if (value instanceof Number) {
                        return ((Number) value).doubleValue();
                    }
                    return Double.parseDouble(value.toString());
                    
                case "BOOLEAN":
                    if (value instanceof Boolean) {
                        return value;
                    }
                    return Boolean.parseBoolean(value.toString());
                    
                case "DATE":
                    if (value instanceof LocalDate) {
                        return value;
                    }
                    return LocalDate.parse(value.toString(), DateTimeFormatter.ISO_DATE);
                    
                case "DATETIME":
                case "TIMESTAMP":
                    if (value instanceof LocalDateTime) {
                        return value;
                    }
                    // 嘗試多種日期格式
                    String dateStr = value.toString();
                    try {
                        return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    } catch (Exception e1) {
                        try {
                            return LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME);
                        } catch (Exception e2) {
                            return LocalDateTime.parse(dateStr);
                        }
                    }
                    
                case "STRING":
                default:
                    return value.toString();
            }
        } catch (Exception e) {
            log.warn("參數類型轉換失敗: {} -> {}, 使用原值", value, paramType);
            return value;
        }
    }
    
    /**
     * 查詢可用的查詢配置列表
     */
    public List<Map<String, Object>> getAvailableQueries(String category) {
        List<ApiQueryConfig> configs;
        
        if (category != null && !category.trim().isEmpty()) {
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
            info.put("requireApiKey", config.getRequireApiKey());
            info.put("maxPageSize", config.getMaxPageSize());
            info.put("datasourceCode", config.getDatasourceCode());
            
            // 參數摘要
            List<String> paramNames = config.getParams().stream()
                .map(ApiQueryParam::getParamName)
                .collect(Collectors.toList());
            info.put("parameters", paramNames);
            
            // 參數數量統計
            long requiredCount = config.getParams().stream()
                .filter(ApiQueryParam::getIsRequired)
                .count();
            info.put("requiredParamCount", requiredCount);
            info.put("totalParamCount", config.getParams().size());
            
            return info;
        }).collect(Collectors.toList());
    }
    
    /**
     * 查詢配置詳情（包含參數定義）
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
        result.put("datasourceCode", config.getDatasourceCode());
        result.put("maxPageSize", config.getMaxPageSize());
        result.put("cacheSeconds", config.getCacheSeconds());
        result.put("requireApiKey", config.getRequireApiKey());
        
        // 參數定義
        List<Map<String, Object>> params = config.getParams().stream().map(param -> {
            Map<String, Object> paramInfo = new HashMap<>();
            paramInfo.put("paramName", param.getParamName());
            paramInfo.put("paramType", param.getParamType());
            paramInfo.put("isRequired", param.getIsRequired());
            paramInfo.put("defaultValue", param.getDefaultValue());
            paramInfo.put("description", param.getDescription());
            paramInfo.put("validationRegex", param.getValidationRegex());
            return paramInfo;
        }).collect(Collectors.toList());
        
        result.put("params", params);
        
        // 範例請求
        Map<String, Object> exampleRequest = new HashMap<>();
        Map<String, Object> exampleParams = new HashMap<>();
        
        for (ApiQueryParam param : config.getParams()) {
            if (param.getDefaultValue() != null) {
                exampleParams.put(param.getParamName(), param.getDefaultValue());
            } else if (param.getIsRequired()) {
                exampleParams.put(param.getParamName(), getExampleValue(param.getParamType()));
            }
        }
        
        exampleRequest.put("params", exampleParams);
        exampleRequest.put("pageNum", 1);
        exampleRequest.put("pageSize", 20);
        
        result.put("exampleRequest", exampleRequest);
        
        // GET 請求範例 URL
        StringBuilder getUrlExample = new StringBuilder("/api/query/execute/" + queryCode + "?");
        List<String> queryParams = new ArrayList<>();
        
        for (Map.Entry<String, Object> entry : exampleParams.entrySet()) {
            queryParams.add(entry.getKey() + "=" + entry.getValue());
        }
        queryParams.add("pageNum=1");
        queryParams.add("pageSize=20");
        
        getUrlExample.append(String.join("&", queryParams));
        result.put("getUrlExample", getUrlExample.toString());
        
        return result;
    }
    
    /**
     * 根據參數類型生成範例值
     */
    private Object getExampleValue(String paramType) {
        if (paramType == null) {
            return "範例值";
        }
        
        switch (paramType.toUpperCase()) {
            case "INTEGER":
            case "INT":
                return 1;
            case "LONG":
                return 1L;
            case "DOUBLE":
            case "DECIMAL":
                return 100.50;
            case "BOOLEAN":
                return true;
            case "DATE":
                return LocalDate.now().toString();
            case "DATETIME":
            case "TIMESTAMP":
                return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            case "STRING":
            default:
                return "範例值";
        }
    }
    
    /**
     * 取得查詢分類統計
     */
    public Map<String, Object> getQueryStatistics() {
        List<ApiQueryConfig> allConfigs = queryConfigRepository.findByIsEnabled(true);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalQueries", allConfigs.size());
        
        // 按分類統計
        Map<String, Long> categoryCount = allConfigs.stream()
            .collect(Collectors.groupingBy(
                config -> config.getCategory() != null ? config.getCategory() : "未分類",
                Collectors.counting()
            ));
        stats.put("categoryCount", categoryCount);
        
        // 按資料源統計
        Map<String, Long> datasourceCount = allConfigs.stream()
            .collect(Collectors.groupingBy(
                ApiQueryConfig::getDatasourceCode,
                Collectors.counting()
            ));
        stats.put("datasourceCount", datasourceCount);
        
        // 需要 API Key 的查詢數量
        long requireApiKeyCount = allConfigs.stream()
            .filter(ApiQueryConfig::getRequireApiKey)
            .count();
        stats.put("requireApiKeyCount", requireApiKeyCount);
        
        return stats;
    }
}