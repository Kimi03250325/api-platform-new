package com.bank.apiplatform.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 查詢參數定義實體
 */
@Entity
@Table(name = "api_query_params")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiQueryParam {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "param_id")
    private Integer paramId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "query_id", nullable = false)
    @JsonIgnore
    private ApiQueryConfig queryConfig;
    
    @Column(name = "param_name", nullable = false, length = 100)
    private String paramName;
    
    @Column(name = "param_type", nullable = false, length = 20)
    private String paramType;
    
    @Column(name = "is_required", nullable = false)
    private Boolean isRequired = false;
    
    @Column(name = "default_value", length = 200)
    private String defaultValue;
    
    @Column(name = "validation_regex", length = 500)
    private String validationRegex;
    
    @Column(name = "description", length = 200)
    private String description;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}