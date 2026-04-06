-- 企业多公司文件管理系统数据库初始化脚本
-- 生成时间: 2026-04-06

-- 创建公司表
CREATE TABLE IF NOT EXISTS companies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
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
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
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
    status VARCHAR(20) NOT NULL,
    FOREIGN KEY (company_id) REFERENCES companies(id),
    FOREIGN KEY (uploaded_by) REFERENCES users(id)
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_users_company_id ON users(company_id);
CREATE INDEX IF NOT EXISTS idx_file_records_company_id ON file_records(company_id);
CREATE INDEX IF NOT EXISTS idx_file_records_uploaded_by ON file_records(uploaded_by);

-- 插入基础数据
-- 插入默认公司
INSERT INTO companies (name) VALUES ('系统管理') ON DUPLICATE KEY UPDATE name = '系统管理';

-- 插入默认管理员用户 (密码: admin123)
INSERT INTO users (username, password, email, company_id, role, status) 
VALUES ('admin', '$2a$10$eNf1fF3JQxL9V7h7X9X9X9eQ7q7q7q7q7q7q7q7q7q7q7q7q7q7', 'admin@example.com', 1, 'ADMIN', 'ENABLED') 
ON DUPLICATE KEY UPDATE status = 'ENABLED';

-- 提交事务
COMMIT;