package com.bank.apiplatform.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 工具頁面控制器
 */
@Controller
public class UtilityController {

    /**
     * 查詢工具首頁 - 重定向到 Swagger UI
     */
    @GetMapping({"/tools/query-tool", "/tools/query-tool/"})
    public String redirectToSwagger() {
        return "redirect:/swagger-ui/index.html";
    }
    
    /**
     * 查詢工具 HTML 頁面
     */
    @GetMapping("/tools/query-tool/index.html")
    @ResponseBody
    public String getQueryToolPage() {
        return buildQueryToolHtml();
    }
    
    /**
     * 構建查詢工具 HTML
     */
    private String buildQueryToolHtml() {
        return """
<!DOCTYPE html>
<html lang="zh-TW">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>動態查詢測試工具 - 銀行 API 平台</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: 'Microsoft JhengHei', 'Segoe UI', Arial, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 20px;
        }
        
        .container {
            max-width: 1200px;
            margin: 0 auto;
            background: white;
            border-radius: 15px;
            box-shadow: 0 20px 60px rgba(0,0,0,0.3);
            overflow: hidden;
        }
        
        .header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 30px;
            text-align: center;
        }
        
        .header h1 {
            font-size: 2.5em;
            margin-bottom: 10px;
        }
        
        .header p {
            font-size: 1.1em;
            opacity: 0.9;
        }
        
        .content {
            padding: 40px;
        }
        
        .card {
            background: #f8f9fa;
            border-radius: 10px;
            padding: 25px;
            margin-bottom: 25px;
            border-left: 4px solid #667eea;
        }
        
        .card h2 {
            color: #333;
            margin-bottom: 15px;
            font-size: 1.5em;
        }
        
        .card h3 {
            color: #555;
            margin: 20px 0 10px 0;
            font-size: 1.2em;
        }
        
        .endpoint {
            background: white;
            padding: 15px;
            border-radius: 8px;
            margin: 10px 0;
            border: 1px solid #ddd;
        }
        
        .endpoint code {
            background: #2d3748;
            color: #68d391;
            padding: 3px 8px;
            border-radius: 4px;
            font-size: 0.9em;
        }
        
        .method {
            display: inline-block;
            padding: 4px 12px;
            border-radius: 4px;
            font-weight: bold;
            font-size: 0.85em;
            margin-right: 10px;
        }
        
        .method.get {
            background: #48bb78;
            color: white;
        }
        
        .method.post {
            background: #4299e1;
            color: white;
        }
        
        .button {
            display: inline-block;
            padding: 12px 30px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            text-decoration: none;
            border-radius: 8px;
            font-weight: bold;
            margin: 10px 10px 10px 0;
            transition: transform 0.2s, box-shadow 0.2s;
        }
        
        .button:hover {
            transform: translateY(-2px);
            box-shadow: 0 10px 20px rgba(0,0,0,0.2);
        }
        
        .button.secondary {
            background: #48bb78;
        }
        
        .info-box {
            background: #edf2f7;
            padding: 15px;
            border-radius: 8px;
            margin: 15px 0;
            border-left: 4px solid #4299e1;
        }
        
        .warning-box {
            background: #fffaf0;
            padding: 15px;
            border-radius: 8px;
            margin: 15px 0;
            border-left: 4px solid #ed8936;
        }
        
        pre {
            background: #2d3748;
            color: #68d391;
            padding: 15px;
            border-radius: 8px;
            overflow-x: auto;
            font-size: 0.9em;
        }
        
        .footer {
            text-align: center;
            padding: 20px;
            background: #f8f9fa;
            color: #666;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🏦 動態查詢測試工具</h1>
            <p>銀行 API 平台 - 配置化通用查詢端點</p>
        </div>
        
        <div class="content">
            <!-- 快速開始 -->
            <div class="card">
                <h2>🚀 快速開始</h2>
                <p>使用以下工具測試和瀏覽 API：</p>
                <div style="margin-top: 20px;">
                    <a href="/swagger-ui/index.html" class="button">
                        📖 Swagger UI（推薦）
                    </a>
                    <a href="/api/query/available" class="button secondary">
                        📋 查看可用查詢
                    </a>
                </div>
            </div>
            
            <!-- API 端點說明 -->
            <div class="card">
                <h2>📡 主要 API 端點</h2>
                
                <h3>1. 查詢管理</h3>
                <div class="endpoint">
                    <span class="method get">GET</span>
                    <code>/api/query/available</code>
                    <p>查詢所有可用的查詢代碼</p>
                </div>
                
                <div class="endpoint">
                    <span class="method get">GET</span>
                    <code>/api/query/config/{queryCode}</code>
                    <p>查看查詢配置詳情（參數定義、範例等）</p>
                </div>
                
                <h3>2. 通用查詢端點</h3>
                <div class="endpoint">
                    <span class="method get">GET</span>
                    <code>/api/query/execute/{queryCode}</code>
                    <p>執行動態查詢（GET 方式，參數在 URL）</p>
                </div>
                
                <div class="endpoint">
                    <span class="method post">POST</span>
                    <code>/api/query/execute/{queryCode}</code>
                    <p>執行動態查詢（POST 方式，參數在 Body）</p>
                </div>
                
                <h3>3. 專用查詢端點</h3>
                <div class="endpoint">
                    <span class="method get">GET</span>
                    <code>/api/query/execute-customer-detail</code>
                    <p>查詢客戶詳情（需要 customer_id 參數）</p>
                </div>
                
                <div class="endpoint">
                    <span class="method get">GET</span>
                    <code>/api/query/execute-customers</code>
                    <p>查詢客戶列表（可選 status 參數）</p>
                </div>
                
                <div class="endpoint">
                    <span class="method get">GET</span>
                    <code>/api/query/execute-search-customers</code>
                    <p>搜尋客戶（可選 name、phone、id_number 參數）</p>
                </div>
            </div>
            
            <!-- 使用範例 -->
            <div class="card">
                <h2>💡 使用範例</h2>
                
                <h3>範例 1: 查詢客戶詳情</h3>
<pre>
curl -X GET "http://localhost:8080/api/query/execute-customer-detail?customer_id=1" \\
  -H "X-API-Key: api_live_1234567890abcdef1234567890abcdef"
</pre>
                
                <h3>範例 2: 搜尋客戶（姓名模糊搜尋）</h3>
<pre>
curl -X GET "http://localhost:8080/api/query/execute-search-customers?name=張&pageNum=1&pageSize=10" \\
  -H "X-API-Key: api_live_1234567890abcdef1234567890abcdef"
</pre>
                
                <h3>範例 3: POST 方式查詢</h3>
<pre>
curl -X POST "http://localhost:8080/api/query/execute/LIST_CUSTOMERS" \\
  -H "X-API-Key: api_live_1234567890abcdef1234567890abcdef" \\
  -H "Content-Type: application/json" \\
  -d '{
    "params": {
      "status": "ACTIVE"
    },
    "pageNum": 1,
    "pageSize": 20
  }'
</pre>
            </div>
            
            <!-- API Key 說明 -->
            <div class="card">
                <h2>🔑 API Key 說明</h2>
                
                <div class="info-box">
                    <strong>所有 API 請求都需要在 Header 中包含 API Key：</strong>
                    <pre style="margin-top: 10px;">X-API-Key: your_api_key_here</pre>
                </div>
                
                <div class="warning-box">
                    <strong>⚠️ 測試用 API Key：</strong>
                    <pre style="margin-top: 10px;">api_live_1234567890abcdef1234567890abcdef</pre>
                    <p style="margin-top: 10px;">注意：生產環境請使用正式的 API Key</p>
                </div>
            </div>
            
            <!-- 可用查詢代碼 -->
            <div class="card">
                <h2>📋 常用查詢代碼</h2>
                <table style="width: 100%; border-collapse: collapse;">
                    <thead>
                        <tr style="background: #edf2f7;">
                            <th style="padding: 12px; text-align: left; border-bottom: 2px solid #ddd;">查詢代碼</th>
                            <th style="padding: 12px; text-align: left; border-bottom: 2px solid #ddd;">說明</th>
                            <th style="padding: 12px; text-align: left; border-bottom: 2px solid #ddd;">必要參數</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td style="padding: 12px; border-bottom: 1px solid #ddd;"><code>LIST_CUSTOMERS</code></td>
                            <td style="padding: 12px; border-bottom: 1px solid #ddd;">查詢客戶列表</td>
                            <td style="padding: 12px; border-bottom: 1px solid #ddd;">無（status 選填）</td>
                        </tr>
                        <tr>
                            <td style="padding: 12px; border-bottom: 1px solid #ddd;"><code>GET_CUSTOMER_DETAIL</code></td>
                            <td style="padding: 12px; border-bottom: 1px solid #ddd;">查詢客戶詳情</td>
                            <td style="padding: 12px; border-bottom: 1px solid #ddd;">customer_id</td>
                        </tr>
                        <tr>
                            <td style="padding: 12px; border-bottom: 1px solid #ddd;"><code>SEARCH_CUSTOMERS</code></td>
                            <td style="padding: 12px; border-bottom: 1px solid #ddd;">搜尋客戶</td>
                            <td style="padding: 12px; border-bottom: 1px solid #ddd;">無（name/phone/id_number 選填）</td>
                        </tr>
                        <tr>
                            <td style="padding: 12px; border-bottom: 1px solid #ddd;"><code>LIST_PORTFOLIOS</code></td>
                            <td style="padding: 12px; border-bottom: 1px solid #ddd;">查詢投資組合</td>
                            <td style="padding: 12px; border-bottom: 1px solid #ddd;">無（customer_id 選填）</td>
                        </tr>
                        <tr>
                            <td style="padding: 12px;"><code>LIST_TRANSACTIONS</code></td>
                            <td style="padding: 12px;">查詢交易記錄</td>
                            <td style="padding: 12px;">無（customer_id/start_date/end_date 選填）</td>
                        </tr>
                    </tbody>
                </table>
                
                <div style="margin-top: 20px;">
                    <a href="/api/query/available" class="button secondary">
                        查看完整查詢列表
                    </a>
                </div>
            </div>
            
            <!-- 文檔連結 -->
            <div class="card">
                <h2>📚 相關文檔</h2>
                <ul style="line-height: 2;">
                    <li><a href="/swagger-ui/index.html" style="color: #667eea;">📖 Swagger UI - 完整 API 文檔</a></li>
                    <li><a href="/api/query/available" style="color: #667eea;">📋 可用查詢列表（JSON）</a></li>
                    <li><a href="/actuator/health" style="color: #667eea;">💚 系統健康狀態</a></li>
                </ul>
            </div>
        </div>
        
        <div class="footer">
            <p>© 2025 銀行 API 平台 | Version 1.0.0</p>
            <p style="margin-top: 5px; font-size: 0.9em;">
                使用問題請聯繫技術支援團隊
            </p>
        </div>
    </div>
</body>
</html>
                """;
    }
}