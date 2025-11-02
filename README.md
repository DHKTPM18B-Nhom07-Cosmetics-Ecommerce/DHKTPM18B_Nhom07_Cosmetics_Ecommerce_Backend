<<<<<<< HEAD
# Hệ Thống Backend Cửa Hàng Mỹ Phẩm

## 📚 Môn học: Lập trình WWW (Java)
```
DHKTPM18B - 420300362102
GV: Đặng Thị Thu Hà
```

## 🎯 Đề tài: Xây dựng website kinh doanh mỹ phẩm

## 🤝 Thành viên nhóm 07
- Nguyễn Ngọc Hồng Minh (Trưởng nhóm) - 22690581
- Lê Mẫn Nghi - 22652921
- Nguyễn Thị Mỹ Hoa - 22643291
- Trần Công Minh - 22638121
- Nguyễn Thành Nhân - 21090991
- Nguyễn Kim Toàn - 22003435
- Huỳnh Lệ Hoan - 22682941
- Nguyễn Thị Kiều Trang -22642451

## 🌟 Giới Thiệu
Hệ thống backend cho website kinh doanh mỹ phẩm, cung cấp API cho việc quản lý sản phẩm, đơn hàng, người dùng và các tính năng khác. Được xây dựng với Spring Boot, hệ thống đảm bảo hiệu suất cao và bảo mật tốt.

## 🚀 Công Nghệ Sử Dụng
- **Core**: Java 21
- **Framework**: Spring Boot 3.1.5
- **Security**: Spring Security 6.1.5
- **Database**: MySQL 8.0, Spring Data JPA
- **Build Tool**: Maven 3.9.5
- **Documentation**: Swagger/OpenAPI
- **Cloud**: Cloundinary (cho lưu trữ hình ảnh)
- **CI/CD**: GitHub Actions

## 🔥 Tính Năng Chính
- 🛍️ Quản lý sản phẩm và danh mục
- 👥 Quản lý người dùng và phân quyền
- 🛒 Xử lý đơn hàng và thanh toán
- 📊 Báo cáo và thống kê
- 🔒 Xác thực và bảo mật JWT
- 📸 Upload và quản lý hình ảnh
- 📧 Gửi email thông báo

## 📚 Dependencies Chính

```xml
<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
        <version>3.1.5</version>
    </dependency>
    
    <!-- Spring Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
        <version>3.1.5</version>
    </dependency>
    
    <!-- Database -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
        <version>3.1.5</version>
    </dependency>
    
    <!-- Và các dependencies khác... -->
</dependencies>
```

## 📂 Cấu Trúc Project
```
src/
    main/
        java/iuh/fit/se/cosmeticsecommercebackend/
            ├── config/         # Cấu hình ứng dụng 
            ├── controller/     # REST API controllers 
            ├── model/         # Domain models (Entities, enums/)
            ├── dto/           # Data Transfer Objects
            ├── repository/    # Repositories
            ├── service/       # Business logic  
            │   └── impl/     # Service implementations
            ├── security/     # Security configurations
            ├── exception/    # Exception handling 
            └── util/         # Utility classes
        resources/
            └── application.properties    # Cấu hình
```

## 📝 Code Convention
### 1️⃣ Naming Convention
- **Classes**: PascalCase (`ProductController`, `UserService`)
- **Methods**: camelCase (`getUserById`, `createOrder`)
- **Variables**: camelCase (`userId`, `orderTotal`)
- **Constants**: UPPER_SNAKE_CASE (`MAX_RETRY_COUNT`)

### 2️⃣ API Endpoints
- **URL**: `/api/v1/resource-name`
- **HTTP Methods**:
  - GET: Lấy dữ liệu
  - POST: Tạo mới
  - PUT: Cập nhật toàn bộ
  - PATCH: Cập nhật một phần
  - DELETE: Xóa

### 3️⃣ Quy Tắc Commit
- Sử dụng format chuẩn: `type(scope): message`
- Các loại commit phổ biến:
  - `feat`: Tính năng mới
  - `fix`: Sửa lỗi
  - `refactor`: Cải tiến code
  - `style`: Chỉnh sửa style không ảnh hưởng logic
  - `docs`: Cập nhật tài liệu
  - `chore`: Các thay đổi liên quan đến công cụ, cấu hình xây dựng
  (ví dụ: cập nhật dependencies, cấu hình build tools, etc.)

## 🚀 Cài Đặt & Chạy

### 1. Yêu Cầu Hệ Thống
- Java JDK 21
- Maven 3.9.5+
- MySQL 8.0+

### 2. Cài Đặt
```bash
# Clone repository
git clone https://github.com/DHKTPM18B-Nhom07-Cosmetics-Ecommerce/DHKTPM18B_Nhom07_Cosmetics_Ecommerce_Backend.git

# Di chuyển vào thư mục
cd DHKTPM18B_Nhom07_Cosmetics_Ecommerce_Backend

# Build project
mvn clean install

# Chạy ứng dụng
mvn spring-boot:run
```

### 3. Cấu Hình
Tạo file `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/cosmetics_db
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
```

## 📚 API Documentation
Truy cập Swagger UI: `http://localhost:8080/swagger-ui.html`

## 🤝 Đóng Góp
Mọi sự đóng góp đều được hoan nghênh! Để đơn giản hoá chỉ sử dụng nhánh `main` để thành viên chưa chưa có kinh nghiệm về `git` vẫn có thể làm việc. Vui lòng kiểm tra thật kỹ trước khi push commit tránh sai sót không đáng có. Xin cảm ơn!

📢 *Nếu bạn thích dự án này, hãy ⭐ trên GitHub!*
## 📝 License
Copyright © 2025 DHKTPM18B - Lập trình WWW (Java) - Nhóm 07.
This project is MIT licensed.
=======
heloo
>>>>>>> c1ad945d54f1f0ebae84037788a52d76ccf4a2c8
