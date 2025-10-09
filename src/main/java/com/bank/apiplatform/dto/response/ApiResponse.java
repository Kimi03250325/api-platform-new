package com.bank.apiplatform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 統一 API 回應格式
 * 
 * @param <T> 資料類型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    
    /**
     * 狀態碼
     */
    private int code;
    
    /**
     * 訊息
     */
    private String message;
    
    /**
     * 資料
     */
    private T data;
    
    /**
     * 時間戳
     */
    private LocalDateTime timestamp;
    
    /**
     * 成功回應
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "成功", data, LocalDateTime.now());
    }
    
    /**
     * 成功回應（自訂訊息）
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data, LocalDateTime.now());
    }
    
    /**
     * 失敗回應
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null, LocalDateTime.now());
    }
    
    /**
     * 失敗回應（預設 500）
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(500, message, null, LocalDateTime.now());
    }
}