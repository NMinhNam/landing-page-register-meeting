USE `dang_ky_tham_quan_nhan`;

-- 1. Dữ liệu Đơn vị (Unit)
-- Cấp 1
INSERT INTO unit (id, name, parent_id) VALUES (1, 'Tiểu đoàn 1', NULL);
INSERT INTO unit (id, name, parent_id) VALUES (2, 'Tiểu đoàn 2', NULL);
INSERT INTO unit (id, name, parent_id) VALUES (3, 'Tiểu đoàn 3', NULL);

-- Cấp 2 (Đại đội thuộc Tiểu đoàn 1)
INSERT INTO unit (id, name, parent_id) VALUES (4, 'Đại đội 1', 1);
INSERT INTO unit (id, name, parent_id) VALUES (5, 'Đại đội 2', 1);
INSERT INTO unit (id, name, parent_id) VALUES (6, 'Đại đội 3', 1);

-- Cấp 2 (Đại đội thuộc Tiểu đoàn 2)
INSERT INTO unit (id, name, parent_id) VALUES (7, 'Đại đội 4', 2);
INSERT INTO unit (id, name, parent_id) VALUES (8, 'Đại đội 5', 2);

-- Reset Auto Increment for Unit if needed (optional since we hardcoded IDs)
ALTER TABLE unit AUTO_INCREMENT = 10;

-- 2. Dữ liệu Quân nhân (Soldier)
INSERT INTO soldier (code, name, unit_id, status) VALUES ('QN001', 'Nguyễn Văn An', 4, 'ACTIVE');
INSERT INTO soldier (code, name, unit_id, status) VALUES ('QN002', 'Trần Bình Trọng', 4, 'ACTIVE');
INSERT INTO soldier (code, name, unit_id, status) VALUES ('QN003', 'Lê Lai', 5, 'ACTIVE');
INSERT INTO soldier (code, name, unit_id, status) VALUES ('QN004', 'Phạm Ngũ Lão', 5, 'ACTIVE');
INSERT INTO soldier (code, name, unit_id, status) VALUES ('QN005', 'Võ Thị Sáu', 6, 'INACTIVE');
INSERT INTO soldier (code, name, unit_id, status) VALUES ('QN006', 'Hoàng Hoa Thám', 7, 'ACTIVE');
INSERT INTO soldier (code, name, unit_id, status) VALUES ('QN007', 'Lý Thường Kiệt', 7, 'ACTIVE');
INSERT INTO soldier (code, name, unit_id, status) VALUES ('QN008', 'Ngô Quyền', 8, 'ACTIVE');
INSERT INTO soldier (code, name, unit_id, status) VALUES ('QN009', 'Đinh Bộ Lĩnh', 8, 'ACTIVE');
INSERT INTO soldier (code, name, unit_id, status) VALUES ('QN010', 'Trần Hưng Đạo', 4, 'ACTIVE');

-- 3. Dữ liệu Admin
INSERT INTO admin_user (username, password, unit_id, role) VALUES ('admin', 'admin123', 1, 'ADMIN');
INSERT INTO admin_user (username, password, unit_id, role) VALUES ('viewer_c1', '123456', 4, 'VIEWER');
INSERT INTO admin_user (username, password, unit_id, role) VALUES ('mod_d2', '123456', 2, 'ADMIN');

-- 4. Dữ liệu Đăng ký thăm (Visit Registration)
-- 5. Dữ liệu Người thân (Relative)