package com.bank.apiplatform.dto.request;

import lombok.Data;

import java.util.Map;

@Data
public class DynamicQueryRequest {

    private Map<String, Object> params;

    private Integer pageNum;

    private Integer pageSize;
}


