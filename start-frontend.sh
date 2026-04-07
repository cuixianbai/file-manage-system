#!/bin/bash
echo "======================================="
echo " 启动企业多公司文件管理系统（前端服务）"
echo "======================================="
echo ""

# 检查前端目录
echo "检查前端构建目录..."
if [ ! -d "$(dirname "$0")/frontend/dist" ]; then
    echo "错误：前端构建目录不存在！"
    echo "请先构建前端项目：npm run build"
    exit 1
fi

# 启动前端服务
echo "启动前端服务..."
cd "$(dirname "$0")/frontend"
# 创建logs目录
mkdir -p logs
# 启动前端服务并将日志输出到文件
npm exec -- serve -s dist -l 5173 > logs/frontend.log 2>&1 &
FRONTEND_PID=$!
echo "前端服务已启动，PID: $FRONTEND_PID"
echo "日志输出到: logs/frontend.log"

echo ""
echo "======================================="
echo " 前端服务启动完成！"
echo "======================================="
echo "前端地址: http://localhost:5173"
echo "======================================="
echo ""
echo "如需停止服务，请使用 Ctrl+C 或关闭终端窗口"