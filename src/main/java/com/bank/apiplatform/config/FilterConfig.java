package com.bank.apiplatform.config;

import com.bank.apiplatform.filter.ApiKeyAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 過濾器配置
 */
@Configuration
@RequiredArgsConstructor
public class FilterConfig {
    
    private final ApiKeyAuthFilter apiKeyAuthFilter;
    
    @Bean
    public FilterRegistrationBean<ApiKeyAuthFilter> apiKeyFilter() {
        FilterRegistrationBean<ApiKeyAuthFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(apiKeyAuthFilter);
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }
}