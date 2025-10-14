package com.bank.apiplatform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 資料源配置實體
 */
@Entity
@Table(name = "api_datasource_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiDataSourceConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "datasource_id")
    private Integer datasourceId;
    
    @Column(name = "datasource_code", nullable = false, unique = true, length = 50)
    private String datasourceCode;
    
    @Column(name = "datasource_name", nullable = false, length = 100)
    private String datasourceName;
    
    @Column(name = "db_type", nullable = false, length = 20)
    private String dbType;
    
    @Column(name = "jdbc_url", nullable = false, length = 500)
    private String jdbcUrl;
    
    @Column(name = "username", nullable = false, length = 100)
    private String username;
    
    @Column(name = "password_encrypted", nullable = false, length = 500)
    private String passwordEncrypted;
    
    @Column(name = "driver_class", length = 200)
    private String driverClass;
    
    @Column(name = "max_pool_size")
    private Integer maxPoolSize;
    
    @Column(name = "min_idle")
    private Integer minIdle;
    
    @Column(name = "connection_timeout")
    private Integer connectionTimeout;
    
    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled = true;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
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