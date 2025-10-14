package com.bank.apiplatform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 查詢配置實體
 */
@Entity
@Table(name = "api_query_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiQueryConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "query_id")
    private Integer queryId;
    
    @Column(name = "query_code", nullable = false, unique = true, length = 100)
    private String queryCode;
    
    @Column(name = "query_name", nullable = false, length = 200)
    private String queryName;
    
    @Column(name = "datasource_code", nullable = false, length = 50)
    private String datasourceCode;
    
    @Column(name = "query_sql", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String querySql;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "category", length = 50)
    private String category;
    
    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled = true;
    
    @Column(name = "require_api_key", nullable = false)
    private Boolean requireApiKey = true;
    
    @Column(name = "max_page_size")
    private Integer maxPageSize = 100;
    
    @Column(name = "cache_seconds")
    private Integer cacheSeconds = 0;
    
    @Column(name = "created_by", length = 50)
    private String createdBy;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "queryConfig", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ApiQueryParam> params;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}