package com.bank.apiplatform.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 交易記錄實體類別
 */
@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;
    
    @Column(name = "portfolio_id", nullable = false)
    private Long portfolioId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", insertable = false, updatable = false)
    private Portfolio portfolio;
    
    @Column(name = "transaction_type", nullable = false, length = 20)
    private String transactionType;
    
    @Column(name = "instrument_code", length = 50)
    private String instrumentCode;
    
    @Column(name = "quantity", precision = 18, scale = 4)
    private BigDecimal quantity;
    
    @Column(name = "price", precision = 18, scale = 2)
    private BigDecimal price;
    
    @Column(name = "amount", precision = 18, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "currency", length = 3)
    private String currency;
    
    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;
    
    @Column(name = "settlement_date")
    private LocalDate settlementDate;
    
    @Column(name = "status", nullable = false, length = 20)
    private String status;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}