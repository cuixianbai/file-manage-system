@echo off
chcp 65001 >nul
echo ========================================
echo  启动企业多公司文件管理系统 - 前端服务
echo ========================================
cd /d "%~dp0frontend"
echo.
echo 正在安装依赖...
call npm install
echo.
echo 正在启动前端服务...
echo 访问地址: http://localhost:5173
echo.
npm run dev
pause
