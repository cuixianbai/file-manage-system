# 企业多公司文件管理系统

## 项目概述

企业多公司文件管理系统是一个基于 SpringBoot 3.x + Vue3 + Element Plus 的文件管理平台，支持多公司文件隔离、用户权限管理、邮件通知等功能。

## 技术栈

### 后端
- SpringBoot 3.x
- Spring Security + JWT
- Spring Data JPA
- H2 Database (文件型，无需安装)
- Spring Mail (QQ邮箱SMTP)
- Lombok

### 前端
- Vue3
- Element Plus
- Pinia (状态管理)
- Vue Router
- Axios

## 系统角色

- **超级管理员 (ADMIN)**: 最高权限，可管理所有公司、用户、文件，可重置密码、修改状态，可选择是否发送邮件通知
- **普通用户 (USER)**: 注册后默认禁用，需超管启用后可登录，仅管理本公司文件
- **禁用用户**: 无法登录系统

## 功能特性

### 基础功能
- ✅ 账号密码登录
- ✅ 禁用用户无法登录
- ✅ 公司注册
- ✅ 用户注册（默认禁用状态）

### 核心业务功能
- ✅ 用户-公司关系管理
- ✅ 文件上传（支持点击/拖拽）
- ✅ 文件重命名规则：公司名_原始文件名
- ✅ 上传记录管理
- ✅ 目录A/B 查看与下载

### 超管高级功能
- ✅ 用户管理（启用/禁用、重置密码）
- ✅ 邮件通知（可选）
- ✅ 公司管理
- ✅ 全权限文件管理

## 快速开始

### 环境要求
- Java 17+
- Node.js 18+
- Maven 3.8+

### 1. 启动后端服务

```bash
cd backend
mvn spring-boot:run
```

后端服务将在 http://localhost:8080 启动

默认管理员账号: `admin` / `admin123`

### 2. 启动前端服务

```bash
cd frontend
npm install
npm run dev
```

前端服务将在 http://localhost:5173 启动

### 3. 访问系统

打开浏览器访问 http://localhost:5173

## 配置说明

### 邮件配置

编辑 `backend/src/main/resources/application.yml`:

```yaml
spring:
  mail:
    username: your_qq_email@qq.com
    password: your_qq_smtp_auth_code
```

### 文件存储路径

```yaml
file:
  storage:
    dir-a: ./storage/dirA
    dir-b: ./storage/dirB
```

### JWT 密钥

```yaml
jwt:
  secret: your-secret-key
  expiration: 86400000
```

## API 文档

### 认证相关
- POST `/api/auth/login` - 登录
- POST `/api/auth/register` - 注册

### 用户管理
- GET `/api/users` - 获取所有用户
- PUT `/api/users/{id}/status` - 修改用户状态
- POST `/api/users/{id}/reset-password` - 重置密码
- GET `/api/users/me` - 获取当前用户

### 公司管理
- GET `/api/companies` - 获取所有公司
- POST `/api/companies` - 创建公司

### 文件管理
- POST `/api/files/upload` - 上传文件
- GET `/api/files/records` - 获取上传记录
- GET `/api/files/dirA` - 获取目录A文件
- GET `/api/files/dirB` - 获取目录B文件
- GET `/api/files/dirB/{companyName}/{filename}` - 下载文件

## 目录结构

```
file-manage-system/
├── backend/                 # SpringBoot 后端
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/filemanage/
│   │   │   │       ├── config/      # 配置类
│   │   │   │       ├── controller/  # 控制器
│   │   │   │       ├── dto/         # 数据传输对象
│   │   │   │       ├── entity/      # 实体类
│   │   │   │       ├── repository/  # 数据访问层
│   │   │   │       ├── security/    # 安全配置
│   │   │   │       └── service/     # 业务逻辑层
│   │   │   └── resources/
│   │   │       └── application.yml
│   │   └── test/
│   └── pom.xml
├── frontend/                # Vue3 前端
│   ├── src/
│   │   ├── api/             # API 接口
│   │   ├── router/          # 路由配置
│   │   ├── stores/          # Pinia 状态管理
│   │   ├── views/           # 页面组件
│   │   ├── App.vue
│   │   └── main.js
│   ├── index.html
│   ├── package.json
│   └── vite.config.js
└── README.md
```

## 开发说明

### 后端开发

1. 使用 Maven 管理依赖
2. 使用 Lombok 简化代码
3. 使用 Spring Security 进行权限控制
4. 使用 JWT 进行身份认证

### 前端开发

1. 使用 Vite 构建工具
2. 使用 Element Plus UI 组件库
3. 使用 Pinia 进行状态管理
4. 使用 Vue Router 进行路由管理

## 注意事项

1. 首次启动会自动创建默认管理员账号
2. 新注册用户默认为禁用状态，需管理员启用
3. 文件上传后会自动重命名为 `公司名_原始文件名` 格式
4. 邮件功能需要配置正确的 QQ 邮箱 SMTP 授权码
5. 重启服务后数据不会丢失（H2 文件型数据库）

## License

MIT
