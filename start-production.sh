#!/bin/bash
echo "======================================="
echo " 启动企业多公司文件管理系统（Mac生产环境）"
echo "======================================="
echo ""

# 启动后端服务
echo "启动后端服务..."
cd "$(dirname "$0")/backend"
java -jar target/file-manage-system-1.0.0.jar --spring.profiles.active=mac &

# 等待后端服务启动
echo "后端服务启动中..."
sleep 8

# 启动前端服务
echo "启动前端服务..."
cd "$(dirname "$0")/frontend"
npm exec -- serve -s dist -l 5173 &

echo ""
echo "======================================="
echo " 服务启动完成！"
echo "======================================="
echo "后端地址: http://localhost:8080"
echo "前端地址: http://localhost:5173"
echo "默认管理员: admin / admin123"
echo "======================================="
echo ""
echo "如需停止服务，请使用 Ctrl+C 或关闭终端窗口"