package com.bank.apiplatform.dto.request;

import lombok.Data;

import java.util.Map;

@Data
public class LegacyDynamicQueryRequest {
    private String queryCode;
    private Map<String, Object> params;
    private Integer pageNum;
    private Integer pageSize;
}


