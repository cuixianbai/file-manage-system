#!/bin/bash
echo "======================================="
echo " 启动企业多公司文件管理系统（后端服务）"
echo "======================================="
echo ""

# 检查后端JAR文件
echo "检查后端服务..."
if [ ! -f "$(dirname "$0")/backend/target/file-manage-system-1.0.0.jar" ]; then
    echo "错误：后端JAR文件不存在！"
    echo "请先构建后端项目：mvn clean package -DskipTests"
    exit 1
fi

# 启动后端服务
echo "启动后端服务..."
cd "$(dirname "$0")/backend"
java -jar target/file-manage-system-1.0.0.jar --spring.profiles.active=mac &
BACKEND_PID=$!
echo "后端服务已启动，PID: $BACKEND_PID"

# 等待后端服务启动
echo "后端服务启动中..."
sleep 8

echo ""
echo "======================================="
echo " 后端服务启动完成！"
echo "======================================="
echo "后端地址: http://localhost:8080"
echo "Swagger API: http://localhost:8080/swagger-ui.html"
echo "======================================="
echo ""
echo "如需停止服务，请使用 Ctrl+C 或关闭终端窗口"