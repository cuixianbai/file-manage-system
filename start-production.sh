#!/bin/bash
echo "======================================="
echo " 启动企业多公司文件管理系统（Mac生产环境）"
echo "======================================="
echo ""

# 检查前端目录
echo "检查前端构建目录..."
if [ ! -d "$(dirname "$0")/frontend/dist" ]; then
    echo "错误：前端构建目录不存在！"
    echo "请先构建前端项目：npm run build"
    exit 1
fi

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

# 等待后端服务启动
echo "后端服务启动中..."
sleep 8

# 启动前端服务
echo "启动前端服务..."
cd "$(dirname "$0")/frontend"

# 尝试不同的serve启动方式
echo "尝试启动前端服务..."
if command -v serve &> /dev/null; then
    echo "使用全局serve命令..."
    serve -s dist -l 5173 &
elif command -v npx &> /dev/null; then
    echo "使用npx serve..."
    npx serve@latest -s dist -l 5173 &
elif command -v npm &> /dev/null; then
    echo "使用npm exec serve..."
    npm exec -- serve -s dist -l 5173 &
else
    echo "错误：无法启动前端服务，未找到serve或npm"
    echo "请手动安装serve：npm install -g serve"
    exit 1
fi

echo ""
echo "======================================="
echo " 服务启动完成！"
echo "======================================="
echo "后端地址: http://localhost:8080"
echo "前端地址: http://localhost:5173"
echo "默认管理员: admin / admin123"
echo "======================================="
echo ""
echo "如需停止服务，请使用 Ctrl+C 或关闭终端窗口"