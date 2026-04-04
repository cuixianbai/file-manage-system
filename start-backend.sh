#!/bin/bash
echo "========================================"
echo " 启动企业多公司文件管理系统 - 后端服务"
echo "========================================"

# 检查 Java 是否安装
echo "检查 Java 环境..."
if ! command -v java &> /dev/null; then
    echo "错误：未找到 Java 运行环境"
    echo "请安装 Java 17+ 版本"
    read -p "按回车键退出..."
    exit 1
fi
echo "Java 环境检查通过"

cd "$(dirname "$0")/backend"
echo ""
echo "正在编译并启动后端服务..."
echo "默认管理员账号: admin / admin123"
echo ""
mvn spring-boot:run
