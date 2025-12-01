/*
HƯỚNG DẪN TEST API BRAND & REVIEW
Author: Nguyễn Thành Nhân
------------------------------

I. TỔNG QUAN CÁC API

1. Brand API (Quản lý thương hiệu):
- CRUD cơ bản (Thêm/Xem/Sửa/Xóa)
- Tìm kiếm theo tên
- Kích hoạt/vô hiệu hóa thương hiệu

2. Review API (Quản lý đánh giá):
- CRUD cơ bản
- Tìm theo khách hàng/sản phẩm
- Lọc theo rating
- Tính điểm trung bình
- Ẩn/hiện đánh giá

II. DỮ LIỆU MẪU TEST
*/

-- Them du lieu mau cho Brand
INSERT INTO brands (name, description, logo, is_active) VALUES 
('L''Oréal', 'Thương hiệu mỹ phẩm số 1 thế giới', 'loreal-logo.png', true),
('Maybelline', 'Maybe she''s born with it, maybe it''s Maybelline', 'maybelline-logo.png', true),
('MAC Cosmetics', 'Make-up Art Cosmetics', 'mac-logo.png', true),
('Innisfree', 'Thương hiệu mỹ phẩm tự nhiên từ đảo Jeju', 'innisfree-logo.png', true),
('The Face Shop', 'Mỹ phẩm từ thiên nhiên Hàn Quốc', 'faceshop-logo.png', true);

-- Them du lieu mau cho Review (gia su co san Customer va Product)
INSERT INTO reviews (customer_id, product_id, rating, comment, review_date, active) VALUES 
(1, 1, 5, 'Sản phẩm rất tốt, da mịn và sáng hơn', '2025-11-02 10:00:00', true),
(2, 1, 4, 'Chất lượng ok, giá hơi cao', '2025-11-02 11:30:00', true),
(1, 2, 3, 'Sản phẩm tạm được, cần cải thiện thêm', '2025-11-02 14:15:00', true),
(3, 3, 5, 'Hoàn toàn hài lòng với sản phẩm này', '2025-11-02 15:45:00', true),
(2, 4, 4, 'Sẽ mua lại lần sau', '2025-11-02 16:20:00', true);

/*
III. HƯỚNG DẪN TEST API

A. BRAND API

1. Tạo thương hiệu mới:
   POST http://localhost:8080/api/brands
   Body:
   {
       "name": "Test Brand",
       "description": "Thương hiệu test",
       "logo": "test-logo.png"
   }

2. Xem danh sách:
   GET http://localhost:8080/api/brands

3. Xem chi tiết:
   GET http://localhost:8080/api/brands/1

4. Cập nhật:
   PUT http://localhost:8080/api/brands/1
   Body:
   {
       "name": "Updated Brand",
       "description": "Mô tả cập nhật",
       "logo": "new-logo.png"
   }

5. Tìm kiếm:
   GET http://localhost:8080/api/brands/search?name=test

6. Vô hiệu hóa:
   POST http://localhost:8080/api/brands/1/deactivate

B. REVIEW API

1. Tạo đánh giá:
   POST http://localhost:8080/api/reviews
   Body:
   {
       "customer": {"id": 1},
       "product": {"id": 1},
       "rating": 5,
       "comment": "Sản phẩm rất tốt"
   }

2. Xem danh sách:
   GET http://localhost:8080/api/reviews

3. Xem theo sản phẩm:
   GET http://localhost:8080/api/reviews/product/1

4. Lọc theo rating:
   GET http://localhost:8080/api/reviews/rating/5

5. Xem rating trung bình:
   GET http://localhost:8080/api/reviews/product/1/average

IV. LƯU Ý 

1. Đảm bảo đã có:
- Database đã được tạo
- Các bảng liên quan đã tồn tại (brands, reviews, customers, products)
- Server chạy ở port 8080

2. Thứ tự test:
- Test CRUD Brand trước
- Sau đó mới test CRUD Review (vì Review cần Product ID)
- Cuối cùng test các tính năng còn lại

3. Xử lý lỗi phổ biến:
- 404: Không tìm thấy resource
- 400: Dữ liệu gửi lên không hợp lệ
- 500: Lỗi server (check log)

*/