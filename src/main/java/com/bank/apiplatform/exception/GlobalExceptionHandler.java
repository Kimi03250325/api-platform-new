package com.bank.apiplatform.exception;

import com.bank.apiplatform.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全域例外處理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    /**
     * 處理資料驗證例外
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        log.warn("資料驗證失敗：{}", errors);
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(400, "資料驗證失敗"));
    }
    
    /**
     * 處理自訂例外
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException ex) {
        log.error("執行時期例外：", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ex.getMessage()));
    }
    
    /**
     * 處理所有其他例外
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleException(Exception ex) {
        log.error("系統例外：", ex);
        
        // 開發環境：顯示詳細錯誤訊息
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("exception", ex.getClass().getName());
        errorDetails.put("message", ex.getMessage());
        errorDetails.put("cause", ex.getCause() != null ? ex.getCause().toString() : null);
        
        ApiResponse<Map<String, Object>> response = new ApiResponse<>();
        response.setCode(500);
        response.setMessage("系統錯誤：" + ex.getMessage());
        response.setData(errorDetails);
        response.setTimestamp(java.time.LocalDateTime.now());
        
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}