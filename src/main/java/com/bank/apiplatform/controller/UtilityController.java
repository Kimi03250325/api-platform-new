package com.bank.apiplatform.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UtilityController {

    @GetMapping({"/tools/query-tool", "/tools/query-tool/", "/tools/query-tool/**"})
    public String forwardQueryTool() {
        return "forward:/tools/query-tool/index.html";
    }
}


