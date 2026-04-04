#!/bin/bash
echo "========================================"
echo " 启动企业多公司文件管理系统"
echo "========================================"
echo ""
echo "正在启动后端服务..."
osascript -e 'tell application "Terminal" to do script "cd \"'$(pwd)'\" && ./start-backend.sh"'
echo ""
sleep 5
echo "正在启动前端服务..."
osascript -e 'tell application "Terminal" to do script "cd \"'$(pwd)'\" && ./start-frontend.sh"'
echo ""
echo "========================================"
echo " 系统启动完成！"
echo "========================================"
echo "后端地址: http://localhost:8080"
echo "前端地址: http://localhost:5173"
echo "默认管理员: admin / admin123"
echo "========================================"
