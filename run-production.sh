#!/bin/bash
echo "======================================="
echo " 运行企业多公司文件管理系统（Mac生产环境）"
echo "======================================="
echo ""
echo "注意：此脚本假设前后端已在开发环境构建完毕"
echo "======================================="
echo ""

# 检查 Java 是否安装
echo "检查 Java 环境..."
if ! command -v java &> /dev/null; then
    echo "错误：未找到 Java 运行环境"
    echo "请安装 Java 17+ 版本"
    read -p "按回车键退出..."
    exit 1
fi
echo "Java 环境检查通过"

# 检查后端 JAR 文件是否存在
BACKEND_JAR="$(dirname "$0")/backend/target/file-manage-system-1.0.0.jar"
if [ -f "$BACKEND_JAR" ]; then
    echo "已找到后端 JAR 文件: $BACKEND_JAR"
else
    echo "错误：未找到后端 JAR 文件"
    echo "请确认后端已在开发环境构建完毕"
    echo "预期位置: $BACKEND_JAR"
    read -p "按回车键退出..."
    exit 1
fi

# 检查前端构建文件是否存在
FRONTEND_DIST="$(dirname "$0")/frontend/dist"
if [ -d "$FRONTEND_DIST" ]; then
    echo "已找到前端构建文件: $FRONTEND_DIST"
else
    echo "错误：未找到前端构建文件"
    echo "请确认前端已在开发环境构建完毕"
    echo "预期位置: $FRONTEND_DIST"
    read -p "按回车键退出..."
    exit 1
fi

# 检查 Node.js 是否安装（用于启动前端静态服务）
echo "检查 Node.js 环境..."
if ! command -v npm &> /dev/null; then
    echo "错误：未找到 Node.js"
    echo "请安装 Node.js 18+ 版本"
    read -p "按回车键退出..."
    exit 1
fi
echo "Node.js 环境检查通过"
echo ""

# 检查数据目录是否存在，不存在则创建
data_dir="$(dirname "$0")/data"
if [ ! -d "$data_dir" ]; then
    echo "创建数据目录: $data_dir"
    mkdir -p "$data_dir"
    chmod 755 "$data_dir"
fi

# 启动后端服务
echo "正在启动后端服务..."
osascript -e 'tell application "Terminal" to do script "cd \"'$(dirname "$0")'\" && cd backend && java -jar target/file-manage-system-1.0.0.jar --spring.profiles.active=mac,prod"'
echo ""
echo "后端服务启动中..."
sleep 8

# 检查后端服务是否启动成功
echo "检查后端服务状态..."
if curl -s http://localhost:8080 > /dev/null; then
    echo "后端服务启动成功！"
else
    echo "警告：后端服务可能启动失败，请检查终端输出"
fi
echo ""

# 启动前端服务
echo "正在启动前端服务..."

# 检查是否安装了 serve 包
if ! npm list -g serve &> /dev/null; then
    echo "安装静态文件服务器..."
    npm install -g serve
fi

osascript -e 'tell application "Terminal" to do script "cd \"'$(dirname "$0")'\" && cd frontend && serve -s dist -l 5173"'
echo ""
echo "前端服务启动中..."
sleep 5

# 检查前端服务是否启动成功
echo "检查前端服务状态..."
if curl -s http://localhost:5173 > /dev/null; then
    echo "前端服务启动成功！"
else
    echo "警告：前端服务可能启动失败，请检查终端输出"
fi
echo ""
echo "======================================="
echo " 系统启动完成！"
echo "======================================="
echo "后端地址: http://localhost:8080"
echo "前端地址: http://localhost:5173"
echo "默认管理员: admin / admin123"
echo "======================================="
echo ""
echo "注意事项："
echo "1. 数据存储在 data 目录中，请定期备份"
echo "2. 如需停止服务，请关闭相应的终端窗口"
echo "3. 生产环境已关闭调试功能，提高安全性"
echo ""
echo "按回车键退出..."
read