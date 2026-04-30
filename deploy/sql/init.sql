-- ============================================================
-- 智能食堂点餐与取餐微服务系统 — 数据库初始化脚本
-- ============================================================

CREATE DATABASE IF NOT EXISTS smart_canteen
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE smart_canteen;

-- ============================================================
-- 1. 用户表
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    phone       VARCHAR(20)  NOT NULL,
    student_no  VARCHAR(50)  DEFAULT NULL,
    password    VARCHAR(255) NOT NULL,
    nickname    VARCHAR(100) DEFAULT NULL,
    role        VARCHAR(20)  NOT NULL DEFAULT 'STUDENT' COMMENT 'STUDENT/MERCHANT/ADMIN',
    status      VARCHAR(20)  NOT NULL DEFAULT 'NORMAL' COMMENT 'NORMAL/DISABLED',
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_phone      (phone),
    UNIQUE KEY uk_student_no (student_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 2. 菜品表
-- ============================================================
CREATE TABLE IF NOT EXISTS dishes (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100)   NOT NULL,
    price         DECIMAL(10,2)  NOT NULL,
    description   VARCHAR(500)   DEFAULT NULL,
    image_url     VARCHAR(500)   DEFAULT NULL,
    stock         INT            NOT NULL DEFAULT 0,
    warning_stock INT            NOT NULL DEFAULT 10,
    status        VARCHAR(20)    NOT NULL DEFAULT 'ON_SALE' COMMENT 'ON_SALE/OFF_SALE',
    created_at    DATETIME       DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 3. 每日菜单表
-- ============================================================
CREATE TABLE IF NOT EXISTS daily_menus (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    menu_date   DATE         NOT NULL,
    meal_period VARCHAR(20)  NOT NULL COMMENT 'BREAKFAST/LUNCH/DINNER',
    start_time  TIME         DEFAULT NULL,
    end_time    TIME         DEFAULT NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/INACTIVE',
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 4. 每日菜单明细表
-- ============================================================
CREATE TABLE IF NOT EXISTS daily_menu_items (
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    menu_id BIGINT NOT NULL,
    dish_id BIGINT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 5. 订单表
-- ============================================================
CREATE TABLE IF NOT EXISTS orders (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT         NOT NULL,
    window_id    BIGINT         NOT NULL,
    total_amount DECIMAL(10,2)  NOT NULL DEFAULT 0.00,
    status       VARCHAR(20)    NOT NULL DEFAULT 'CREATED' COMMENT 'CREATED/ACCEPTED/COOKING/WAIT_PICKUP/COMPLETED/CANCELLED',
    pickup_no    INT            DEFAULT NULL,
    pickup_code  VARCHAR(20)    DEFAULT NULL,
    created_at   DATETIME       DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id   (user_id),
    INDEX idx_status    (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 6. 订单明细表
-- ============================================================
CREATE TABLE IF NOT EXISTS order_items (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id  BIGINT         NOT NULL,
    dish_id   BIGINT         NOT NULL,
    dish_name VARCHAR(100)   NOT NULL,
    price     DECIMAL(10,2)  NOT NULL,
    quantity  INT            NOT NULL DEFAULT 1,
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 7. 取餐窗口表
-- ============================================================
CREATE TABLE IF NOT EXISTS pickup_windows (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    location   VARCHAR(200) DEFAULT NULL,
    status     VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/DISABLED',
    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 8. 取餐队列表
-- ============================================================
CREATE TABLE IF NOT EXISTS pickup_queue (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    window_id   BIGINT      NOT NULL,
    order_id    BIGINT      NOT NULL,
    pickup_no   INT         NOT NULL,
    pickup_code VARCHAR(20) NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'WAITING' COMMENT 'WAITING/CALLED/FINISHED/CANCELLED',
    queue_time  DATETIME    DEFAULT CURRENT_TIMESTAMP,
    call_time   DATETIME    DEFAULT NULL,
    finish_time DATETIME    DEFAULT NULL,
    created_at  DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_window_id (window_id),
    INDEX idx_pickup_no (pickup_no),
    INDEX idx_status   (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 演示数据
-- ============================================================

-- 密码统一为 123456（BCrypt 加密，使用 Spring Security BCryptPasswordEncoder 生成）
INSERT INTO users (phone, student_no, password, nickname, role, status) VALUES
('13800000001', '2024001', '$2a$10$xiZtRmYtYAvlZkmBKRQQTObXognvwDo5gaf6p.VaRJMk/Un6VlF9i', '张三', 'STUDENT',  'NORMAL'),
('13800000002', '2024002', '$2a$10$xiZtRmYtYAvlZkmBKRQQTObXognvwDo5gaf6p.VaRJMk/Un6VlF9i', '李老板', 'MERCHANT', 'NORMAL'),
('13800000003', '2024003', '$2a$10$xiZtRmYtYAvlZkmBKRQQTObXognvwDo5gaf6p.VaRJMk/Un6VlF9i', '管理员', 'ADMIN',    'NORMAL');

-- 5 个演示菜品
INSERT INTO dishes (name, price, description, stock, warning_stock, status) VALUES
('红烧肉',   25.00, '经典红烧肉，肥而不腻',   50, 10, 'ON_SALE'),
('番茄炒蛋', 12.00, '酸甜可口，下饭神器',     80, 15, 'ON_SALE'),
('宫保鸡丁', 22.00, '麻辣鲜香，经典川菜',     40, 8,  'ON_SALE'),
('清炒时蔬', 10.00, '当季新鲜蔬菜',          100, 20, 'ON_SALE'),
('紫菜蛋花汤', 8.00, '清淡鲜美',             60, 10, 'ON_SALE');

-- 2 个取餐窗口
INSERT INTO pickup_windows (name, location, status) VALUES
('1号窗口', '一楼A区', 'ACTIVE'),
('2号窗口', '二楼B区', 'ACTIVE');
