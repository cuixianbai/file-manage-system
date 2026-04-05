#!/bin/bash
echo "======================================="
echo " 启动企业多公司文件管理系统（生产环境）"
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
if [ -f "$(dirname "$0")/backend/target/file-manage-system-1.0.0.jar" ]; then
    echo "已找到后端 JAR 文件，将直接运行"
    USE_JAR=1
else
    echo "错误：未找到后端 JAR 文件"
    echo "请先构建后端项目：mvn clean package -DskipTests"
    read -p "按回车键退出..."
    exit 1
fi

# 检查前端 Node.js 是否安装
echo "检查 Node.js 环境..."
if ! command -v npm &> /dev/null; then
    echo "错误：未找到 Node.js"
    echo "请安装 Node.js 18+ 版本"
    read -p "按回车键退出..."
    exit 1
fi
echo "Node.js 环境检查通过"
echo ""

# 启动后端服务
echo "正在启动后端服务（生产模式）..."
osascript -e 'tell application "Terminal" to do script "cd \"'$(dirname "$0")'\" && cd backend && java -jar target/file-manage-system-1.0.0.jar --spring.profiles.active=mac --spring.profiles.active=prod"'
echo ""
sleep 5

# 启动前端服务
echo "正在启动前端服务（生产模式）..."
osascript -e 'tell application "Terminal" to do script "cd \"'$(dirname "$0")'\" && ./start-frontend-production.sh"'
echo ""
echo "======================================="
echo " 系统启动完成！"
echo "======================================="
echo "后端地址: http://localhost:8080"
echo "前端地址: http://localhost:5173"
echo "默认管理员: admin / admin123"
echo "======================================="
echo ""
echo "注意：这是生产环境启动模式，已关闭调试信息"
echo "如需开发环境，请使用 start-all.sh 脚本"
