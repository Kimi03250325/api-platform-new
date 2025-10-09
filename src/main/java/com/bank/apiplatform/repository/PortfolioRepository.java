package com.bank.apiplatform.repository;

import com.bank.apiplatform.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Portfolio Repository
 */
@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    
    /**
     * 根據客戶 ID 查詢所有投資組合
     */
    List<Portfolio> findByCustomerId(Long customerId);
    
    /**
     * 根據客戶 ID 和狀態查詢
     */
    List<Portfolio> findByCustomerIdAndStatus(Long customerId, String status);
    
    /**
     * 根據投資組合代碼查詢
     */
    Optional<Portfolio> findByPortfolioCode(String portfolioCode);
    
    /**
     * 檢查投資組合代碼是否存在
     */
    boolean existsByPortfolioCode(String portfolioCode);
}