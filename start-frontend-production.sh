#!/bin/bash
echo "======================================="
echo " 启动企业多公司文件管理系统 - 前端服务（生产环境）"
echo "======================================="

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

# 检查是否已构建
if [ ! -d "dist" ]; then
    echo "未找到构建文件，正在构建前端..."
    npm install
    npm run build
    if [ $? -ne 0 ]; then
        echo "构建失败，请检查错误信息"
        read -p "按回车键退出..."
        exit 1
    fi
    echo "前端构建完成"
else
    echo "已找到构建文件，跳过构建步骤"
fi

echo ""
echo "正在启动前端静态文件服务器..."
echo "访问地址: http://localhost:5173"
echo ""

# 检查是否安装了 serve 包
if ! npm list -g serve &> /dev/null; then
    echo "未找到 serve 包，正在全局安装..."
    npm install -g serve
fi

# 使用 serve 启动静态文件服务器
serve -s dist -l 5173