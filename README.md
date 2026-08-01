# 👟 Shoe Store — Quản lý cửa hàng giày

Dự án quản lý cửa hàng giày gồm 2 phần tách biệt trong cùng 1 repo:

- **Backend** — REST API viết bằng Spring Boot (Java 17), nằm trong `src/`
- **`frontend/`** — Giao diện web tĩnh (HTML/CSS/JavaScript thuần)

---

## 📁 Cấu trúc thư mục

```
shoe_store/
├── frontend/
│   ├── index.html             # Dashboard
│   ├── login.html
│   ├── register.html
│   ├── products.html
│   ├── css/
│   │   └── style.css
│   └── js/
│       ├── api.js             # Quản lý JWT token, gọi API
│       ├── app.js             # Auth guard, navbar
│       ├── auth.js            # Xử lý login/register
│       └── products.js        # CRUD sản phẩm
│
├── src/main/java/com/example/shoestore/
│   ├── controller/             # REST Controllers
│   ├── service/                 # Business logic
│   ├── repository/              # JPA Repositories
│   ├── entity/                  # JPA Entities
│   ├── dto/                     # Data Transfer Objects
│   ├── security/                # JWT Auth
│   ├── config/                  # Security, CORS, DataInitializer
│   └── exception/                # Global Exception Handler
├── src/main/resources/
│   ├── application.properties
│   └── schema.sql
│
├── pom.xml
├── mvnw / mvnw.cmd
├── seed.sql
└── README.md
```

---

## ⚙️ Yêu cầu môi trường

| Thành phần | Phiên bản |
|---|---|
| Java | 17+ |
| Maven | dùng kèm `mvnw` (không cần cài riêng) |
| MySQL | 8.x |
| Trình duyệt | bất kỳ |

---

## 🚀 Cách chạy Backend

1. Tạo database MySQL tên `shoe_store` (hoặc để `schema.sql` tự tạo).

2. Cấu hình kết nối DB trong `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/shoe_store?useSSL=false&serverTimezone=UTC
   spring.datasource.username=root
   spring.datasource.password=root
   ```

3. Chạy bằng IntelliJ: mở `ShoeStoreApplication.java` → bấm nút ▶️ Run

   Hoặc chạy bằng terminal tại thư mục gốc:
   ```bash
   ./mvnw spring-boot:run
   ```
   Windows: `mvnw.cmd spring-boot:run`

4. Backend chạy tại: **http://localhost:8080**

5. (Tùy chọn) Nạp dữ liệu mẫu:
   ```bash
   mysql -u root -p shoe_store < seed.sql
   ```

---

## 🎨 Cách chạy Frontend

Frontend là static site thuần, không cần build. **Không mở trực tiếp file `.html`** kiểu double-click (`file://`) vì sẽ lỗi khi gọi API — cần chạy qua local server.

**Cách A — Terminal (khuyến nghị, tránh lỗi CORS với IntelliJ built-in server)**
```bash
cd frontend
python -m http.server 5500
```
Mở trình duyệt: **http://localhost:5500/login.html**

**Cách B — Node.js**
```bash
cd frontend
npx http-server -p 5500
```

**Cách C — VS Code Live Server**
- Mở thư mục `frontend/` trong VS Code
- Chuột phải `login.html` → *Open with Live Server*

> ⚠️ Không dùng icon trình duyệt tích hợp sẵn của IntelliJ (port 63342) — extension "JetBrains IDE Support" có thể chèn header gây lỗi CORS khi tải Google Fonts.

> Đảm bảo `API_BASE` trong `frontend/js/api.js` trỏ đúng địa chỉ backend (mặc định: `http://localhost:8080`).

---

## 🔐 Tài khoản mặc định (sau khi seed data)

| Username | Password | Role |
|---|---|---|
| admin | admin123 | ADMIN |

*(Mật khẩu tự động mã hóa BCrypt khi khởi động backend lần đầu — xem `DataInitializer.java`)*

---

## 📌 Ghi chú

- Backend cấu hình CORS cho phép mọi origin (`CorsConfig.java`) để frontend chạy ở port khác không bị chặn.
- Xác thực dùng JWT — token lưu tại `localStorage` phía frontend (`shoe_store_token`).
- Khi deploy thật, nên:
  - Giới hạn CORS `allowedOriginPatterns` về domain cụ thể thay vì `*`
  - Đổi `app.jwt.secret` trong `application.properties`
  - Đổi `API_BASE` trong `api.js` thành domain backend đã deploy
