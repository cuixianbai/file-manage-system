#!/bin/bash
echo "========================================"
echo " 启动企业多公司文件管理系统 - 后端服务"
echo "========================================"
cd "$(dirname "$0")/backend"
echo ""
echo "正在编译并启动后端服务..."
echo "默认管理员账号: admin / admin123"
echo ""
mvn spring-boot:run
