package com.bank.apiplatform.service;

import com.bank.apiplatform.entity.Customer;

import java.util.List;

/**
 * 客戶服務介面
 */
public interface CustomerService {
    
    /**
     * 建立新客戶
     */
    Customer createCustomer(Customer customer);
    
    /**
     * 根據 ID 查詢客戶
     */
    Customer getCustomerById(Long customerId);
    
    /**
     * 根據客戶代碼查詢
     */
    Customer getCustomerByCode(String customerCode);
    
    /**
     * 查詢所有客戶
     */
    List<Customer> getAllCustomers();
    
    /**
     * 更新客戶資訊
     */
    Customer updateCustomer(Long customerId, Customer customer);
    
    /**
     * 刪除客戶
     */
    void deleteCustomer(Long customerId);
    
    /**
     * 搜尋客戶
     */
    List<Customer> searchCustomers(String keyword);
}