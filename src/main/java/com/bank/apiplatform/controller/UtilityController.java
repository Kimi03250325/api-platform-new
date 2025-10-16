package com.bank.apiplatform.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class UtilityController {

    @GetMapping({"/tools/query-tool", "/tools/query-tool/"})
    public String forwardQueryTool() {
        return "redirect:/tools/query-tool/index.html";
    }
    
    @GetMapping("/tools/query-tool/index.html")
    @ResponseBody
    public String getQueryTool() {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>動態查詢測試工具</title></head><body><h1>動態查詢測試工具</h1><p>請直接使用 API 端點：</p><ul><li>POST /api/query/execute/LIST_CUSTOMERS</li><li>POST /api/dynamic/query</li></ul><p>或使用 Swagger UI：<a href='/swagger-ui/index.html'>/swagger-ui/index.html</a></p></body></html>";
    }
}


