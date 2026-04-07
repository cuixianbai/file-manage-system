-- 企业多公司文件管理系统生产环境数据库初始化脚本
-- 生成时间: 2026-04-07

-- 创建数据库
CREATE DATABASE IF NOT EXISTS filemanage_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE filemanage_db;

-- 创建公司表
CREATE TABLE IF NOT EXISTS companies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    status ENUM('ENABLED', 'DISABLED') NOT NULL DEFAULT 'ENABLED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    company_id BIGINT NOT NULL,
    role ENUM('ADMIN', 'MANAGER', 'USER') NOT NULL,
    status ENUM('ENABLED', 'DISABLED') NOT NULL DEFAULT 'ENABLED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES companies(id)
);

-- 创建文件记录表
CREATE TABLE IF NOT EXISTS file_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_filename VARCHAR(255) NOT NULL,
    new_filename VARCHAR(255) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    file_size BIGINT,
    company_id BIGINT NOT NULL,
    uploaded_by BIGINT NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('UPLOADED', 'PROCESSING', 'COMPLETED', 'FAILED') NOT NULL DEFAULT 'UPLOADED',
    FOREIGN KEY (company_id) REFERENCES companies(id),
    FOREIGN KEY (uploaded_by) REFERENCES users(id)
);

-- 创建索引
CREATE INDEX idx_users_company_id ON users(company_id);
CREATE INDEX idx_file_records_company_id ON file_records(company_id);
CREATE INDEX idx_file_records_uploaded_by ON file_records(uploaded_by);

-- 插入基础数据
-- 插入默认公司
INSERT INTO companies (name) VALUES ('系统管理') ON DUPLICATE KEY UPDATE name = '系统管理';

-- 插入默认管理员用户 (密码: qwe123)
INSERT INTO users (username, password, email, company_id, role, status) 
VALUES ('admin', 'qwe123', 'admin@example.com', 1, 'ADMIN', 'ENABLED') 
ON DUPLICATE KEY UPDATE password = 'qwe123', status = 'ENABLED';

-- 提交事务
COMMIT;

-- 查看结果
SELECT '数据库初始化完成' AS result;
SELECT '公司表数据:' AS table_name;
SELECT * FROM companies;
SELECT '用户表数据:' AS table_name;
SELECT * FROM users;