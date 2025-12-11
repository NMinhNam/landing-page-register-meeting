# DROP DATABASE `dang_ky_tham_quan_nhan`;

CREATE DATABASE IF NOT EXISTS `dang_ky_tham_quan_nhan`;

USE `dang_ky_tham_quan_nhan`;

-- Xóa bảng cũ nếu có để reset sạch sẽ
DROP TABLE IF EXISTS admin_user;
DROP TABLE IF EXISTS visit_registration;
DROP TABLE IF EXISTS relative;
DROP TABLE IF EXISTS soldier;
DROP TABLE IF EXISTS unit;

/* ===========================
   1. BẢNG ĐƠN VỊ QUÂN ĐỘI
   =========================== */
CREATE TABLE unit (
                      id          BIGINT PRIMARY KEY AUTO_INCREMENT,
                      name        VARCHAR(100) NOT NULL,
                      parent_id   BIGINT NULL,
                      CONSTRAINT fk_unit_parent
                          FOREIGN KEY (parent_id) REFERENCES unit(id)
);

/* ===========================
   2. BẢNG QUÂN NHÂN
   =========================== */
CREATE TABLE soldier (
                         id          BIGINT PRIMARY KEY AUTO_INCREMENT,
                         code        VARCHAR(50) UNIQUE NOT NULL,
                         name        VARCHAR(100)       NOT NULL,
                         unit_id     BIGINT             NOT NULL,
                         status      ENUM('ACTIVE','INACTIVE') DEFAULT 'ACTIVE',
                         CONSTRAINT fk_soldier_unit
                             FOREIGN KEY (unit_id) REFERENCES unit(id)
);

/* ===========================
   3. BẢNG NGƯỜI THÂN (CHI TIẾT)
   =========================== */
CREATE TABLE relative (
                          id          BIGINT PRIMARY KEY AUTO_INCREMENT,
                          visit_registration_id BIGINT NOT NULL,
                          name        VARCHAR(100) NOT NULL,
                          relationship VARCHAR(50) NOT NULL,
                          id_number   VARCHAR(50)  NULL
);

/* ===========================
   4. BẢNG ĐĂNG KÝ THĂM
   =========================== */
CREATE TABLE visit_registration (
                                    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
                                    soldier_id      BIGINT      NULL,                  -- Có thể NULL nếu nhập tay
                                    manual_soldier_name VARCHAR(100) NULL,             -- Tên quân nhân nhập tay
                                    manual_unit_name    VARCHAR(100) NULL,             -- Tên đơn vị nhập tay
                                    representative_phone VARCHAR(20) NOT NULL,
                                    province        VARCHAR(100) NULL,                 -- Tỉnh/TP của người đại diện (để lọc)
                                    visit_week      TINYINT     NULL,
                                    status          ENUM('PENDING','APPROVED','REJECTED','CANCELLED') NOT NULL DEFAULT 'PENDING',
                                    note            VARCHAR(255) NULL,
                                    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    approved_at     DATETIME NULL,

                                    CONSTRAINT fk_visit_soldier
                                        FOREIGN KEY (soldier_id)  REFERENCES soldier(id)
);

ALTER TABLE relative ADD CONSTRAINT fk_relative_visit 
    FOREIGN KEY (visit_registration_id) REFERENCES visit_registration(id) ON DELETE CASCADE;

/* ===========================
   5. BẢNG TÀI KHOẢN QUẢN TRỊ
   =========================== */
CREATE TABLE admin_user (
                            id          BIGINT PRIMARY KEY AUTO_INCREMENT,
                            username    VARCHAR(50) UNIQUE NOT NULL,
                            password    VARCHAR(255)       NOT NULL,
                            unit_id     BIGINT             NULL,
                            role        ENUM('ADMIN','VIEWER') DEFAULT 'ADMIN',
                            created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                            CONSTRAINT fk_admin_unit
                                FOREIGN KEY (unit_id) REFERENCES unit(id)
);

-- INDEX
CREATE INDEX idx_soldier_unit ON soldier(unit_id);
CREATE INDEX idx_visit_status ON visit_registration(status);
CREATE INDEX idx_visit_province_week ON visit_registration(visit_week);
CREATE INDEX idx_visit_soldier ON visit_registration(soldier_id);