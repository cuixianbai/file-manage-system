-- 企业多公司文件管理系统数据库初始化脚本
-- 生成时间: 2026-04-05

-- 创建公司表
CREATE TABLE IF NOT EXISTS companies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(50),
    role VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES companies(id)
);

-- 创建文件表
CREATE TABLE IF NOT EXISTS files (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT,
    user_id BIGINT,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(512) NOT NULL,
    file_size BIGINT NOT NULL,
    file_type VARCHAR(100) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES companies(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 创建文件夹表
CREATE TABLE IF NOT EXISTS folders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT,
    parent_id BIGINT,
    folder_name VARCHAR(255) NOT NULL,
    path VARCHAR(512) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES companies(id),
    FOREIGN KEY (parent_id) REFERENCES folders(id)
);

-- 创建文件与文件夹关联表
CREATE TABLE IF NOT EXISTS file_folder (
    file_id BIGINT,
    folder_id BIGINT,
    PRIMARY KEY (file_id, folder_id),
    FOREIGN KEY (file_id) REFERENCES files(id),
    FOREIGN KEY (folder_id) REFERENCES folders(id)
);

-- 创建操作记录表
CREATE TABLE IF NOT EXISTS operations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT,
    user_id BIGINT,
    operation_type VARCHAR(50) NOT NULL,
    operation_object VARCHAR(255) NOT NULL,
    operation_detail TEXT,
    ip_address VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES companies(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 插入基础数据
-- 插入默认公司
INSERT INTO companies (name, status) VALUES ('默认公司', 'ACTIVE') ON DUPLICATE KEY UPDATE status = 'ACTIVE';

-- 插入默认管理员用户 (密码: admin123)
INSERT INTO users (company_id, username, password, name, email, phone, role, status) 
VALUES (1, 'admin', '$2a$10$eNf1fF3JQxL9V7h7X9X9X9eQ7q7q7q7q7q7q7q7q7q7q7q7q7q7q7', '系统管理员', 'admin@example.com', '13800138000', 'ADMIN', 'ACTIVE') 
ON DUPLICATE KEY UPDATE status = 'ACTIVE';

-- 插入默认文件夹
INSERT INTO folders (company_id, parent_id, folder_name, path, status) 
VALUES (1, NULL, '根目录', '/', 'ACTIVE') 
ON DUPLICATE KEY UPDATE status = 'ACTIVE';

-- 提交事务
COMMIT;