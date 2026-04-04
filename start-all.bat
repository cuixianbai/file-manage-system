@echo off
chcp 65001 >nul
echo ========================================
echo  启动企业多公司文件管理系统
echo ========================================
echo.
echo 正在启动后端服务...
start "后端服务" cmd /k "cd /d "%~dp0backend" && mvn spring-boot:run"
echo.
timeout /t 10 /nobreak >nul
echo 正在启动前端服务...
start "前端服务" cmd /k "cd /d "%~dp0frontend" && npm install && npm run dev"
echo.
echo ========================================
echo  系统启动完成！
echo ========================================
echo 后端地址: http://localhost:8080
echo 前端地址: http://localhost:5173
echo 默认管理员: admin / admin123
echo ========================================
pause
