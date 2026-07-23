CREATE DATABASE IF NOT EXISTS quanlybangiay DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE quanlybangiay;

-- 1. Bảng Vai trò (Role)
CREATE TABLE vai_tro (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ten_vai_tro VARCHAR(50) NOT NULL UNIQUE
);

-- 2. Bảng Người dùng (User)
CREATE TABLE nguoi_dung (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ho_ten VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    mat_khau VARCHAR(255) NOT NULL,
    so_dien_thoai VARCHAR(15),
    dia_chi TEXT,
    id_vai_tro INT NOT NULL,
    FOREIGN KEY (id_vai_tro) REFERENCES vai_tro(id) ON DELETE RESTRICT
);

-- 3. Bảng Danh mục (Category)
CREATE TABLE danh_muc (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ten_danh_muc VARCHAR(100) NOT NULL UNIQUE
);

-- 4. Bảng Thương hiệu (Brand)
CREATE TABLE thuong_hieu (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ten_thuong_hieu VARCHAR(100) NOT NULL UNIQUE
);

-- 5. Bảng Sản phẩm (Product) - Thông tin chung của mẫu giày
CREATE TABLE san_pham (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ten_san_pham VARCHAR(255) NOT NULL,
    mo_ta TEXT,
    gia_ban DECIMAL(15, 2) NOT NULL,
    id_danh_muc INT NOT NULL,
    id_thuong_hieu INT NOT NULL,
    FOREIGN KEY (id_danh_muc) REFERENCES danh_muc(id) ON DELETE RESTRICT,
    FOREIGN KEY (id_thuong_hieu) REFERENCES thuong_hieu(id) ON DELETE RESTRICT
);

-- 6. Bảng Biến thể Sản phẩm (Product Variant) - Quản lý size và màu sắc
CREATE TABLE bien_the_san_pham (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_san_pham INT NOT NULL,
    kich_co INT NOT NULL, -- Ví dụ: 39, 40, 41, 42
    mau_sac VARCHAR(50) NOT NULL,
    so_luong_ton INT NOT NULL DEFAULT 0,
    FOREIGN KEY (id_san_pham) REFERENCES san_pham(id) ON DELETE CASCADE
);

-- 7. Bảng Đơn hàng (Order)
CREATE TABLE don_hang (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_nguoi_dung INT NOT NULL,
    ngay_dat DATETIME DEFAULT CURRENT_TIMESTAMP,
    tong_tien DECIMAL(15, 2) NOT NULL,
    trang_thai VARCHAR(50) NOT NULL DEFAULT 'Chờ xác nhận',
    dia_chi_giao_hang TEXT NOT NULL,
    FOREIGN KEY (id_nguoi_dung) REFERENCES nguoi_dung(id) ON DELETE RESTRICT
);

-- 8. Bảng Chi tiết Đơn hàng (Order Detail)
CREATE TABLE chi_tiet_don_hang (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_don_hang INT NOT NULL,
    id_bien_the_san_pham INT NOT NULL,
    so_luong INT NOT NULL,
    don_gia DECIMAL(15, 2) NOT NULL,
    FOREIGN KEY (id_don_hang) REFERENCES don_hang(id) ON DELETE CASCADE,
    FOREIGN KEY (id_bien_the_san_pham) REFERENCES bien_the_san_pham(id) ON DELETE RESTRICT
);

