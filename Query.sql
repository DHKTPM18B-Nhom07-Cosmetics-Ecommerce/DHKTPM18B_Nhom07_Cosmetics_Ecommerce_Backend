-- Dumping data for table cosmetics_db.accounts: ~9 rows (approximately)
use cosmetics_db;
-- ==================== Account ====================
INSERT INTO accounts (created_at, disabled_reason, full_name, password, phone_number, role, status, username) VALUES
                                                                                                                  ('2024-01-15 10:30:00.000000', NULL, 'Nguyễn Văn An', '123456', '0901234567', 'ADMIN', 'ACTIVE', 'an.nguyen@example.com'), -- 1. ADMIN đang hoạt động
                                                                                                                  ('2024-01-15 10:30:00.000000', NULL, 'Trần Thị Bích', 'matkhau123', '0934455667', 'CUSTOMER', 'ACTIVE', 'bich.tran@example.com'), -- 2. CUSTOMER hoạt động
                                                                                                                  ('2024-01-15 10:30:00.000000', 'Tài khoản bị khóa do nhập sai mật khẩu nhiều lần', 'Phạm Quốc Huy', 'huy2024', '0988877665', 'CUSTOMER', 'LOCKED', 'huy.pham@example.com'), -- 3. CUSTOMER bị khóa
                                                                                                                  ('2024-01-15 10:30:00.000000', NULL, 'Lê Minh Tuấn', 'tuan@123', '0912345789', 'EMPLOYEE', 'ACTIVE', 'tuan.le@example.com'), -- 4. EMPLOYEE hoạt động
                                                                                                                  ('2024-01-15 10:30:00.000000', 'Nhân viên nghỉ việc', 'Đỗ Thùy Dung', 'dung987', '0909988776', 'EMPLOYEE', 'DISABLED', 'dung.do@example.com'), -- 5. EMPLOYEE bị vô hiệu hóa
                                                                                                                  ('2024-01-15 10:30:00.000000', 'Khóa tạm thời do nghi ngờ đăng nhập trái phép', 'Hoàng Gia Bảo', 'baoSecure!', '0977554433', 'ADMIN', 'LOCKED', 'bao.hoang@example.com'); -- 6. ADMIN bị khóa

-- ==================== Employee ====================
INSERT INTO employees (hire_date, account_id) VALUES
                                                  ('2024-01-15 10:30:00.000000', 4), -- Nhân viên: Lê Minh Tuấn
                                                  ('2024-01-15 10:30:00.000000', 5); -- Nhân viên: Đỗ Thùy Dung

-- ==================== Customers ====================
INSERT INTO customers (account_id) VALUES
                                       (2), -- CUSTOMER hoạt động
                                       (3); -- CUSTOMER bị khóa

-- ==================== Address ====================
INSERT INTO addresses (address, city, country, full_name, is_default, phone, state, customer_id) VALUES
                                                                                                     ('25 Nguyễn Huệ', 'Quận 1', 'Việt Nam', 'Trần Thị Bích', 1, '0934455667', 'TP. Hồ Chí Minh', 1), -- Địa chỉ mặc định của Trần Thị Bích
                                                                                                     ('120 Lê Lợi', 'Hải Châu', 'Việt Nam', 'Phạm Quốc Huy', 1, '0988877665', 'Đà Nẵng', 2), -- Địa chỉ mặc định của Phạm Quốc Huy
                                                                                                     ('45 Trần Hưng Đạo', 'Quận 5', 'Việt Nam', 'Trần Thị Bích', 0, '0934455667', 'TP. Hồ Chí Minh', 1), -- Địa chỉ phụ của Trần Thị Bích
                                                                                                     ('78 Nguyễn Văn Linh', 'Liên Chiểu', 'Việt Nam', 'Phạm Quốc Huy', 0, '0988877665', 'Đà Nẵng', 2); -- Địa chỉ phụ của Phạm Quốc Huy
-- ==================== Categories ====================
INSERT INTO categories (category_id, name) VALUES
                                               (1, 'Sữa Rửa Mặt'),
                                               (2, 'Tẩy Trang'),
                                               (3, 'Toner');
INSERT INTO categories (category_id, name) VALUES
                                               (4, 'Mặt nạ'),
                                               (5, 'Serum'),
                                               (6, 'Kem chống nắng');

-- ==================== Brands ====================
INSERT INTO brands (brand_id, name, description, logo, is_active) VALUES
                                                                      (1, 'Cerave', 'Thương hiệu nổi tiếng với các sản phẩm dưỡng da và sữa rửa mặt dịu nhẹ.', 'https://res.cloudinary.com/defxwmvee/image/upload/v1764078865/cerave_s6y77k.jpg', true),
                                                                      (2, 'La Roche-Posay', 'Thương hiệu dược mỹ phẩm chuyên cho da nhạy cảm.', 'https://res.cloudinary.com/defxwmvee/image/upload/v1764078866/la_roche-posay_i2ek7w.jpg', true),
                                                                      (3, 'Cetaphil', 'Các sản phẩm chăm sóc da dịu nhẹ, thích hợp cho da nhạy cảm.', 'https://res.cloudinary.com/defxwmvee/image/upload/v1764078865/cetaphil_filwb5.jpg', true),
                                                                      (4, 'Simple', 'Mỹ phẩm dịu nhẹ, không hương liệu, phù hợp cho da nhạy cảm.', 'https://res.cloudinary.com/defxwmvee/image/upload/v1764078869/simple_dikm8v.jpg', true),
                                                                      (5, 'L\'Oreal', 'Thương hiệu mỹ phẩm quốc tế, nổi tiếng với các sản phẩm tẩy trang và trang điểm.', 'https://res.cloudinary.com/defxwmvee/image/upload/v1764078868/loreal_vkypsn.jpg', true),
(6, 'Cocoon', 'Mỹ phẩm thiên nhiên Việt Nam, tập trung vào chăm sóc da mặt.', 'https://res.cloudinary.com/defxwmvee/image/upload/v1764078865/cocoon_upit5a.jpg', true),
(7, 'Bioderma', 'Thương hiệu dược mỹ phẩm Pháp nổi tiếng với nước tẩy trang và chăm sóc da.', 'https://res.cloudinary.com/defxwmvee/image/upload/v1764078865/bioderma_ee64qi.jpg', true),
(8, 'Klairs', 'Thương hiệu Hàn Quốc chuyên sản phẩm dưỡng da dịu nhẹ và toner.', 'https://res.cloudinary.com/defxwmvee/image/upload/v1764078865/klairs_ymexas.jpg', TRUE),
(9, 'BNBG', 'BNBG là thương hiệu mỹ phẩm Hàn Quốc, ra mắt năm 2013. Nổi bật với mặt nạ dưỡng da như Jelly Mask, Skin Booster, Tea Tree Mask chứa collagen, tràm trà giúp cấp ẩm, giảm mụn, làm sáng da, mang lại làn da khỏe mịn.', 'https://res.cloudinary.com/defxwmvee/image/upload/v1764484444/bnbgjpg_s0icf9.jpg', TRUE);


-- ==================== Products ====================
-- Sữa Rửa Mặt Cerave
INSERT INTO products (product_id, name, description, category_id, brand_id, average_rating, is_active, created_at) VALUES
(1, 'Sữa Rửa Mặt CeraVe Sạch Sâu Cho Da Thường Đến Da Dầu', 'Sữa rửa mặt dịu nhẹ, làm sạch sâu cho da thường đến da dầu.', 1, 1, 4.5, true, NOW());

-- Sữa Rửa Mặt La Roche-Posay
INSERT INTO products (product_id, name, description, category_id, brand_id, average_rating, is_active, created_at) VALUES
(2, 'Gel Rửa Mặt La Roche-Posay Dành Cho Da Dầu, Nhạy Cảm', 'Gel rửa mặt làm sạch sâu, giảm bóng nhờn.', 1, 2, 4.7, true, NOW());

-- Sữa Rửa Mặt Cetaphil
INSERT INTO products (product_id, name, DESCRIPTION, category_id, brand_id, average_rating, is_active, created_at) VALUES
(3, 'Sữa Rửa Mặt Cetaphil Dịu Lành Cho Da Nhạy Cảm', 'Dịu nhẹ, không gây kích ứng, phù hợp da nhạy cảm.', 1, 3, 4.6, true, NOW());

-- Sữa Rửa Mặt Simple
INSERT INTO products (product_id, name, description, category_id, brand_id, average_rating, is_active, created_at) VALUES
(4, 'Sữa Rửa Mặt Simple Giúp Da Sạch Thoáng', 'Giúp da sạch thoáng, giảm bóng nhờn.', 1, 4, 4.3, true, NOW());

-- Tẩy Trang L'Oreal
INSERT INTO products (product_id, name, description, category_id, brand_id, average_rating, is_active, created_at) VALUES
(5, 'Nước Tẩy Trang L\'Oreal Làm Sạch Tươi Mát, Dưỡng Ẩm Cho Da 3-in-1 Micellar Water', 'Tẩy trang dịu nhẹ, làm sạch sâu.', 2, 5, 4.4, true, NOW());

-- Tẩy Trang Cocoon
INSERT INTO products (product_id, name, description, category_id, brand_id, average_rating, is_active, created_at) VALUES
(6, 'Nước Tẩy Trang Bí Đao Cocoon Làm Sạch & Giảm Dầu', 'Thành phần thiên nhiên, làm sạch & giảm dầu.', 2, 6, 4.5, true, NOW());

-- Tẩy Trang Bioderma
INSERT INTO products (product_id, name, description, category_id, brand_id, average_rating, is_active, created_at) VALUES
(7, 'Nước Tẩy Trang Bioderma Cho Da Dầu, Da Hỗn Hợp, Da Mụn Sebium H2O ', 'Dành cho da dầu, da hỗn hợp, da mụn, làm sạch dịu nhẹ.', 2, 7, 4.6, true, NOW());

-- Toner Cocoon
INSERT INTO products (product_id, name, description, category_id, brand_id, average_rating, is_active, created_at) VALUES
(8, 'Nước Cân Bằng Cocoon Sen Hậu Giang Làm Dịu Da Nhạy Cảm', 'Toner thiên nhiên, làm dịu da nhạy cảm.', 3, 6, 4.5, true, NOW());

-- Toner Klairs
INSERT INTO products (product_id, name, description, category_id, brand_id, average_rating, is_active, created_at) VALUES
(9, 'Nước Hoa Hồng Klairs Không Mùi Cho Da Nhạy Cảm', 'Dưỡng ẩm, cân bằng da nhạy cảm.', 3, 8, 4.6, true, NOW());

-- Toner Simple
INSERT INTO products (product_id, name, description, category_id, brand_id, average_rating, is_active, created_at) VALUES
(10, 'Nước Hoa Hồng Simple Làm Dịu, Cân Bằng Độ Ẩm Soothing Facial Toner', 'Dưỡng ẩm, làm dịu da.', 3, 4, 4.4, true, NOW());

-- Mặt nạ
INSERT INTO products (product_id, name, description, category_id, brand_id, average_rating, is_active, created_at) VALUES
(11, 'Mặt Nạ Rửa Cocoon Chiết Xuất Nghệ Hưng Yên Turmeric Face Mask', 'Mặt Nạ Nghệ Hưng Yên Cocoon Giúp Da Rạng Rỡ Mịn Màng là dòng mặt nạ rửa đến từ thương hiệu mỹ phẩm thuần chay Cocoon của Việt Nam.', 4, 6, 4.5, true, NOW()),
(12, 'Mặt Nạ Miếng BNBG Dưỡng Ẩm, Hỗ Trợ Dưỡng Trắng, Cải Thiện Làn Da Hàn Quốc (Vitamin A, B, B3, E)', 'Mặt Nạ Vitamin BNBG Vita Genic Jelly Mask là mặt nạ hỗ trợ cải thiện những vấn đề khác nhau trên da của thương hiệu BNBG có tác dụng cấp ẩm, hỗ trợ làm sáng da.', 4, 9, 4.5, true, NOW());

-- Serum
INSERT INTO products (product_id, name, description, category_id, brand_id, average_rating, is_active, created_at) VALUES
(13, 'Serum L\'Oreal Cấp Ẩm, Cải Thiện Nếp Nhăn Revitalift 1.5% Hyaluronic Acid Serum', 'Cấp ẩm, cải thiện nếp nhăn', 5, 5, 4.4, true, NOW()),
                                                                       (14, 'Serum L\'oreal Tinh Chất Mờ Thâm Nám Glycolic-Bright Instant Glowing Serum', 'Serum L\'oreal Tinh Chất Mờ Thâm Nám L\'oreal Glycolic-Bright Instant Glowing Serum là tinh chất dưỡng ẩm thuộc thương hiệu L\'Oreal đến từ Pháp.', 5, 5, 4.2, true, NOW()),
                                                                       (15, 'Serum La Roche-Posay Hỗ Trợ Phục Hồi Da, Cấp Ẩm Cho Da Hyalu B5 Serum 30ml', 'Serum La Roche-Posay Hỗ Trợ Phục Hồi Da, Cấp Ẩm Cho Da La Roche-Posay Hyalu B5 Serum là tinh chất với công thức giúp hỗ trợ phục hồi da gồm các thành phần Hyaluronic Acid nguyên chất, Vitamin B5 và Madecassoside, giúp làn da căng mịn, trở nên đàn hồi hơn, đồng thời giúp phục hồi hàng rào bảo vệ da thuộc thương hiệu dược mỹ phẩm La Roche-Posay đến từ Pháp.', 5, 2, 4.4, true, NOW()),
                                                                       (16, 'Serum La Roche-Posay Hỗ Trợ Dưỡng Sáng, Giảm Thâm Nám La Roche-Posay Mela B3 Serum 30ml', 'Serum La Roche-Posay Hỗ Trợ Dưỡng Sáng, Giảm Thâm Nám La Roche-Posay Mela B3 Serum 30ml là tinh chất dưỡng da thuộc thương hiệu La Roche-Posay. Với 10 hoạt chất đậm đặc trong cùng 1 sản phẩm hỗ trợ giảm thâm nám và dưỡng sáng, an toàn sử dụng cho mọi làn da.', 5, 2, 3.8, true, NOW()),
                                                                       (17, 'Serum Klairs Làm Dịu, Phục Hồi Da Klairs Midnight Blue Youth Activating Drop 20ml', 'Serum Klairs Làm Dịu, Phục Hồi Da Klairs Midnight Blue Youth Activating Drop 20ml là tinh chất dưỡng giúp nuôi dưỡng da từ sâu bên trong, mang lại làn da căng bóng khỏe mạnh. Công thức EGF, bFGF, và 95% nguyên liệu tự nhiên cho tác dụng ngăn ngừa lão hóa, tinh chất giúp cải thiện sức khỏe và chất lượng tổng thể của da thuộc thương hiệu Klairs đến từ Hàn Quốc.', 5, 8, 3.8, true, NOW()),
                                                                       (18, 'Serum Klairs Vitamin C Hỗ Trợ Dưỡng Sáng, Mờ Thâm Nám Dành Cho Da Dễ Kích Ứng Freshly Juiced Vitamin Drop 35ml', 'Serum KlairsVitamin C Hỗ Trợ Dưỡng Sáng, Mờ Thâm Nám Dành Cho Da Dễ Kích Ứng Klairs Freshly Juiced Vitamin Drop là tinh chất dưỡng chứa vitamin C tươi chiết xuất từ Cam, Chanh, Quýt cùng nhiều chiết xuất từ thực vật giúp hỗ trợ dưỡng sáng da, mờ thâm nám thuộc thương hiệu Klairs đến từ Hàn Quốc.', 5, 8, 5.0, true, NOW());

-- chống nắng
INSERT INTO products (product_id, name, description, category_id, brand_id, average_rating, is_active, created_at) VALUES
                                                                                                                       (19, 'Kem Chống Nắng La Roche-Posay Dạng Sữa, Bảo Vệ Da Khỏi UVA Dài, Ngăn Ngừa Thâm Nám Anthelios UVMune 400 Invisible Fluid SPF50+ 50ml', 'Là dòng sản phẩm kem chống nắng có khả năng chống nắng phổ rộng, bảo vệ da toàn diện trước tác hại từ tia UVA/UVB thuộc thương hiệu La Roche-Posay', 6, 2, 4.4, true, NOW()),
                                                                                                                       (20, 'Kem Chống Nắng L\'Oreal Mịn Nhẹ, Bảo Vệ Da, Ngăn Ngừa Lão Hóa UV Defender SPF 50+/PA++++', 'Kem chống nắng với 2 lớp màng bảo vệ (Màng lọc Mexoryl SX và Mexorul XL) giúp bảo vệ da dưới tác động của tia UVA và UVB cả ngày dài.', 6, 5, 4.4, true, NOW()),
(21, 'Sữa Chống Nắng Phổ Rộng L\'oreal Mỏng Nhẹ, Hỗ Trợ Dưỡng Sáng, Giảm Mờ Thâm Nám Glycolic-Bright Anti-Dark Spot Daily Uv Fluid SPF 50+/PA++++ 50ml', ' Là sữa chống nắng phổ rộng 2 trong 1 thế hệ mới với phức hợp Niacinamide và Glycolic Acid chính là thành phần chống nắng vừa bảo vệ làn da vừa hỗ trợ ngăn ngừa thâm nám để mang đến làn da đều màu và rạng rỡ.',6, 5, 4.4, true, NOW()),
                                                                                                                       (22, 'Kem Chống Nắng Bioderma Cho Da Dầu Mụn Photoderm AKN Mat SPF30 40ml', 'Kem Chống Nắng Cho Da Mụn Bioderma Photoderm AKN Mat SPF30 là dòng kem chống nắng đến từ thương hiệu dược mỹ phẩm Bioderma, sản phẩm mang lại hiệu quả 2 trong 1 chỉ số chống nắng cao và ngăn ngừa sự phát triển của mụn với kết cấu mịn màng, không màu, không bết dính hay để lại vệt trắng.',6, 7, 4.4, true, NOW());

--  product_images --
INSERT INTO product_images (product_id, image_url) VALUES
                                                       (1, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764080646/cerave_srm_1024x1024_rd0fqg.jpg'),
                                                       (2, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764080648/la_roche-posay_effaclar_lotion_astringente_200ml_1024x1024_umwouq.jpg'),
                                                       (3, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764080646/cetaphil_skin_cleanser_1024x1024_eaolc4.jpg'),
                                                       (4, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764080650/simple_srm_1024x1024_xli8og.jpg'),
                                                       (5, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764080649/loreal_micellar_1024x1024_pyaze0.jpg'),
                                                       (6, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764080646/cocoon_taytrang_1024x1024_gbwzhp.jpg'),
                                                       (7, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764080645/bioderma_taytrang_1024x1024_lc0yau.jpg'),
                                                       (8, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764080647/cocoon_winter_melon_tonner_1024x1024_dnctmg.jpg'),
                                                       (9, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764080649/klairs_tonner_1024x1024_q0m4g5.jpg'),
                                                       (10, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764080650/simple_tonner_1024x1024_gjdsi1.jpg'),
                                                       (11, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764485667/cocoon_turmeric_face_mask_1024x1024_vmr2ge.jpg'),
                                                       (12, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764485749/bgbg_1_1024x1024_qx5rxz.png'),
                                                       (12, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764485667/bgbg_2_1024x1024_j0n4oz.jpg');

INSERT INTO product_images (product_id, image_url) VALUES
                                                       (13, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764488758/loreal_serum_revitalift_1_1024x1024_qrlnk3.png'),
                                                       (13, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764488757/loreal_serum_revitalift_2_1024x1024_scv4xs.jpg'),
                                                       (13, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764488758/loreal_serum_revitalift_3_1024x1024_cdbblo.jpg'),
                                                       (13, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764488757/loreal_serum_revitalift_4_1024x1024_e4i2x4.jpg'),

                                                       (14, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764488757/loreal_serum_glycolic_bright_1_1024x1024_cgqhhw.png'),
                                                       (14, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764488757/loreal_serum_glycolic_bright_2_1024x1024_n3zd4t.jpg'),
                                                       (14, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764488757/loreal_serum_glycolic_bright_3_1024x1024_zex65k.jpg'),

                                                       (15, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764489708/la_roche-posay_serum_b5_30ml_1_1024x1024_bpyksa.png'),
                                                       (15, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764489707/la_roche-posay_serum_b5_30ml_2_1024x1024_prcpyj.jpg'),
                                                       (15, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764489711/la_roche-posay_serum_b5_30ml_3_1024x1024_cf88ps.jpg'),

                                                       (16, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764489707/la_roche-posay_serum_b3_30ml_1_1024x1024_spknlj.png'),
                                                       (16, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764489706/la_roche-posay_serum_b3_30ml_2_1024x1024_i9sqpp.jpg'),
                                                       (16, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764489706/la_roche-posay_serum_b3_30ml_3_1024x1024_diiy3h.jpg'),

                                                       (17, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764490451/klairs_serum_midnight_1_1024x1024_tb5pkd.jpg'),
                                                       (17, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764490452/klairs_serum_midnight_2_1024x1024_vgnmoi.png'),
                                                       (17, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764490452/klairs_serum_midnight_3_1024x1024_xsww0a.jpg'),

                                                       (18, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764490453/klairs_serum_vitamin_c_1_1024x1024_mluig5.jpg'),
                                                       (18, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764490477/klairs_serum_vitamin_c_2_1024x1024_oi737a.jpg'),
-- kcn la roche
                                                       (19, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764492788/la_roche-posay_kcn_1_1024x1024_vzmwly.png'),
                                                       (19, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764492787/la_roche-posay_kcn_2_1024x1024_rk718b.jpg'),
                                                       (19, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764492786/la_roche-posay_kcn_3_1024x1024_k19vy1.jpg'),
                                                       (19, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764492785/la_roche-posay_kcn_4_1024x1024_quqgld.jpg'),
-- kcn loreal
                                                       (20, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764492784/loreal_kcn_1_1024x1024_lhklkp.png'),
                                                       (20, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764492777/loreal_kcn_2_1024x1024_ypue0h.jpg'),
                                                       (21, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764492758/loreal_kcn_glycolic_bright_1_1024x1024_efgmxk.png'),
                                                       (21, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764492758/loreal_kcn_glycolic_bright_1_1024x1024_efgmxk.png'),

                                                       (22, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764492756/bioderma_kcn_1_1024x1024_juriwr.jpg');

-- ==================== Product Variants ====================
-- Cerave variants
INSERT INTO product_variants (variant_id, variant_name, price, quantity, product_id) VALUES
                                                                                         (1, '473ml', 439000, 100, 1),
                                                                                         (2, '236ml', 309000, 100, 1),
                                                                                         (3, '88ml', 155000, 100, 1);

-- La Roche-Posay variants
INSERT INTO product_variants (variant_id, variant_name, price, quantity, product_id) VALUES
                                                                                         (4, '400ml', 525000, 80, 2),
                                                                                         (5, '200ml', 372000, 100, 2);

-- Cetaphil variants
INSERT INTO product_variants (variant_id, variant_name, price, quantity, product_id) VALUES
                                                                                         (6, '500ml', 349000, 100, 3),
                                                                                         (7, '1000ml', 680000, 80, 3),
                                                                                         (8, '125ml', 139000, 50, 3);

-- Simple srm variants
INSERT INTO product_variants (variant_id, variant_name, price, quantity, product_id) VALUES
    (9, '150ml', 90000, 100, 4);

-- L'Oreal variants
INSERT INTO product_variants (variant_id, variant_name, price, quantity, product_id) VALUES
                                                                                         (10, 'Tươi Mát - 400ml', 199000, 100, 5),
                                                                                         (11, 'Dưỡng Ẩm - 400ml', 199000, 100, 5),
                                                                                         (12, 'Sạch Sâu - 400ml', 182000, 100, 5);

-- Cocoon variants
INSERT INTO product_variants (variant_id, variant_name, price, quantity, product_id) VALUES
                                                                                         (13, '500ml', 236000, 90, 6),
                                                                                         (14, '140ml', 130000, 100, 6),
-- toner
                                                                                         (15, '310ml', 265000, 80, 8),
                                                                                         (16, '140ml', 175000, 80, 8);

-- Bioderma variants
INSERT INTO product_variants (variant_id, variant_name, price, quantity, product_id) VALUES
                                                                                         (17, '500ml', 409000, 100, 7),
                                                                                         (18, '250ml', 389000, 90, 7),
                                                                                         (19, '100ml', 209000, 70, 7);

-- Klairs variants
INSERT INTO product_variants (variant_id, variant_name, price, quantity, product_id) VALUES
    (20, '180ml', 210000, 100, 9);


-- Simple Toner variants
INSERT INTO product_variants (variant_id, variant_name, price, quantity, product_id) VALUES
    (21, '200ml', 158000, 100, 10);

-- Coccon mask variants
INSERT INTO product_variants (variant_id, variant_name, price, quantity, product_id) VALUES
                                                                                         (22, '30ml', 130000, 100, 11),
                                                                                         (23, '100ml', 310000, 90, 11),
                                                                                         (24, '30ml tặng 30ml', 179000, 90, 11);


-- BNBG mask variants
INSERT INTO product_variants (variant_id, variant_name, price, quantity, product_id) VALUES
                                                                                         (25, 'VITAMIN C (1 MIẾNG) (BẢN HÀN)', 18000, 100, 12),
                                                                                         (26, 'VITAMIN A (1 MIẾNG) (BẢN HÀN)', 18000, 90, 12),
                                                                                         (27, 'VITAMIN B3 (1 MIẾNG) (BẢN VIỆT)', 18000, 90, 12),
                                                                                         (28, 'VITAMIN B (1 MIẾNG) (BẢN VIỆT)', 18000, 90, 12),
                                                                                         (29, 'VITAMIN A (1 MIẾNG) (BẢN VIỆT)', 18000, 90, 12),
                                                                                         (30, 'VITAMIN E (1 MIẾNG) (BẢN VIỆT)', 18000, 90, 12);

-- Serum variants
INSERT INTO product_variants (variant_id, variant_name, price, quantity, product_id) VALUES
                                                                                         (31, '30ml', 499000, 100, 13),
                                                                                         (32, '15ml', 299000, 90, 13),

                                                                                         (33, '30ml', 499000, 100, 14),
                                                                                         (34, '15ml', 299000, 90, 14),

                                                                                         (35, '30ml', 1350000, 90, 15),
                                                                                         (36, '30ml', 1550000, 90, 16),

                                                                                         (37, '20ml', 1350000, 90, 17),
                                                                                         (38, '35ml', 1550000, 90, 18),
-- kcn
                                                                                         (39, '50ml', 640000, 90, 19),
                                                                                         (40, '50ML TẶNG NƯỚC TẨY TRANG 50ML', 449000, 90, 19),

                                                                                         (41, '50ML BRIGHT & CLEAR', 399000, 90, 20),
                                                                                         (42, '50ML MATTE & FRESH', 399000, 90, 20),
                                                                                         (43, '50ML INVISIBLE FLUID', 399000, 90, 20),
                                                                                         (44, '50ML AQUAFUSION', 409000, 90, 20),

                                                                                         (45, '50ML', 399000, 90, 21),
                                                                                         (46, '40ML', 399000, 90, 22);


-- product_variant_images--
INSERT INTO product_variant_images (variant_id, image_url) VALUES
                                                               (1, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764083529/cerave_foaming_cleanser_473ml_1024x1024_y89pjx.jpg'),
                                                               (2, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764083528/cerave_foaming_cleanser_236ml_1024x1024_csnrbk.jpg'),
                                                               (3, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764083526/cerave_foaming_cleanser_88ml_1024x1024_rfn7ft.jpg'),

                                                               (4, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764083548/la_roche-posay_effaclar_purifying_foaming_gel_for_oily_sensitive_skin_400ml_1024x1024_kie9pt.jpg'),
                                                               (5, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764083548/la_roche-posay_effaclar_purifying_foaming_gel_for_oily_sensitive_skin_200ml_1024x1024_ujpkgl.jpg'),

                                                               (6, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764083540/cetaphil_srm_500ml_1024x1024_fksjvp.jpg'),
                                                               (7, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764083538/cetaphil_srm_1lit_1024x1024_lx8jhw.jpg'),
                                                               (8, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764083539/cetaphil_srm_125ml_1024x1024_uukwp7.jpg'),

                                                               (9, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764083554/simple_srm_150ml_1024x1024_plbtev.jpg'),

                                                               (10, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764083553/loreal_micellar_water_tuoimat_400ml_1024x1024_mvbytc.jpg'),
                                                               (11, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764083550/loreal_micellar_water_duongam_400ml_1024x1024_fmvb28.jpg'),
                                                               (12, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764083551/loreal_micellar_water_sachsau_400ml_1024x1024_dgufrb.jpg'),

                                                               (13, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764083542/cocoon_taytrang_500ml_1024x1024_felpqy.jpg'),
                                                               (14, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764083541/cocoon_taytrang_140ml_1024x1024_ap2a1w.jpg'),

                                                               (15, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764083545/cocoon_tonner_310ml_1024x1024_g6jfra.jpg'),
                                                               (16, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764083543/cocoon_tonner_140ml_1024x1024_adxark.jpg'),

                                                               (17, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764083526/bioderma_taytrang_500ml_1024x1024_ds6w4a.jpg'),
                                                               (18, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764083525/bioderma_taytrang_250ml_1024x1024_ytugsc.jpg'),
                                                               (19, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764083525/bioderma_taytrang_100ml_1024x1024_znrmgk.jpg'),

                                                               (20, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764083546/klairs_tonner_180ml_1024x1024_zrg1wn.jpg'),

                                                               (21, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764083556/sipmle_tonner_200ml_1024x1024_nenber.jpg');

INSERT INTO product_variant_images (variant_id, image_url) VALUES
                                                               (22, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764486002/cocoon_mask_30ml_1024x1024_khelib.jpg'),
                                                               (23, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764486004/cocoon_mask_100ml_1024x1024_dwvk0k.jpg'),
                                                               (24, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764486003/cocoon_mask_30ml_tang_30ml_1024x1024_kyynls.png');


INSERT INTO product_variant_images (variant_id, image_url) VALUES
                                                               (25, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764486561/bnbg_vitamin_c_han_1024x1024_jj3aul.jpg'),
                                                               (26, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764486561/bnbg_vitamin_a_han_1024x1024_ykrrje.jpg'),
                                                               (27, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764486561/bnbg_vitamin_b3_1024x1024_tfuizc.jpg'),
                                                               (28, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764486561/bnbg_vitamin_b_1024x1024_ytwbvi.jpg'),
                                                               (29, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764486561/bnbg_vitamin_a_1024x1024_qqsnyd.jpg'),
                                                               (30, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764486562/bnbg_vitamin_e_1024x1024_g0zz33.jpg');


INSERT INTO product_variant_images (variant_id, image_url) VALUES
                                                               (31, 'hhttps://res.cloudinary.com/defxwmvee/image/upload/v1764489182/loreal_serum_revitalift_30ml_1024x1024_xfkqxn.jpg'),
                                                               (32, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764489182/loreal_serum_revitalift_15ml_1024x1024_d2j024.jpg'),

                                                               (33, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764489182/loreal_serum_glycolic_bright_30ml_1024x1024_zyivzk.jpg'),
                                                               (34, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764489181/loreal_serum_glycolic_bright_15ml_1024x1024_vuxeye.jpg'),

                                                               (35, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764490071/la_roche-posay_serum_b5_30ml_1024x1024_bvg7cu.jpg'),
                                                               (36, 'hhttps://res.cloudinary.com/defxwmvee/image/upload/v1764490072/la_roche-posay_serum_b3_30ml_1024x1024_m0trnh.jpg'),

                                                               (37, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764490708/klairs_serum_midnight_20ml_1024x1024_cchcw6.jpg'),
                                                               (38, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764490708/klairs_serum_vitamin_c_35ml_1024x1024_nitvyb.jpg'),
-- kcn
                                                               (39, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764493671/la_roche-posay_kcn_50ml_1024x1024_gfuyzy.jpg'),
                                                               (40, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764493671/la_roche-posay_kcn_50ml_tang_nuoctaytrang_1024x1024_hodu97.jpg'),

                                                               (41, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764493236/loreal_kcn_bright_clear_50ml_1024x1024_x8vjkc.jpg'),
                                                               (42, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764493211/loreal_kcn_matte_fresh_50ml_1024x1024_gqfrmo.jpg'),
                                                               (43, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764493209/loreal_kcn_invisible_50ml_1024x1024_zqccyc.jpg'),
                                                               (44, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764493207/loreal_kcn_aquafusion_50ml_1024x1024_cfetga.jpg'),

                                                               (45, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764493204/loreal_kcn_glycolic_bright_50ml_1024x1024_cnekff.jpg'),
                                                               (46, 'https://res.cloudinary.com/defxwmvee/image/upload/v1764493185/bioderma_kcn_40ml_1024x1024_qmrq24.jpg');

-- ==================== Carts ====================
INSERT INTO carts (cart_id, total_price, customer_id) VALUES
                                                          (1, 748000, 1), -- Giỏ của Trần Thị Bích
                                                          (2, 598000, 2); -- Giỏ của Phạm Quốc Huy

-- ==================== Cart Items ====================
INSERT INTO cart_items (id, quantity, sub_total, cart_id, variant_id) VALUES
                                                                          (1, 1, 439000, 1, 1), -- Trần Thị Bích: 1 x Cerave 473ml
                                                                          (2, 1, 309000, 1, 2), -- Trần Thị Bích: 1 x Cerave 236ml
                                                                          (3, 1, 155000, 2, 3), -- Phạm Quốc Huy: 1 x Cerave 88ml
                                                                          (4, 2, 372000, 2, 5); -- Phạm Quốc Huy: 2 x La Roche-Posay 200ml

/*
-- Bảng: customer_wishlist (Liên kết với customers, product_variants)
INSERT INTO `customer_wishlist` (`customer_id`, `product_variant_id`) VALUES
(1, 6), -- Khách 1 thích Nước hoa Chanel
(1, 5), -- Khách 1 thích Son Velvet Teddy
(2, 1); -- Khách 2 thích Serum 30ml
*/
-- ==================== Orders ====================
INSERT INTO orders (order_id, cancel_reason, canceled_at, order_date, shipping_fee, status, total, address_id, customer_id, employee_id) VALUES
                                                                                                                                             (1, NULL, NULL, '2025-11-16 10:00:00.000000', 15000, 'CONFIRMED', 758000, 1, 1, NULL), -- Trần Thị Bích, đơn 1
                                                                                                                                             (2, NULL, NULL, '2025-11-16 11:00:00.000000', 15000, 'PROCESSING', 559000, 2, 2, NULL), -- Phạm Quốc Huy, đơn 1
                                                                                                                                             (3, NULL, NULL, '2025-11-17 09:30:00.000000', 15000, 'SHIPPING', 617000, 1, 1, NULL), -- Trần Thị Bích, đơn 2
                                                                                                                                             (4, NULL, NULL, '2025-11-17 10:15:00.000000', 15000, 'DELIVERED', 780000, 2, 2, NULL), -- Phạm Quốc Huy, đơn 2
                                                                                                                                             (5, 'Khách hủy đơn', '2024-01-18 14:00:00.000000', '2024-01-18 13:45:00.000000', 15000, 'CANCELLED', 395000, 1, 1, NULL); -- Trần Thị Bích, đơn 3 hủy

-- ==================== Order Details ====================
INSERT INTO order_details (order_detail_id, discount_amount, quantity, total_price, unit_price, order_id, variant_id) VALUES
-- Đơn 1 - Trần Thị Bích
(1, 0, 1, 439000, 439000, 1, 1), -- Cerave 473ml
(2, 0, 1, 309000, 309000, 1, 2), -- Cerave 236ml
(3, 0, 1, 10000, 10000, 1, 9), -- Simple 150ml

-- Đơn 1 - Phạm Quốc Huy
(4, 0, 1, 349000, 349000, 2, 6), -- Cetaphil 500ml
(5, 0, 1, 210000, 210000, 2, 20), -- Klairs Toner 180ml

-- Đơn 2 - Trần Thị Bích
(6, 0, 1, 409000, 409000, 3, 17), -- Bioderma 500ml
(7, 0, 1, 236000, 236000, 3, 13), -- Cocoon 500ml

-- Đơn 2 - Phạm Quốc Huy
(8, 0, 1, 372000, 372000, 4, 5), -- La Roche-Posay 200ml
(9, 0, 1, 408000, 408000, 4, 18), -- Bioderma 250ml

-- Đơn 3 - Trần Thị Bích (hủy)
(10, 0, 1, 209000, 209000, 5, 19), -- Bioderma 100ml
(11, 0, 1, 186000, 186000, 5, 12); -- L'Oreal 400ml Làm Sạch
/*
INSERT INTO `orders` (`cancel_reason`, `canceled_at`, `order_date`, `status`, `total`, `customer_id`, `employee_id`, `shipping_fee`, `address_id`) VALUES
-- ORDERS GỐC (1-8, có áp dụng Voucher)
(NULL, NULL, '2025-03-20 09:00:00.000000', 'DELIVERED', 480000.00, 1, 1, 30000.00, 1), -- Subtotal 550k (Discount 100k) -> Total 480k
(NULL, NULL, '2025-03-21 10:00:00.000000', 'CONFIRMED', 390000.00, 2, 2, 30000.00, 2), -- Subtotal 400k (Discount 40k) -> Total 390k
(NULL, NULL, '2025-03-22 12:00:00.000000', 'PROCESSING', 560000.00, 3, 3, 30000.00, 3), -- Subtotal 550k (Discount 20k) -> Total 560k
(NULL, NULL, '2025-03-31 13:00:00.000000', 'SHIPPING', 580000.00, 4, 1, 30000.00, 4), -- Subtotal 750k (Discount 200k) -> Total 580k
(NULL, NULL, '2025-04-01 08:30:00.000000', 'DELIVERED', 180000.00, 5, 2, 30000.00, 5), -- Subtotal 150k (No Discount) -> Total 180k
(NULL, NULL, '2025-04-02 15:00:00.000000', 'DELIVERED', 285000.00, 6, 3, 30000.00, 6), -- Subtotal 300k (Discount 45k) -> Total 285k
(NULL, NULL, '2025-04-04 16:00:00.000000', 'CONFIRMED', 510000.00, 1, 1, 30000.00, 1), -- Subtotal 500k (Discount 20k) -> Total 510k
(NULL, NULL, '2025-04-05 11:30:00.000000', 'PENDING', 165000.00, 2, 2, 30000.00, 2), -- Subtotal 150k (Discount 15k) -> Total 165k
-- ORDERS MỚI (9-20, KHÔNG áp dụng Voucher)
(NULL, NULL, '2025-05-10 10:00:00.000000', 'CONFIRMED', 779000.00, 3, 1, 30000.00, 3), -- Subtotal 749k -> Total 779k
(NULL, NULL, '2025-05-11 11:30:00.000000', 'PROCESSING', 927000.00, 4, 2, 30000.00, 4), -- Subtotal 897k -> Total 927k
(NULL, NULL, '2025-05-12 14:00:00.000000', 'DELIVERED', 1077000.00, 5, 3, 30000.00, 5), -- Subtotal 1,047k -> Total 1,077k
(NULL, NULL, '2025-05-13 09:15:00.000000', 'PENDING', 480000.00, 6, 1, 30000.00, 6), -- Subtotal 450k -> Total 480k
(NULL, NULL, '2025-05-14 16:30:00.000000', 'CONFIRMED', 610000.00, 1, 2, 30000.00, 1), -- Subtotal 580k -> Total 610k
(NULL, NULL, '2025-05-15 17:45:00.000000', 'SHIPPING', 526000.00, 2, 3, 30000.00, 2), -- Subtotal 496k -> Total 526k
(NULL, NULL, '2025-05-16 12:00:00.000000', 'DELIVERED', 439000.00, 3, 1, 30000.00, 3), -- Subtotal 409k -> Total 439k
(NULL, NULL, '2025-05-17 10:10:00.000000', 'PROCESSING', 470000.00, 4, 2, 30000.00, 4), -- Subtotal 440k -> Total 470k
(NULL, NULL, '2025-05-18 15:20:00.000000', 'CONFIRMED', 808000.00, 5, 3, 30000.00, 5), -- Subtotal 778k -> Total 808k
(NULL, NULL, '2025-05-19 11:50:00.000000', 'DELIVERED', 660000.00, 6, 1, 30000.00, 6), -- Subtotal 630k -> Total 660k
(NULL, NULL, '2025-05-20 08:00:00.000000', 'PENDING', 785000.00, 1, 2, 30000.00, 1), -- Subtotal 755k -> Total 785k
(NULL, NULL, '2025-05-21 14:30:00.000000', 'CONFIRMED', 826000.00, 2, 3, 30000.00, 2); -- Subtotal 796k -> Total 826k

-- ==================== Order_detail ====================
INSERT INTO `order_details` (`quantity`, `total_price`, `unit_price`, `order_id`, `variant_id`, `discount_amount`) VALUES
(2, 300000.00, 150000.00, 1, 1, NULL), (1, 250000.00, 250000.00, 1, 2, NULL), -- Order 1
(2, 400000.00, 200000.00, 2, 3, NULL), -- Order 2
(1, 250000.00, 250000.00, 3, 5, NULL), (2, 300000.00, 150000.00, 3, 1, NULL), -- Order 3
(3, 750000.00, 250000.00, 4, 5, NULL), -- Order 4
(1, 150000.00, 150000.00, 5, 1, NULL), -- Order 5
(3, 300000.00, 100000.00, 6, 4, NULL), -- Order 6
(2, 500000.00, 250000.00, 7, 2, NULL), -- Order 7
(1, 150000.00, 150000.00, 8, 1, NULL), -- Order 8

(1, 439000.00, 439000.00, 9, 1, NULL), (2, 310000.00, 155000.00, 9, 3, NULL), -- Order 9
(1, 525000.00, 525000.00, 10, 4, NULL), (1, 372000.00, 372000.00, 10, 5, NULL), -- Order 10
(3, 1047000.00, 349000.00, 11, 6, NULL), -- Order 11
(5, 450000.00, 90000.00, 12, 9, NULL), -- Order 12
(2, 398000.00, 199000.00, 13, 10, NULL), (1, 182000.00, 182000.00, 13, 12, NULL), -- Order 13
(1, 236000.00, 236000.00, 14, 13, NULL), (2, 260000.00, 130000.00, 14, 14, NULL), -- Order 14
(1, 409000.00, 409000.00, 15, 17, NULL), -- Order 15
(1, 265000.00, 265000.00, 16, 15, NULL), (1, 175000.00, 175000.00, 16, 16, NULL), -- Order 16
(2, 778000.00, 389000.00, 17, 18, NULL), -- Order 17
(3, 630000.00, 210000.00, 18, 20, NULL), -- Order 18
(2, 316000.00, 158000.00, 19, 21, NULL), (1, 439000.00, 439000.00, 19, 1, NULL), -- Order 19
(4, 796000.00, 199000.00, 20, 11, NULL); -- Order 20
*/
-- ==================== Voucher ====================
INSERT INTO vouchers
(voucher_id, code, type, value, max_discount, min_order_amount,
 max_uses, per_user_limit, is_stackable, start_at, end_at, status,
 scope, brand_id, category_id, created_at, updated_at)
VALUES
    (1, 'WELCOME10', 'PERCENT', 10, 50000, 0, 500, 1, b'1', '2025-01-01 00:00:00', '2025-12-31 23:59:59', 'ACTIVE', 'GLOBAL', NULL, NULL, '2025-01-01 00:00:00', '2025-01-01 00:00:00'),
    (2, 'SKIN50', 'AMOUNT', 50000, 50000, 300000, 200, 1, b'0', '2025-03-01 00:00:00', '2025-05-30 23:59:59', 'ACTIVE', 'CATEGORY', NULL, 3, '2025-03-01 00:00:00', '2025-03-01 00:00:00'),
    (3, 'BRAND20', 'PERCENT', 20, 20000, 200000, 999, 2, b'1', '2025-02-01 00:00:00', '2025-07-01 23:59:59', 'ACTIVE', 'BRAND', 5, NULL, '2025-02-01 00:00:00', '2025-02-01 00:00:00'),
    (4, 'VIP100', 'AMOUNT', 100000, 100000, 500000, 100, 1, b'0', '2025-01-01 00:00:00', '2025-12-31 23:59:59', 'ACTIVE', 'GLOBAL', NULL, NULL, '2025-01-01 00:00:00', '2025-01-01 00:00:00'),
    (5, 'FLASH10', 'PERCENT', 10, 30000, 200000, 300, 1, b'1', '2025-06-01 00:00:00', '2025-06-10 23:59:59', 'EXPIRED', 'GLOBAL', NULL, NULL, '2025-06-01 00:00:00', '2025-06-01 00:00:00'),
    (6, 'SUMMER15', 'PERCENT', 15, 70000, 400000, 500, 1, b'1', '2025-05-01 00:00:00', '2025-08-01 23:59:59', 'ACTIVE', 'CATEGORY', NULL, 2, '2025-05-01 00:00:00', '2025-05-01 00:00:00'),
    (7, 'WINTER30', 'AMOUNT', 30000, 30000, 600000, 300, 1, b'0', '2025-11-01 00:00:00', '2026-02-01 23:59:59', 'ACTIVE', 'GLOBAL', NULL, NULL, '2025-11-01 00:00:00', '2025-11-01 00:00:00'),
    (8, 'BRANDVIP', 'PERCENT', 5, 15000, 250000, 200, 1, b'1', '2025-03-05 00:00:00', '2025-09-30 23:59:59', 'ACTIVE', 'BRAND', 7, NULL, '2025-03-05 00:00:00', '2025-03-05 00:00:00'),
    (9, 'PERSONAL200', 'AMOUNT', 200000, 200000, 700000, 50, 1, b'0', '2025-02-10 00:00:00', '2025-12-31 23:59:59', 'ACTIVE', 'GLOBAL', NULL, NULL, '2025-02-10 00:00:00', '2025-02-10 00:00:00'),
    (10, 'NEWUSER15', 'PERCENT', 15, 40000, 0, 1000, 1, b'1', '2025-01-01 00:00:00', '2025-12-31 23:59:59', 'ACTIVE', 'GLOBAL', NULL, NULL, '2025-01-01 00:00:00', '2025-01-01 00:00:00');

/*
-- ==================== Customer_voucher ====================
INSERT INTO customer_voucher
(customer_voucher_id, customer_id, voucher_id, claimed_at, is_claimed, is_used)
VALUES
(1, 101, 4, '2025-01-05 00:00:00', b'1', b'0'),
(2, 105, 4, '2025-01-06 00:00:00', b'1', b'1'),
(3, 101, 3, '2025-03-01 00:00:00', b'1', b'0'),
(4, 110, 9, '2025-02-15 00:00:00', b'1', b'0'),
(5, 110, 1, '2025-01-10 00:00:00', b'1', b'0'),
(6, 115, 4, '2025-02-11 00:00:00', b'1', b'0'),
(7, 115, 9, '2025-02-18 00:00:00', b'1', b'0'),
(8, 120, 3, '2025-03-05 00:00:00', b'1', b'1'),
(9, 130, 4, '2025-01-20 00:00:00', b'1', b'0'),
(10, 130, 1, '2025-01-22 00:00:00', b'1', b'0');


-- ==================== voucher_redemptions ====================
INSERT INTO voucher_redemptions
(voucher_redemption_id, voucher_id, order_id, customer_id, amount_discounted, redeemed_at)
VALUES
(1, 4, 501, 101, 100000, '2025-03-21 10:22:00'),
(2, 1, 502, NULL, 20000, '2025-03-22 11:15:00'),
(3, 3, 503, 105, 15000, '2025-03-22 13:40:00'),
(4, 9, 504, 110, 200000, '2025-04-01 14:12:00'),
(5, 4, 505, 115, 100000, '2025-04-02 09:45:00'),
(6, 10, 506, NULL, 30000, '2025-04-03 16:00:00'),
(7, 3, 507, 101, 20000, '2025-04-05 17:30:00'),
(8, 1, 508, 120, 15000, '2025-04-06 12:55:00'),
(9, 4, 509, 130, 100000, '2025-04-06 14:25:00'),
(10, 1, 510, 130, 10000, '2025-04-07 18:10:00');
*/
-- ==================== Customer Vouchers ====================
INSERT INTO customer_voucher (customer_id, voucher_id, claimed_at, is_claimed, is_used)
VALUES
    (1, 2, '2025-11-28 10:30:00', b'1', b'0'), -- Khách hàng 1 nhận voucher 2
    ( 1, 3, '2025-11-28 10:30:00', b'1', b'0'), -- Khách hàng 1 nhận voucher 3
    ( 2, 1, '2025-11-27 15:00:00', b'1', b'0'), -- Khách hàng 2 nhận voucher 1
    (2, 4, '2025-11-27 15:00:00', b'1', b'0'); -- Khách hàng 2 nhận voucher 4


-- ==================== Voucher Redemptions ====================
-- Chỉ các đơn không hủy mới được dùng voucher
INSERT INTO voucher_redemptions
(voucher_id, customer_id, order_id, amount_discounted, redeemed_at)
VALUES
    (1, 1, 1, 100000, '2025-01-16 10:00:00.000000'),
    (4, 1, 3, 20000, '2025-01-17 09:30:00.000000'),
    (7, 2, 2, 15000, '2025-01-16 11:00:00.000000'),
    (9, 2, 4, 200000, '2025-01-17 10:15:00.000000');
