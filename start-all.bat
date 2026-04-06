@echo off
chcp 65001 >nul
echo ========================================
echo  启动企业多公司文件管理系统（Windows开发环境）
echo ========================================
echo.

:: 检查 Java 是否安装
echo 检查 Java 环境...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误：未找到 Java 运行环境
    echo 请安装 Java 17+ 版本
    pause
    exit /b 1
)
echo Java 环境检查通过

:: 检查后端 JAR 文件是否存在
if exist "%~dp0backend\target\file-manage-system-1.0.0.jar" (
    echo 已找到后端 JAR 文件，将直接运行
    set USE_JAR=1
) else (
    echo 未找到后端 JAR 文件，将使用 Maven 构建
    set USE_JAR=0
    :: 检查 Maven 是否安装
    mvn --version >nul 2>&1
    if %errorlevel% neq 0 (
        echo 错误：未找到 Maven
        echo 请安装 Maven 3.8+ 版本
        pause
        exit /b 1
    )
)

:: 检查前端 Node.js 是否安装
echo 检查 Node.js 环境...
npm --version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误：未找到 Node.js
    echo 请安装 Node.js 18+ 版本
    pause
    exit /b 1
)
echo Node.js 环境检查通过
echo.

:: 创建数据目录（如果不存在）
if not exist "%~dp0data" (
    echo 创建数据目录...
    mkdir "%~dp0data"
)

:: 启动后端服务
echo 正在启动后端服务...
if %USE_JAR% equ 1 (
    start "后端服务" cmd /k "cd /d "%~dp0backend" && java -jar target\file-manage-system-1.0.0.jar"
) else (
    start "后端服务" cmd /k "cd /d "%~dp0backend" && mvn spring-boot:run"
)
echo.
echo 后端服务启动中...
timeout /t 8 /nobreak >nul

:: 启动前端服务
echo 正在启动前端服务...
start "前端服务" cmd /k "cd /d "%~dp0frontend" && npm install && npm run dev"
echo.
echo 前端服务启动中...
timeout /t 5 /nobreak >nul

echo ========================================
echo  系统启动完成！
echo ========================================
echo 后端地址: http://localhost:8080
echo 前端地址: http://localhost:5173
echo 默认管理员: admin / admin123
echo ========================================
echo.
echo 注意事项：
echo 1. 数据存储在 data 目录中
echo 2. 后端服务使用 application.yml 配置
echo 3. 如需停止服务，请关闭相应的命令窗口
echo.
pause