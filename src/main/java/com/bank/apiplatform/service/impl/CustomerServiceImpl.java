package com.bank.apiplatform.service.impl;

import com.bank.apiplatform.entity.Customer;
import com.bank.apiplatform.repository.CustomerRepository;
import com.bank.apiplatform.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 客戶服務實作
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {
    
    private final CustomerRepository customerRepository;
    
    @Override
    public Customer createCustomer(Customer customer) {
        log.info("建立新客戶：{}", customer.getCustomerCode());
        
        // 檢查客戶代碼是否已存在
        if (customerRepository.existsByCustomerCode(customer.getCustomerCode())) {
            throw new RuntimeException("客戶代碼已存在：" + customer.getCustomerCode());
        }
        
        // 設定預設狀態
        if (customer.getAccountStatus() == null) {
            customer.setAccountStatus("ACTIVE");
        }
        
        Customer savedCustomer = customerRepository.save(customer);
        log.info("客戶建立成功，ID：{}", savedCustomer.getCustomerId());
        
        return savedCustomer;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Customer getCustomerById(Long customerId) {
        log.info("查詢客戶 ID：{}", customerId);
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("找不到客戶，ID：" + customerId));
    }
    
    @Override
    @Transactional(readOnly = true)
    public Customer getCustomerByCode(String customerCode) {
        log.info("查詢客戶代碼：{}", customerCode);
        return customerRepository.findByCustomerCode(customerCode)
                .orElseThrow(() -> new RuntimeException("找不到客戶，代碼：" + customerCode));
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Customer> getAllCustomers() {
        log.info("查詢所有客戶");
        return customerRepository.findAll();
    }
    
    @Override
    public Customer updateCustomer(Long customerId, Customer customer) {
        log.info("更新客戶 ID：{}", customerId);
        
        Customer existingCustomer = getCustomerById(customerId);
        
        // 更新欄位
        existingCustomer.setName(customer.getName());
        existingCustomer.setIdNumber(customer.getIdNumber());
        existingCustomer.setEmail(customer.getEmail());
        existingCustomer.setPhone(customer.getPhone());
        existingCustomer.setRiskLevel(customer.getRiskLevel());
        existingCustomer.setAccountStatus(customer.getAccountStatus());
        
        Customer updatedCustomer = customerRepository.save(existingCustomer);
        log.info("客戶更新成功，ID：{}", customerId);
        
        return updatedCustomer;
    }
    
    @Override
    public void deleteCustomer(Long customerId) {
        log.info("刪除客戶 ID：{}", customerId);
        
        if (!customerRepository.existsById(customerId)) {
            throw new RuntimeException("找不到客戶，ID：" + customerId);
        }
        
        customerRepository.deleteById(customerId);
        log.info("客戶刪除成功，ID：{}", customerId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Customer> searchCustomers(String keyword) {
        log.info("搜尋客戶，關鍵字：{}", keyword);
        return customerRepository.findByNameContaining(keyword);
    }
}