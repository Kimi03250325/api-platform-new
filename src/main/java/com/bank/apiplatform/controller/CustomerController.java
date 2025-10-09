package com.bank.apiplatform.controller;

import com.bank.apiplatform.dto.response.ApiResponse;
import com.bank.apiplatform.entity.Customer;
import com.bank.apiplatform.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 客戶 API 控制器
 */
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "客戶管理", description = "客戶相關 API")
public class CustomerController {
    
    private final CustomerService customerService;
    
    /**
     * 建立新客戶
     */
    @PostMapping
    @Operation(summary = "建立客戶", description = "建立新的客戶資料")
    public ApiResponse<Customer> createCustomer(@Valid @RequestBody Customer customer) {
        log.info("收到建立客戶請求：{}", customer.getCustomerCode());
        Customer createdCustomer = customerService.createCustomer(customer);
        return ApiResponse.success("客戶建立成功", createdCustomer);
    }
    
    /**
     * 查詢所有客戶
     */
    @GetMapping
    @Operation(summary = "查詢所有客戶", description = "取得所有客戶清單")
    public ApiResponse<List<Customer>> getAllCustomers() {
        log.info("收到查詢所有客戶請求");
        List<Customer> customers = customerService.getAllCustomers();
        return ApiResponse.success("查詢成功", customers);
    }
    
    /**
     * 根據 ID 查詢客戶
     */
    @GetMapping("/{customerId}")
    @Operation(summary = "查詢客戶", description = "根據 ID 查詢客戶資料")
    public ApiResponse<Customer> getCustomerById(@PathVariable Long customerId) {
        log.info("收到查詢客戶請求，ID：{}", customerId);
        Customer customer = customerService.getCustomerById(customerId);
        return ApiResponse.success("查詢成功", customer);
    }
    
    /**
     * 根據客戶代碼查詢
     */
    @GetMapping("/code/{customerCode}")
    @Operation(summary = "根據代碼查詢", description = "根據客戶代碼查詢")
    public ApiResponse<Customer> getCustomerByCode(@PathVariable String customerCode) {
        log.info("收到查詢客戶請求，代碼：{}", customerCode);
        Customer customer = customerService.getCustomerByCode(customerCode);
        return ApiResponse.success("查詢成功", customer);
    }
    
    /**
     * 更新客戶資訊
     */
    @PutMapping("/{customerId}")
    @Operation(summary = "更新客戶", description = "更新客戶資料")
    public ApiResponse<Customer> updateCustomer(
            @PathVariable Long customerId,
            @Valid @RequestBody Customer customer) {
        log.info("收到更新客戶請求，ID：{}", customerId);
        Customer updatedCustomer = customerService.updateCustomer(customerId, customer);
        return ApiResponse.success("更新成功", updatedCustomer);
    }
    
    /**
     * 刪除客戶
     */
    @DeleteMapping("/{customerId}")
    @Operation(summary = "刪除客戶", description = "刪除客戶資料")
    public ApiResponse<Void> deleteCustomer(@PathVariable Long customerId) {
        log.info("收到刪除客戶請求，ID：{}", customerId);
        customerService.deleteCustomer(customerId);
        return ApiResponse.success("刪除成功", null);
    }
    
    /**
     * 搜尋客戶
     */
    @GetMapping("/search")
    @Operation(summary = "搜尋客戶", description = "根據關鍵字搜尋客戶")
    public ApiResponse<List<Customer>> searchCustomers(@RequestParam String keyword) {
        log.info("收到搜尋客戶請求，關鍵字：{}", keyword);
        List<Customer> customers = customerService.searchCustomers(keyword);
        return ApiResponse.success("搜尋成功", customers);
    }
}