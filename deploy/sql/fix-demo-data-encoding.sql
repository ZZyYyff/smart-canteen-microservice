-- ============================================================
-- 修复当前数据库中已存在的乱码演示数据（不重建数据库）
-- 执行方式:
--   docker exec -i smart-canteen-mysql mysql -uroot -proot --default-character-set=utf8mb4 < deploy/sql/fix-demo-data-encoding.sql
-- ============================================================

SET NAMES utf8mb4;
SET character_set_client = utf8mb4;
SET character_set_connection = utf8mb4;
SET character_set_results = utf8mb4;

USE smart_canteen;

-- 转换表字符集（修复已有表的编码）
ALTER TABLE users CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE dishes CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE pickup_windows CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE orders CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE order_items CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE daily_menus CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE daily_menu_items CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE pickup_queue CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 修复 dishes 表演示数据 (id=1~5)
UPDATE dishes SET name = '红烧牛肉饭', description = '经典红烧牛肉套餐，牛肉软烂入味，适合午餐',
    price = 25.00, stock = 50, warning_stock = 10, status = 'ON_SALE' WHERE id = 1;

UPDATE dishes SET name = '番茄鸡蛋面', description = '酸甜可口的番茄鸡蛋面，分量适中',
    price = 12.00, stock = 80, warning_stock = 15, status = 'ON_SALE' WHERE id = 2;

UPDATE dishes SET name = '宫保鸡丁盖饭', description = '微辣口味，鸡丁鲜嫩，搭配米饭',
    price = 22.00, stock = 40, warning_stock = 8, status = 'ON_SALE' WHERE id = 3;

UPDATE dishes SET name = '蔬菜沙拉', description = '清爽低脂，适合晚餐和轻食',
    price = 10.00, stock = 100, warning_stock = 20, status = 'ON_SALE' WHERE id = 4;

UPDATE dishes SET name = '豆浆油条套餐', description = '早餐经典组合，包含豆浆和油条',
    price = 8.00, stock = 60, warning_stock = 10, status = 'ON_SALE' WHERE id = 5;

-- 修复 users 表演示数据 (id=1~3)，保留密码字段不变
UPDATE users SET nickname = '张三', role = 'STUDENT', status = 'NORMAL' WHERE id = 1;
UPDATE users SET nickname = '李四', role = 'MERCHANT', status = 'NORMAL' WHERE id = 2;
UPDATE users SET nickname = '管理员', role = 'ADMIN', status = 'NORMAL' WHERE id = 3;

-- 修复 pickup_windows 表演示数据 (id=1~2)
UPDATE pickup_windows SET name = '一号取餐窗口', location = '一楼A区', status = 'ACTIVE' WHERE id = 1;
UPDATE pickup_windows SET name = '二号取餐窗口', location = '一楼B区', status = 'ACTIVE' WHERE id = 2;

-- 验证
SELECT '=== dishes ===' AS '';
SELECT id, name, description, price, stock, warning_stock, status FROM dishes;
SELECT '=== users ===' AS '';
SELECT id, phone, student_no, nickname, role, status FROM users;
SELECT '=== pickup_windows ===' AS '';
SELECT id, name, location, status FROM pickup_windows;
