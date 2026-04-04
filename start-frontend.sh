#!/bin/bash
echo "========================================"
echo " 启动企业多公司文件管理系统 - 前端服务"
echo "========================================"

# 检查 Node.js 是否安装
echo "检查 Node.js 环境..."
if ! command -v npm &> /dev/null; then
    echo "错误：未找到 Node.js"
    echo "请安装 Node.js 18+ 版本"
    read -p "按回车键退出..."
    exit 1
fi
echo "Node.js 环境检查通过"

cd "$(dirname "$0")/frontend"
echo ""
echo "正在安装依赖..."
npm install
echo ""
echo "正在启动前端服务..."
echo "访问地址: http://localhost:5173"
echo ""
npm run dev
