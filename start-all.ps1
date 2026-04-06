<#
.SYNOPSIS
启动企业多公司文件管理系统（Windows开发环境）

.DESCRIPTION
自动检查环境并启动前后端服务
#>

Write-Host "=======================================" -ForegroundColor Green
Write-Host "  启动企业多公司文件管理系统（Windows开发环境）" -ForegroundColor Green
Write-Host "=======================================" -ForegroundColor Green
Write-Host

# 检查 Java 是否安装
Write-Host "检查 Java 环境..." -ForegroundColor Cyan
try {
    java -version | Out-Null
    Write-Host "Java 环境检查通过" -ForegroundColor Green
} catch {
    Write-Host "错误：未找到 Java 运行环境" -ForegroundColor Red
    Write-Host "请安装 Java 17+ 版本" -ForegroundColor Red
    Read-Host "按回车键退出..."
    exit 1
}

# 检查后端 JAR 文件是否存在
$jarPath = Join-Path $PSScriptRoot "backend\target\file-manage-system-1.0.0.jar"
if (Test-Path $jarPath) {
    Write-Host "已找到后端 JAR 文件，将直接运行" -ForegroundColor Cyan
    $useJar = $true
} else {
    Write-Host "未找到后端 JAR 文件，将使用 Maven 构建" -ForegroundColor Cyan
    $useJar = $false
    # 检查 Maven 是否安装
    try {
        mvn --version | Out-Null
    } catch {
        Write-Host "错误：未找到 Maven" -ForegroundColor Red
        Write-Host "请安装 Maven 3.8+ 版本" -ForegroundColor Red
        Read-Host "按回车键退出..."
        exit 1
    }
}

# 检查前端 Node.js 是否安装
Write-Host "检查 Node.js 环境..." -ForegroundColor Cyan
try {
    npm --version | Out-Null
    Write-Host "Node.js 环境检查通过" -ForegroundColor Green
} catch {
    Write-Host "错误：未找到 Node.js" -ForegroundColor Red
    Write-Host "请安装 Node.js 18+ 版本" -ForegroundColor Red
    Read-Host "按回车键退出..."
    exit 1
}

Write-Host

# 创建数据目录（如果不存在）
$dataDir = Join-Path $PSScriptRoot "data"
if (-not (Test-Path $dataDir)) {
    Write-Host "创建数据目录..." -ForegroundColor Cyan
    New-Item -ItemType Directory -Path $dataDir -Force | Out-Null
}

# 启动后端服务
Write-Host "正在启动后端服务..." -ForegroundColor Cyan
$backendDir = Join-Path $PSScriptRoot "backend"
if ($useJar) {
    Start-Process -FilePath "cmd.exe" -ArgumentList "/k cd /d ""$backendDir"" && java -jar target\file-manage-system-1.0.0.jar" -WindowStyle Normal -WorkingDirectory $backendDir
} else {
    Start-Process -FilePath "cmd.exe" -ArgumentList "/k cd /d ""$backendDir"" && mvn spring-boot:run" -WindowStyle Normal -WorkingDirectory $backendDir
}

Write-Host
Write-Host "后端服务启动中..." -ForegroundColor Yellow
Start-Sleep -Seconds 8

# 启动前端服务
Write-Host "正在启动前端服务..." -ForegroundColor Cyan
$frontendDir = Join-Path $PSScriptRoot "frontend"
Start-Process -FilePath "cmd.exe" -ArgumentList "/k cd /d ""$frontendDir"" && npm install && npm run dev" -WindowStyle Normal -WorkingDirectory $frontendDir

Write-Host
Write-Host "前端服务启动中..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

Write-Host "=======================================" -ForegroundColor Green
Write-Host "  系统启动完成！" -ForegroundColor Green
Write-Host "=======================================" -ForegroundColor Green
Write-Host "后端地址: http://localhost:8080" -ForegroundColor Cyan
Write-Host "前端地址: http://localhost:5173" -ForegroundColor Cyan
Write-Host "默认管理员: admin / admin123" -ForegroundColor Cyan
Write-Host "=======================================" -ForegroundColor Green
Write-Host
Write-Host "注意事项：" -ForegroundColor Yellow
Write-Host "1. 数据存储在 data 目录中"
Write-Host "2. 后端服务使用 application.yml 配置"
Write-Host "3. 如需停止服务，请关闭相应的命令窗口"
Write-Host
Read-Host "按回车键退出..."