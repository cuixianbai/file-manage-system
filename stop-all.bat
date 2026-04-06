@echo off
chcp 65001 >nul
echo ========================================
echo  停止企业多公司文件管理系统
 echo ========================================
echo.

:: 停止 Java 进程（后端服务）
echo 停止后端服务...
taskkill /F /IM java.exe 2>nul
if %errorlevel% equ 0 (
    echo 后端服务已停止
) else (
    echo 后端服务未运行或已停止
)

:: 停止 Node.js 进程（前端服务）
echo 停止前端服务...
taskkill /F /IM node.exe 2>nul
if %errorlevel% equ 0 (
    echo 前端服务已停止
) else (
    echo 前端服务未运行或已停止
)
echo.
echo ========================================
echo  系统已停止！
echo ========================================
echo 所有服务进程已关闭
echo.
pause