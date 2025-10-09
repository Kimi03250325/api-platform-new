package com.bank.apiplatform.repository;

import com.bank.apiplatform.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Transaction Repository
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    /**
     * 根據投資組合 ID 查詢所有交易
     */
    List<Transaction> findByPortfolioId(Long portfolioId);
    
    /**
     * 根據投資組合 ID 和日期範圍查詢（分頁）
     */
    Page<Transaction> findByPortfolioIdAndTransactionDateBetween(
        Long portfolioId, 
        LocalDateTime startDate, 
        LocalDateTime endDate, 
        Pageable pageable
    );
    
    /**
     * 查詢最近的交易記錄
     */
    @Query("SELECT t FROM Transaction t WHERE t.portfolioId = :portfolioId ORDER BY t.transactionDate DESC")
    List<Transaction> findRecentTransactions(@Param("portfolioId") Long portfolioId, Pageable pageable);
}