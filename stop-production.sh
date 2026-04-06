#!/bin/bash
echo "======================================="
echo " 停止企业多公司文件管理系统（Mac生产环境）"
echo "======================================="
echo ""

# 停止后端 Java 进程
echo "停止后端服务..."
backend_pid=$(pgrep -f "file-manage-system-1.0.0.jar")
if [ ! -z "$backend_pid" ]; then
    kill -9 $backend_pid
    echo "后端服务已停止 (PID: $backend_pid)"
else
    echo "后端服务未运行或已停止"
fi

# 停止前端 Node.js 进程
echo "停止前端服务..."
frontend_pid=$(pgrep -f "serve -s dist")
if [ ! -z "$frontend_pid" ]; then
    kill -9 $frontend_pid
    echo "前端服务已停止 (PID: $frontend_pid)"
else
    echo "前端服务未运行或已停止"
fi

echo ""
echo "======================================="
echo " 系统已停止！"
echo "======================================="
echo "所有服务进程已关闭"
echo ""
echo "按回车键退出..."
read