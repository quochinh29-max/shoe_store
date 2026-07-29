SET SQL_SAFE_UPDATES = 0;
SET FOREIGN_KEY_CHECKS = 0;

-- 1. Brands
DELETE FROM brands;
INSERT INTO brands (name, logo_url) VALUES
('Nike', 'nike_logo.png'), ('Adidas', 'adidas_logo.png'), ('Puma', 'puma_logo.png'),
('Reebok', 'reebok_logo.png'), ('Vans', 'vans_logo.png'), ('Converse', 'converse_logo.png'),
('New Balance', 'nb_logo.png'), ('Asics', 'asics_logo.png'), ('Fila', 'fila_logo.png'),
('Skechers', 'skechers_logo.png');

-- 2. Colors
DELETE FROM colors;
INSERT INTO colors (color_name, hex_code) VALUES
('Đen', '#000000'), ('Trắng', '#FFFFFF'), ('Đỏ', '#FF0000'), ('Xanh dương', '#0000FF'),
('Xanh lá', '#00FF00'), ('Vàng', '#FFFF00'), ('Cam', '#FFA500'), ('Xám', '#808080'),
('Hồng', '#FFC0CB'), ('Nâu', '#A52A2A');

-- 3. Sizes
DELETE FROM sizes;
INSERT INTO sizes (size_value) VALUES
('36'), ('37'), ('38'), ('39'), ('40'), ('41'), ('42'), ('43'), ('44'), ('45');

-- 4. Users (Thêm đầy đủ các trường bắt buộc của Entity User: username, email, password, password_hash)
DELETE FROM users;
INSERT INTO users (username, email, password, password_hash, full_name, phone_number, role) VALUES
('admin', 'admin@shoestore.com', 'admin123', 'hashed_pw_1', 'Admin System', '0901234567', 'ADMIN'),
('staff1', 'staff1@shoestore.com', 'staff123', 'hashed_pw_2', 'Nguyen Van Staff', '0912345678', 'CUSTOMER'),
('customer1', 'customer1@gmail.com', 'pass123', 'hashed_pw_3', 'Tran Van A', '0923456789', 'CUSTOMER'),
('customer2', 'customer2@gmail.com', 'pass123', 'hashed_pw_4', 'Le Thi B', '0934567890', 'CUSTOMER'),
('customer3', 'customer3@gmail.com', 'pass123', 'hashed_pw_5', 'Pham Van C', '0945678901', 'CUSTOMER'),
('customer4', 'customer4@gmail.com', 'pass123', 'hashed_pw_6', 'Hoang Thi D', '0956789012', 'CUSTOMER'),
('customer5', 'customer5@gmail.com', 'pass123', 'hashed_pw_7', 'Ngo Van E', '0967890123', 'CUSTOMER'),
('customer6', 'customer6@gmail.com', 'pass123', 'hashed_pw_8', 'Vu Thi F', '0978901234', 'CUSTOMER'),
('customer7', 'customer7@gmail.com', 'pass123', 'hashed_pw_9', 'Bui Van G', '0989012345', 'CUSTOMER'),
('customer8', 'customer8@gmail.com', 'pass123', 'hashed_pw_10', 'Dang Thi H', '0990123456', 'CUSTOMER');

-- 5. Vouchers
DELETE FROM vouchers;
INSERT INTO vouchers (code, discount_type, discount_value, min_order_value, max_discount, start_date, end_date, usage_limit, used_count) VALUES
('SUMMER24', 'PERCENT', 10, 500000, 100000, '2024-06-01', '2024-08-31', 100, 0),
('FREESHIP', 'FIXED_AMOUNT', 30000, 300000, NULL, '2024-01-01', '2024-12-31', 500, 50),
('NEWYEAR', 'PERCENT', 15, 1000000, 200000, '2024-01-01', '2024-01-10', 200, 200),
('FLASH1', 'PERCENT', 50, 0, 50000, '2024-11-11', '2024-11-11', 50, 50),
('BLACKFRIDAY', 'PERCENT', 30, 200000, 150000, '2024-11-20', '2024-11-30', 300, 10),
('VIPUSER', 'FIXED_AMOUNT', 100000, 2000000, NULL, '2024-01-01', '2025-01-01', 1000, 5),
('WELCOME', 'PERCENT', 5, 0, 50000, '2024-01-01', '2025-01-01', 9999, 100),
('WEEKEND', 'FIXED_AMOUNT', 50000, 500000, NULL, '2024-07-06', '2024-07-07', 50, 0),
('SHOES10', 'PERCENT', 10, 800000, 120000, '2024-01-01', '2024-12-31', 200, 20),
('BIRTHDAY', 'FIXED_AMOUNT', 200000, 1000000, NULL, '2024-01-01', '2024-12-31', 300, 2);

-- 6. Categories
DELETE FROM categories;
INSERT INTO categories (id, parent_id, name, slug, status) VALUES
(1, NULL, 'Giày Thể Thao', 'giay-the-thao', true),
(2, NULL, 'Giày Chạy Bộ', 'giay-chay-bo', true),
(3, NULL, 'Giày Thời Trang', 'giay-thoi-trang', true),
(4, NULL, 'Giày Đá Bóng', 'giay-da-bong', true),
(5, NULL, 'Giày Trẻ Em', 'giay-tre-em', true),
(6, 1, 'Sneakers Nam', 'sneakers-nam', true),
(7, 1, 'Sneakers Nữ', 'sneakers-nu', true),
(8, 2, 'Giày Chạy Marathon', 'giay-chay-marathon', true),
(9, 3, 'Giày Lười', 'giay-luoi', true),
(10, 4, 'Giày Sân Cỏ Nhân Tạo', 'giay-san-co-nhan-tao', true);

-- 7. User Addresses
DELETE FROM user_addresses;
INSERT INTO user_addresses (user_id, full_name, phone_number, address_line, city, district, ward, is_default) VALUES
(3, 'Tran Van A', '0923456789', '123 Le Loi', 'TP HCM', 'Quận 1', 'Phường Bến Nghé', true),
(3, 'Tran Van A', '0923456789', '456 Nguyen Hue', 'TP HCM', 'Quận 1', 'Phường Bến Nghé', false),
(4, 'Le Thi B', '0934567890', '789 Tran Hung Dao', 'Hà Nội', 'Quận Hoàn Kiếm', 'Phường Hàng Bài', true),
(5, 'Pham Van C', '0945678901', '101 Nguyen Trai', 'TP HCM', 'Quận 5', 'Phường 2', true),
(6, 'Hoang Thi D', '0956789012', '202 Le Hong Phong', 'Đà Nẵng', 'Quận Hải Châu', 'Phường Thạch Thang', true),
(7, 'Ngo Van E', '0967890123', '303 Nguyen Dinh Chieu', 'TP HCM', 'Quận 3', 'Phường 5', true),
(8, 'Vu Thi F', '0978901234', '404 CMT8', 'TP HCM', 'Quận 10', 'Phường 12', true),
(9, 'Bui Van G', '0989012345', '505 Dinh Tien Hoang', 'Hà Nội', 'Quận Ba Đình', 'Phường Kim Mã', true),
(10, 'Dang Thi H', '0990123456', '606 Hai Ba Trung', 'Hà Nội', 'Quận Hai Bà Trưng', 'Phường Bùi Thị Xuân', true),
(10, 'Dang Thi H', '0990123456', '707 Ba Trieu', 'Hà Nội', 'Quận Hai Bà Trưng', 'Phường Lê Đại Hành', false);

-- 8. Products (Có thêm price và quantity như trong entity java)
DELETE FROM products;
INSERT INTO products (id, category_id, brand_id, name, slug, description, base_price, price, quantity, status) VALUES
(1, 6, 1, 'Nike Air Max', 'nike-air-max', 'Giày thể thao nam cao cấp.', 2500000, 2500000, 100, true),
(2, 7, 2, 'Adidas Ultraboost', 'adidas-ultraboost', 'Giày chạy bộ êm ái.', 3000000, 3000000, 100, true),
(3, 8, 3, 'Puma Deviate', 'puma-deviate', 'Giày marathon chuyên nghiệp.', 2800000, 2800000, 100, true),
(4, 9, 6, 'Converse Chuck Taylor', 'converse-chuck-taylor', 'Cổ điển và phong cách.', 1200000, 1200000, 100, true),
(5, 10, 1, 'Nike Mercurial', 'nike-mercurial', 'Giày đá bóng nhẹ nhàng.', 3500000, 3500000, 100, true),
(6, 6, 5, 'Vans Old Skool', 'vans-old-skool', 'Thời trang đường phố.', 1500000, 1500000, 100, true),
(7, 7, 7, 'New Balance 574', 'new-balance-574', 'Giày nữ thoải mái.', 1800000, 1800000, 100, true),
(8, 8, 8, 'Asics Gel-Nimbus', 'asics-gel-nimbus', 'Giày chạy bộ cao cấp.', 3200000, 3200000, 100, true),
(9, 9, 9, 'Fila Disruptor', 'fila-disruptor', 'Chunky sneaker.', 1600000, 1600000, 100, true),
(10, 10, 10, 'Skechers Go Walk', 'skechers-go-walk', 'Đi bộ siêu êm.', 1400000, 1400000, 100, true);

-- 9. Product Variants
DELETE FROM product_variants;
INSERT INTO product_variants (id, product_id, size_id, color_id, sku, price, stock_quantity) VALUES
(1, 1, 6, 1, 'NK-AM-41-B', 2500000, 50),
(2, 1, 7, 2, 'NK-AM-42-W', 2500000, 30),
(3, 2, 5, 1, 'AD-UB-40-B', 3000000, 100),
(4, 2, 6, 4, 'AD-UB-41-BL', 3000000, 20),
(5, 3, 7, 3, 'PM-DV-42-R', 2800000, 10),
(6, 4, 4, 1, 'CV-CT-39-B', 1200000, 200),
(7, 5, 7, 6, 'NK-MC-42-Y', 3500000, 5),
(8, 6, 6, 2, 'VN-OS-41-W', 1500000, 80),
(9, 7, 3, 9, 'NB-574-38-P', 1800000, 40),
(10, 8, 8, 8, 'AS-GN-43-G', 3200000, 15);

-- 10. Product Images
DELETE FROM product_images;
INSERT INTO product_images (product_id, variant_id, image_url, is_thumbnail) VALUES
(1, NULL, 'nike_am_thumb.jpg', true),
(1, 1, 'nike_am_black.jpg', false),
(1, 2, 'nike_am_white.jpg', false),
(2, NULL, 'adidas_ub_thumb.jpg', true),
(2, 3, 'adidas_ub_black.jpg', false),
(3, NULL, 'puma_dv_thumb.jpg', true),
(4, NULL, 'converse_ct_thumb.jpg', true),
(5, NULL, 'nike_mc_thumb.jpg', true),
(6, NULL, 'vans_os_thumb.jpg', true),
(7, NULL, 'nb_574_thumb.jpg', true);

-- 11. Orders
DELETE FROM orders;
INSERT INTO orders (id, user_id, voucher_id, total_amount, discount_amount, final_amount, order_status, payment_method, payment_status, shipping_address, note) VALUES
(1, 3, NULL, 2500000, 0, 2500000, 'COMPLETED', 'COD', 'PAID', '123 Le Loi, TP HCM', 'Giao chiều'),
(2, 4, 1, 3000000, 100000, 2900000, 'SHIPPING', 'VNPAY', 'PAID', '789 Tran Hung Dao, HN', NULL),
(3, 5, 2, 2800000, 30000, 2770000, 'CONFIRMED', 'COD', 'PENDING', '101 Nguyen Trai, HCM', 'Gọi trước khi giao'),
(4, 6, NULL, 1200000, 0, 1200000, 'PENDING', 'COD', 'PENDING', '202 Le Hong Phong, DN', NULL),
(5, 7, NULL, 3500000, 0, 3500000, 'CANCELLED', 'VNPAY', 'FAILED', '303 Nguyen Dinh Chieu, HCM', 'Khách hủy'),
(6, 8, 6, 1500000, 100000, 1400000, 'COMPLETED', 'COD', 'PAID', '404 CMT8, HCM', NULL),
(7, 9, NULL, 1800000, 0, 1800000, 'SHIPPING', 'COD', 'PENDING', '505 Dinh Tien Hoang, HN', NULL),
(8, 10, 9, 3200000, 120000, 3080000, 'COMPLETED', 'VNPAY', 'PAID', '606 Hai Ba Trung, HN', 'Gói quà'),
(9, 3, NULL, 2500000, 0, 2500000, 'PENDING', 'COD', 'PENDING', '123 Le Loi, TP HCM', NULL),
(10, 4, NULL, 3000000, 0, 3000000, 'PENDING', 'VNPAY', 'PENDING', '789 Tran Hung Dao, HN', NULL);

-- 12. Order Details
DELETE FROM order_details;
INSERT INTO order_details (order_id, variant_id, quantity, unit_price) VALUES
(1, 1, 1, 2500000), (2, 3, 1, 3000000), (3, 5, 1, 2800000), (4, 6, 1, 1200000),
(5, 7, 1, 3500000), (6, 8, 1, 1500000), (7, 9, 1, 1800000), (8, 10, 1, 3200000),
(9, 2, 1, 2500000), (10, 4, 1, 3000000);

-- 13. Reviews
DELETE FROM reviews;
INSERT INTO reviews (user_id, product_id, rating, comment) VALUES
(3, 1, 5, 'Giày rất đẹp, mang êm chân.'), (4, 2, 4, 'Chất lượng tốt nhưng giao hơi chậm.'),
(5, 3, 5, 'Rất hài lòng về sản phẩm.'), (6, 4, 3, 'Bình thường, không giống ảnh lắm.'),
(8, 6, 5, 'Sẽ ủng hộ shop thêm nhiều lần.'), (9, 7, 4, 'Màu ở ngoài nhạt hơn một chút.'),
(10, 8, 5, 'Quá tuyệt vời!'), (3, 2, 4, 'Giày auth chuẩn.'),
(4, 1, 5, 'Size vừa in.'), (5, 4, 4, 'Chăm sóc khách hàng tốt.');

-- 14. Daily Revenue Reports
DELETE FROM daily_revenue_reports;
INSERT INTO daily_revenue_reports (report_date, total_orders, successful_orders, cancelled_orders, total_revenue) VALUES
('2024-07-01', 10, 8, 2, 15000000), ('2024-07-02', 15, 14, 1, 25000000),
('2024-07-03', 12, 12, 0, 18000000), ('2024-07-04', 8, 7, 1, 12000000),
('2024-07-05', 20, 18, 2, 35000000), ('2024-07-06', 5, 5, 0, 8000000),
('2024-07-07', 25, 20, 5, 40000000), ('2024-07-08', 30, 29, 1, 50000000),
('2024-07-09', 14, 10, 4, 20000000), ('2024-07-10', 18, 17, 1, 28000000);

SET FOREIGN_KEY_CHECKS = 1;
