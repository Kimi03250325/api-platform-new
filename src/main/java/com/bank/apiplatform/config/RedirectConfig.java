package com.bank.apiplatform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class RedirectConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 讓 /tools/query-tool 自動導向至 index.html
        registry.addRedirectViewController("/tools/query-tool", "/tools/query-tool/index.html");
        registry.addRedirectViewController("/tools/query-tool/", "/tools/query-tool/index.html");
    }
}


