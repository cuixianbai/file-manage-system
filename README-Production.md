# Mac生产环境运行说明

## 系统要求

在Mac生产环境中运行企业多公司文件管理系统需要以下环境：

- **Java 17+**：后端服务运行环境
- **Node.js 18+**：前端构建和运行环境
- **npm**：Node.js包管理器
- **serve**：静态文件服务器（会自动安装）

## 部署步骤

### 1. 构建项目

在部署前，需要先构建项目：

```bash
# 构建后端
cd backend
mvn clean package -DskipTests

# 构建前端
cd ../frontend
npm install
npm run build
```

### 2. 启动服务

#### 一键启动（推荐）

```bash
chmod +x start-production.sh
./start-production.sh
```

#### 分别启动

##### 启动后端服务

```bash
cd backend
java -jar target/file-manage-system-1.0.0.jar --spring.profiles.active=mac,prod
```

##### 启动前端服务

```bash
chmod +x start-frontend-production.sh
./start-frontend-production.sh
```

## 生产环境配置

### 配置文件

- **`application-mac.yml`**：Mac环境基础配置
- **`application-prod.yml`**：生产环境配置

### 生产环境特点

1. **关闭调试信息**：
   - 关闭H2数据库控制台
   - 关闭SQL语句打印
   - 降低日志级别

2. **性能优化**：
   - 使用构建后的静态文件
   - 减少不必要的日志输出
   - 优化数据库连接

3. **安全增强**：
   - 关闭开发模式功能
   - 减少敏感信息暴露

## 服务访问地址

- **后端服务**：http://localhost:8080
- **前端服务**：http://localhost:5173

## 默认账号

- **用户名**：admin
- **密码**：admin123

## 常见问题

### 1. 端口被占用

如果端口8080或5173被占用，可以修改配置文件中的端口设置。

### 2. 构建失败

如果前端构建失败，尝试：

```bash
cd frontend
npm cache clean --force
npm install
npm run build
```

### 3. 数据库连接问题

确保`data`目录存在且有写入权限：

```bash
mkdir -p data
chmod 755 data
```

### 4. 静态文件服务器问题

如果`serve`命令失败，尝试：

```bash
npm install -g serve
```

## 项目结构

```
file-manage-system/
├── backend/           # 后端代码
│   ├── target/        # 构建产物
│   └── src/main/resources/  # 配置文件
├── frontend/          # 前端代码
│   └── dist/          # 构建产物
├── data/              # 数据库文件
├── start-production.sh       # 生产环境一键启动脚本
├── start-frontend-production.sh  # 前端生产环境启动脚本
└── README-Mac.md      # Mac环境说明
```

## 技术栈

- **后端**：Spring Boot 3.2, Java 17, H2 Database
- **前端**：Vue 3, Vite, Element Plus
- **构建工具**：Maven, npm
- **静态文件服务**：serve

## 注意事项

1. **首次运行**：首次运行时会自动创建数据库表结构和默认数据

2. **数据持久化**：数据会持久化存储在`data`目录中，定期备份此目录

3. **安全配置**：
   - 生产环境已关闭H2控制台
   - 已降低日志级别，减少敏感信息暴露

4. **性能监控**：
   - 生产环境模式下，系统会自动优化性能
   - 建议定期检查系统资源使用情况

5. **更新部署**：
   - 更新代码后需要重新构建项目
   - 重新启动服务以应用新的更改

## 故障排查

### 后端服务问题

1. 检查Java版本：`java -version`
2. 检查端口是否被占用：`lsof -i :8080`
3. 查看日志输出，定位错误信息

### 前端服务问题

1. 检查Node.js版本：`node -v`
2. 检查端口是否被占用：`lsof -i :5173`
3. 确认前端构建是否成功：检查`frontend/dist`目录

---

**祝使用愉快！**