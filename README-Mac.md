# Mac环境运行说明

## 系统要求

在Mac上运行企业多公司文件管理系统需要以下环境：

- **Java 17+**：后端服务运行环境
- **Maven 3.8+**：后端构建工具（可选，如果已有JAR文件）
- **Node.js 18+**：前端运行环境
- **npm**：Node.js包管理器

## 快速启动

### 方法一：一键启动（推荐）

1. 打开终端
2. 进入项目根目录
3. 运行启动脚本：

```bash
chmod +x start-all.sh
./start-all.sh
```

### 方法二：分别启动

#### 启动后端服务

1. 打开终端
2. 进入项目根目录
3. 运行后端启动脚本：

```bash
chmod +x start-backend.sh
./start-backend.sh
```

#### 启动前端服务

1. 打开另一个终端
2. 进入项目根目录
3. 运行前端启动脚本：

```bash
chmod +x start-frontend.sh
./start-frontend.sh
```

## 配置说明

### Mac环境配置

系统会自动使用`application-mac.yml`配置文件，主要特点：

- **路径分隔符**：使用Mac标准的正斜杠`/`
- **数据库路径**：使用相对路径`../data/filemanage_db`
- **文件存储路径**：使用`storage/dirA`和`storage/dirB`

### 服务访问地址

- **后端服务**：http://localhost:8080
- **前端服务**：http://localhost:5173
- **H2数据库控制台**：http://localhost:8080/h2-console

### 默认账号

- **用户名**：admin
- **密码**：admin123

## 常见问题

### 1. 端口被占用

如果端口8080或5173被占用，可以修改配置文件中的端口设置。

### 2. 依赖安装失败

如果前端依赖安装失败，尝试：

```bash
cd frontend
npm cache clean --force
npm install
```

### 3. 数据库连接问题

确保`data`目录存在且有写入权限：

```bash
mkdir -p data
chmod 755 data
```

## 项目结构

```
file-manage-system/
├── backend/           # 后端代码
├── frontend/          # 前端代码
├── data/              # 数据库文件
├── start-all.sh       # 一键启动脚本
├── start-backend.sh   # 后端启动脚本
├── start-frontend.sh  # 前端启动脚本
└── README.md          # 项目说明
```

## 技术栈

- **后端**：Spring Boot 3.2, Java 17, H2 Database
- **前端**：Vue 3, Vite, Element Plus
- **构建工具**：Maven, npm

## 注意事项

1. 首次运行时会自动创建数据库表结构和默认数据
2. 数据会持久化存储在`data`目录中
3. 确保网络连接正常，特别是邮件发送功能需要网络
4. 开发环境下H2控制台已启用，生产环境建议关闭

---

**祝使用愉快！**