# 👟 Shoe Store — Quản lý cửa hàng giày

Dự án quản lý cửa hàng giày gồm 2 phần tách biệt trong cùng 1 repo:

- **Backend** — REST API viết bằng Spring Boot (Java 17), nằm trong `src/`
- **`frontend/`** — Giao diện web tĩnh (HTML/CSS/JavaScript thuần)

---

## 📁 Cấu trúc thư mục

```
shoe_store/
├── frontend/
│   ├── HTML/
│   │   ├── index.html         # Dashboard
│   │   ├── login.html
│   │   ├── register.html
│   │   └── products.html
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

2. **Cấu hình mật khẩu DB bằng biến môi trường** (không sửa trực tiếp vào `application.properties` để tránh lộ mật khẩu khi commit lên Git):

   Trong IntelliJ: **Run → Edit Configurations → Environment variables**, thêm:
   ```
   DB_USERNAME=root
   DB_PASSWORD=<mật khẩu MySQL thật của bạn>
   ```

   Hoặc chạy bằng terminal:
   ```bash
   export DB_USERNAME=root
   export DB_PASSWORD=<mật khẩu MySQL thật của bạn>
   ./mvnw spring-boot:run
   ```
   Windows (PowerShell):
   ```powershell
   $env:DB_USERNAME="root"
   $env:DB_PASSWORD="<mật khẩu MySQL thật của bạn>"
   ./mvnw.cmd spring-boot:run
   ```

3. Hoặc đơn giản nhất khi chạy local một mình: mở `ShoeStoreApplication.java` trong IntelliJ → bấm ▶️ Run (dùng giá trị mặc định `changeme` nếu bạn tạm thời sửa trực tiếp trong file, nhưng **nhớ không commit mật khẩu thật lên Git**).

4. Backend chạy tại: **http://localhost:8080**

5. (Tùy chọn) Nạp dữ liệu mẫu:
   ```bash
   mysql -u root -p shoe_store < seed.sql
   ```

---

## 🎨 Cách chạy Frontend

Các file HTML nằm trong `frontend/HTML/`, không phải trực tiếp trong `frontend/`.

**Không mở trực tiếp file `.html`** kiểu double-click (`file://`) vì sẽ lỗi khi gọi API — cần chạy qua local server.

**Cách A — Terminal (khuyến nghị)**
```bash
cd frontend
python -m http.server 5500
```
Mở trình duyệt: **http://localhost:5500/HTML/login.html**

**Cách B — Node.js**
```bash
cd frontend
npx http-server -p 5500
```
Mở: **http://localhost:5500/HTML/login.html**

**Cách C — VS Code Live Server**
- Mở thư mục `frontend/` trong VS Code
- Vào `HTML/login.html` → chuột phải → *Open with Live Server*

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
- **Bảo mật**: mật khẩu database KHÔNG được hardcode trong `application.properties` — luôn dùng biến môi trường `DB_PASSWORD`. Nếu từng commit mật khẩu thật lên Git, hãy đổi mật khẩu MySQL ngay cả sau khi đã sửa file, vì lịch sử Git vẫn còn lưu giá trị cũ.
- Khi deploy thật, nên:
    - Giới hạn CORS `allowedOriginPatterns` về domain cụ thể thay vì `*`
    - Đổi `app.jwt.secret` trong biến môi trường, không dùng giá trị mặc định
    - Đổi `API_BASE` trong `api.js` thành domain backend đã deploy