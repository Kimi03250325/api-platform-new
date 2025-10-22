package com.bank.apiplatform.service;

import com.bank.apiplatform.dto.request.QueryConfigRequest;
import com.bank.apiplatform.entity.ApiQueryConfig;
import com.bank.apiplatform.repository.ApiQueryConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 查詢配置服務
 * 
 * 路徑: src/main/java/com/bank/apiplatform/service/QueryConfigService.java
 * 
 * 建立方法:
 * 1. 在 Eclipse 右鍵點擊 com.bank.apiplatform.service 套件
 * 2. New > Class
 * 3. Name: QueryConfigService
 * 4. Finish
 * 5. 複製此檔案內容，取代原內容
 * 6. Ctrl + S 儲存
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QueryConfigService {
    
    private final ApiQueryConfigRepository queryConfigRepository;
    
    /**
     * 查詢所有配置
     * 
     * @return 查詢配置列表
     */
    public List<ApiQueryConfig> findAll() {
        log.debug("查詢所有查詢配置");
        return queryConfigRepository.findAll();
    }
    
    /**
     * 根據分類查詢
     * 
     * @param category 分類
     * @return 查詢配置列表
     */
    public List<ApiQueryConfig> findByCategory(String category) {
        log.debug("查詢配置: category={}", category);
        return queryConfigRepository.findByCategoryAndIsEnabled(category, true);
    }
    
    /**
     * 根據查詢代碼查詢
     * 
     * @param queryCode 查詢代碼
     * @return 查詢配置
     * @throws RuntimeException 如果查詢配置不存在
     */
    public ApiQueryConfig findByQueryCode(String queryCode) {
        log.debug("查詢配置: queryCode={}", queryCode);
        return queryConfigRepository.findByQueryCodeAndIsEnabled(queryCode, true)
            .orElseThrow(() -> new RuntimeException("查詢配置不存在: " + queryCode));
    }
    
    /**
     * 建立查詢配置
     * 
     * @param request 查詢配置請求
     * @return 建立的查詢配置
     * @throws RuntimeException 如果查詢代碼已存在或 SQL 格式錯誤
     */
    @Transactional
    public ApiQueryConfig create(QueryConfigRequest request) {
        log.info("建立查詢配置: queryCode={}", request.getQueryCode());
        
        // 檢查代碼是否已存在
        if (queryConfigRepository.existsByQueryCode(request.getQueryCode())) {
            log.error("查詢代碼已存在: {}", request.getQueryCode());
            throw new RuntimeException("查詢代碼已存在: " + request.getQueryCode());
        }
        
        // 驗證 SQL 參數格式
        validateSqlParams(request.getQuerySql());
        
        // 建立新的查詢配置
        ApiQueryConfig config = new ApiQueryConfig();
        copyProperties(request, config);
        
        ApiQueryConfig saved = queryConfigRepository.save(config);
        log.info("查詢配置建立成功: id={}, queryCode={}", saved.getQueryId(), saved.getQueryCode());
        
        return saved;
    }
    
    /**
     * 更新查詢配置
     * 
     * @param queryCode 查詢代碼
     * @param request 查詢配置請求
     * @return 更新的查詢配置
     * @throws RuntimeException 如果查詢配置不存在或代碼已被使用
     */
    @Transactional
    public ApiQueryConfig update(String queryCode, QueryConfigRequest request) {
        log.info("更新查詢配置: queryCode={}", queryCode);
        
        ApiQueryConfig config = queryConfigRepository.findByQueryCode(queryCode)
            .orElseThrow(() -> new RuntimeException("查詢配置不存在: " + queryCode));
        
        // 如果修改了代碼，檢查新代碼是否已存在
        if (!config.getQueryCode().equals(request.getQueryCode())) {
            if (queryConfigRepository.existsByQueryCode(request.getQueryCode())) {
                log.error("查詢代碼已存在: {}", request.getQueryCode());
                throw new RuntimeException("查詢代碼已存在: " + request.getQueryCode());
            }
        }
        
        // 驗證 SQL 參數格式
        validateSqlParams(request.getQuerySql());
        
        copyProperties(request, config);
        ApiQueryConfig updated = queryConfigRepository.save(config);
        
        log.info("查詢配置更新成功: id={}, queryCode={}", updated.getQueryId(), updated.getQueryCode());
        
        return updated;
    }
    
    /**
     * 刪除查詢配置
     * 
     * @param queryCode 查詢代碼
     * @throws RuntimeException 如果查詢配置不存在
     */
    @Transactional
    public void delete(String queryCode) {
        log.info("刪除查詢配置: queryCode={}", queryCode);
        
        ApiQueryConfig config = queryConfigRepository.findByQueryCode(queryCode)
            .orElseThrow(() -> new RuntimeException("查詢配置不存在: " + queryCode));
        
        // 級聯刪除參數會由資料庫處理 (ON DELETE CASCADE)
        queryConfigRepository.delete(config);
        log.info("查詢配置刪除成功: id={}, queryCode={}", config.getQueryId(), queryCode);
    }
    
    /**
     * 驗證 SQL 語法
     * 
     * @param sql SQL 語句
     * @return 驗證結果訊息
     * @throws RuntimeException 如果 SQL 為空或使用錯誤的參數格式
     */
    public String validateSql(String sql) {
        log.debug("驗證 SQL: {}", sql);
        
        if (sql == null || sql.trim().isEmpty()) {
            throw new RuntimeException("SQL 不能為空");
        }
        
        // 檢查是否使用了錯誤的 @ 參數格式（T-SQL 原生格式）
        Pattern atParamPattern = Pattern.compile("@(\\w+)(?!\\w)");
        Matcher atMatcher = atParamPattern.matcher(sql);
        
        List<String> errorParams = new ArrayList<>();
        while (atMatcher.find()) {
            String param = atMatcher.group();
            // 排除 SQL 中的特殊用法，例如 @@IDENTITY
            if (!param.startsWith("@@")) {
                errorParams.add(param);
            }
        }
        
        if (!errorParams.isEmpty()) {
            String errorMsg = "SQL 使用了錯誤的參數格式: " + String.join(", ", errorParams) + 
                            "\n請改用 JDBC 標準格式 :paramName (冒號開頭)";
            log.error("SQL 驗證失敗: {}", errorMsg);
            throw new RuntimeException(errorMsg);
        }
        
        // 檢查正確的 : 參數格式（JDBC 標準）
        Pattern colonParamPattern = Pattern.compile(":(\\w+)");
        Matcher colonMatcher = colonParamPattern.matcher(sql);
        
        List<String> params = new ArrayList<>();
        while (colonMatcher.find()) {
            params.add(colonMatcher.group());
        }
        
        if (!params.isEmpty()) {
            String result = "✅ SQL 驗證通過！偵測到參數: " + String.join(", ", params);
            log.info(result);
            return result;
        } else {
            String result = "✅ SQL 驗證通過！此 SQL 未使用參數";
            log.info(result);
            return result;
        }
    }
    
    /**
     * 驗證 SQL 參數格式（內部使用）
     * 
     * @param sql SQL 語句
     * @throws RuntimeException 如果 SQL 格式錯誤
     */
    private void validateSqlParams(String sql) {
        try {
            validateSql(sql);
        } catch (RuntimeException e) {
            throw e;
        }
    }
    
    /**
     * 複製屬性從 Request 到 Entity
     * 
     * @param request 請求物件
     * @param config Entity 物件
     */
    private void copyProperties(QueryConfigRequest request, ApiQueryConfig config) {
        config.setQueryCode(request.getQueryCode());
        config.setQueryName(request.getQueryName());
        config.setDatasourceCode(request.getDatasourceCode());
        config.setQuerySql(request.getQuerySql());
        config.setDescription(request.getDescription());
        config.setCategory(request.getCategory());
        config.setIsEnabled(request.getIsEnabled() != null ? request.getIsEnabled() : true);
        config.setRequireApiKey(request.getRequireApiKey() != null ? request.getRequireApiKey() : true);
        config.setMaxPageSize(request.getMaxPageSize() != null ? request.getMaxPageSize() : 100);
        config.setCacheSeconds(request.getCacheSeconds() != null ? request.getCacheSeconds() : 0);
        config.setCreatedBy(request.getCreatedBy());
    }
    
    /**
     * 查詢所有已啟用的配置
     * 
     * @return 已啟用的查詢配置列表
     */
    public List<ApiQueryConfig> findAllEnabled() {
        log.debug("查詢所有已啟用的查詢配置");
        return queryConfigRepository.findByIsEnabled(true);
    }
    
    /**
     * 根據資料源代碼查詢配置
     * 
     * @param datasourceCode 資料源代碼
     * @return 查詢配置列表
     */
    public List<ApiQueryConfig> findByDatasourceCode(String datasourceCode) {
        log.debug("查詢配置: datasourceCode={}", datasourceCode);
        return queryConfigRepository.findByDatasourceCodeAndIsEnabled(datasourceCode, true);
    }
    
    /**
     * 檢查查詢代碼是否存在
     * 
     * @param queryCode 查詢代碼
     * @return true 表示存在，false 表示不存在
     */
    public boolean existsByQueryCode(String queryCode) {
        return queryConfigRepository.existsByQueryCode(queryCode);
    }
    
    /**
     * 根據分類統計查詢數量
     * 
     * @param category 分類
     * @return 查詢數量
     */
    public long countByCategory(String category) {
        return queryConfigRepository.findByCategoryAndIsEnabled(category, true).size();
    }
    
    /**
     * 提取 SQL 中的參數名稱列表
     * 
     * @param sql SQL 語句
     * @return 參數名稱列表
     */
    public List<String> extractParamNames(String sql) {
        List<String> paramNames = new ArrayList<>();
        
        if (sql == null || sql.trim().isEmpty()) {
            return paramNames;
        }
        
        Pattern colonParamPattern = Pattern.compile(":(\\w+)");
        Matcher colonMatcher = colonParamPattern.matcher(sql);
        
        while (colonMatcher.find()) {
            String paramName = colonMatcher.group(1); // 不包含冒號
            if (!paramNames.contains(paramName)) {
                paramNames.add(paramName);
            }
        }
        
        return paramNames;
    }
}