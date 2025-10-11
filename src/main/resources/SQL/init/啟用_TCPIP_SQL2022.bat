@echo off
chcp 65001 >nul
echo ========================================
echo SQL Server 2022 - 啟用 TCP/IP
echo ========================================
echo.

REM 檢查是否以系統管理員身分執行
net session >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ❌ 請以系統管理員身分執行此批次檔！
    echo.
    echo 右鍵點選此檔案 → 選擇「以系統管理員身分執行」
    pause
    exit /b 1
)

echo [步驟 1/5] 檢查 SQL Server 服務...
sc query MSSQLSERVER >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ❌ SQL Server 服務不存在
    echo 請確認已安裝 SQL Server 2022
    pause
    exit /b 1
)

sc query MSSQLSERVER | findstr "STATE" | findstr "RUNNING" >nul
if %ERRORLEVEL% EQ 0 (
    echo ✅ SQL Server 服務正在運行
) else (
    echo ⚠️  SQL Server 服務未運行，正在啟動...
    net start MSSQLSERVER
    if %ERRORLEVEL% EQ 0 (
        echo ✅ SQL Server 服務已啟動
    ) else (
        echo ❌ 啟動服務失敗
        pause
        exit /b 1
    )
)
echo.

echo [步驟 2/5] 啟用 TCP/IP 協定...
echo 正在使用 PowerShell 啟用 TCP/IP...
echo.

powershell -NoProfile -ExecutionPolicy Bypass -Command ^
"$computerName = $env:COMPUTERNAME; ^
[System.Reflection.Assembly]::LoadWithPartialName('Microsoft.SqlServer.SqlWmiManagement') | Out-Null; ^
$smo = New-Object ('Microsoft.SqlServer.Management.Smo.Wmi.ManagedComputer') $computerName; ^
$uri = \"ManagedComputer[@Name='\" + $computerName + \"']/ServerInstance[@Name='MSSQLSERVER']/ServerProtocol[@Name='Tcp']\"; ^
$tcp = $smo.GetSmoObject($uri); ^
if ($tcp.IsEnabled) { ^
    Write-Host '✅ TCP/IP 已啟用' -ForegroundColor Green; ^
} else { ^
    $tcp.IsEnabled = $true; ^
    $tcp.Alter(); ^
    Write-Host '✅ TCP/IP 已成功啟用' -ForegroundColor Green; ^
}; ^
$ipAll = $tcp.IPAddresses['IPAll']; ^
$ipAll.IPAddressProperties['TcpPort'].Value = '1433'; ^
$ipAll.IPAddressProperties['TcpDynamicPorts'].Value = ''; ^
$tcp.Alter(); ^
Write-Host '✅ TCP Port 已設定為 1433' -ForegroundColor Green"

if %ERRORLEVEL% NEQ 0 (
    echo ❌ 啟用 TCP/IP 失敗
    echo.
    echo 請手動啟用：
    echo 1. 開啟 SQL Server Configuration Manager
    echo 2. SQL Server Network Configuration → Protocols for MSSQLSERVER
    echo 3. 右鍵 TCP/IP → Enable
    echo 4. 重新啟動 SQL Server 服務
    pause
    exit /b 1
)
echo.

echo [步驟 3/5] 設定 SQL Server Browser...
sc query SQLBrowser >nul 2>&1
if %ERRORLEVEL% EQ 0 (
    sc config SQLBrowser start= auto
    net start SQLBrowser >nul 2>&1
    echo ✅ SQL Server Browser 已設定為自動啟動
) else (
    echo ⚠️  SQL Server Browser 服務不存在（可選）
)
echo.

echo [步驟 4/5] 設定防火牆規則...
netsh advfirewall firewall show rule name="SQL Server 2022" >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    netsh advfirewall firewall add rule name="SQL Server 2022" dir=in action=allow protocol=TCP localport=1433
    echo ✅ 防火牆規則已新增
) else (
    echo ✅ 防火牆規則已存在
)
echo.

echo [步驟 5/5] 重新啟動 SQL Server...
net stop MSSQLSERVER
timeout /t 3 /nobreak >nul
net start MSSQLSERVER

if %ERRORLEVEL% EQ 0 (
    echo ✅ SQL Server 已重新啟動
) else (
    echo ❌ 重新啟動失敗
    pause
    exit /b 1
)
echo.

echo ========================================
echo 驗證設定
echo ========================================
echo.

echo 檢查 1433 端口...
timeout /t 2 /nobreak >nul
netstat -an | findstr ":1433.*LISTENING"
if %ERRORLEVEL% EQ 0 (
    echo ✅ 1433 端口正在監聽
) else (
    echo ❌ 1433 端口未監聽
    echo 請稍後再檢查，或重新執行此腳本
)
echo.

echo ========================================
echo 完成！
echo ========================================
echo.
echo 現在可以重新啟動您的 Spring Boot 應用程式
echo 執行：mvnw.cmd spring-boot:run
echo.
pause