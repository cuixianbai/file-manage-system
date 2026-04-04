#!/bin/bash
echo "========================================"
echo " 启动企业多公司文件管理系统 - 前端服务"
echo "========================================"
cd "$(dirname "$0")/frontend"
echo ""
echo "正在安装依赖..."
npm install
echo ""
echo "正在启动前端服务..."
echo "访问地址: http://localhost:5173"
echo ""
npm run dev
