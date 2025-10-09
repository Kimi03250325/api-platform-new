package com.bank.apiplatform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 客戶實體類別
 */
@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long customerId;
    
    @Column(name = "customer_code", nullable = false, unique = true, length = 50)
    private String customerCode;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "id_number", length = 20)
    private String idNumber;
    
    @Column(name = "email", length = 100)
    private String email;
    
    @Column(name = "phone", length = 20)
    private String phone;
    
    @Column(name = "risk_level", length = 20)
    private String riskLevel;
    
    @Column(name = "account_status", nullable = false, length = 20)
    private String accountStatus = "ACTIVE";
    
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