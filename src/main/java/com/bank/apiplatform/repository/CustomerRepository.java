package com.bank.apiplatform.repository;

import com.bank.apiplatform.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Customer Repository
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    /**
     * 根據客戶代碼查詢
     */
    Optional<Customer> findByCustomerCode(String customerCode);
    
    /**
     * 根據帳戶狀態查詢
     */
    List<Customer> findByAccountStatus(String status);
    
    /**
     * 根據名稱模糊查詢
     */
    List<Customer> findByNameContaining(String keyword);
    
    /**
     * 檢查客戶代碼是否存在
     */
    boolean existsByCustomerCode(String customerCode);
}