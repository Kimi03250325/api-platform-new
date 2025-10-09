package com.bank.apiplatform.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 投資組合實體類別
 */
@Entity
@Table(name = "portfolios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Portfolio {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portfolio_id")
    private Long portfolioId;
    
    @Column(name = "customer_id", nullable = false)
    private Long customerId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", insertable = false, updatable = false)
    private Customer customer;
    
    @Column(name = "portfolio_code", nullable = false, unique = true, length = 50)
    private String portfolioCode;
    
    @Column(name = "portfolio_name", length = 100)
    private String portfolioName;
    
    @Column(name = "total_value", precision = 18, scale = 2)
    private BigDecimal totalValue;
    
    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "TWD";
    
    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE";
    
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