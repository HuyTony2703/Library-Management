# LibraDesk

LibraDesk là ứng dụng desktop quản lý thư viện, gồm backend Spring Boot, frontend React/Vite/Electron và cơ sở dữ liệu SQL Server. Ứng dụng hỗ trợ quản lý đầu sách, cuốn sách, độc giả, mượn trả, thu tiền phạt, kiểm duyệt bình luận, quy định hệ thống, báo cáo và cài đặt tài khoản.

## Thành viên

| Họ tên | MSSV | Vai trò |
|---|---:|---|
| Lê Trí Cao | 24520206 | Backend, database, nghiệp vụ |
| Tô Ngọc Huy | 24520698 | Frontend, Electron, giao diện |
| Lê Tuấn Dương | 24520359 | Tài liệu, kiểm thử, báo cáo |

## Cấu Trúc Project

```text
Library-Management/
├─ backend/                  # Spring Boot API, cấu hình DB và runner backend
├─ frontend/                 # React + Vite + Electron desktop app
├─ database/                 # Script tạo schema, seed demo và truy vấn kiểm thử
├─ docs/                     # Tài liệu kỹ thuật, API, mapping và Postman collection
│  ├─ api/
│  ├─ backend/
│  └─ frontend/
├─ scripts/
│  ├─ build/                 # Script build artifact backend
│  └─ runtime/               # Script runtime phụ trợ cho app
├─ release/                  # Artifact đã build để chạy/demo
└─ start-libradesk.bat       # Điểm chạy chính cho người dùng Windows
```

Các script runtime/build đã được gom vào `scripts/` để root project gọn hơn. File `start-libradesk.bat` vẫn nằm ở root để có thể chạy app bằng cách double-click như trước.

## Chạy App

Yêu cầu máy có SQL Server và database `QuanLyThuVien`. Nếu chưa có database, chạy lần lượt:

```text
database/scripts/01_full_database.sql
database/scripts/02_seed_demo_data.sql
```

Sau đó chạy:

```bat
start-libradesk.bat
```

Script sẽ tự kiểm tra backend tại `http://localhost:8080/api/health`, khởi động backend khi cần, warm-up backend và mở LibraDesk bằng artifact trong `release/` hoặc fallback sang Electron/Vite nếu phù hợp.

Nếu cần reset cấu hình database đã lưu:

```bat
scripts\runtime\reset-db-config.bat
```

Nếu cần chạy backend riêng:

```bat
scripts\runtime\start-backend.bat
```

## Tài Khoản Demo

| Vai trò | Tên đăng nhập | Mật khẩu |
|---|---|---|
| Admin | `admin` | `123456` |
| Thủ thư | `thuthu01` | `123456` |
| Độc giả | `docgia01` | `123456` |

Một số mã dữ liệu demo thường dùng:

| Dữ liệu | Mã |
|---|---|
| Nhân viên thủ thư | `NV_TT001` |
| Độc giả mẫu | `DG001` |
| Chi nhánh | `CN_TD` |
| Đầu sách | `F01` |
| Phương thức tiền mặt | `PT_TIEN_MAT` |

## Lệnh Phát Triển

Backend:

```bat
cd backend
.\mvnw.cmd -DskipTests compile
```

Frontend:

```bat
cd frontend
npm install
npm run build
```

Desktop build:

```bat
cd frontend
npm run dist:win
```

Build backend artifact:

```bat
scripts\build\build-backend-aot.bat
```

## Tài Liệu

- Tổng mục tài liệu: [docs/README.md](docs/README.md)
- Hợp đồng prefix API: [docs/api-prefix-contract.md](docs/api-prefix-contract.md)
- Tài liệu backend: [docs/backend/complete-documentation.md](docs/backend/complete-documentation.md)
- Cấu trúc backend: [docs/backend/structure.md](docs/backend/structure.md)
- Mapping frontend/API: [docs/frontend/api-mapping.md](docs/frontend/api-mapping.md)
- Database: [database/README_DATABASE.md](database/README_DATABASE.md)
- Script vận hành: [scripts/README.md](scripts/README.md)
- Postman collection: [docs/api/library-desktop-app.postman_collection.json](docs/api/library-desktop-app.postman_collection.json)

## Quy Ước API

- Admin: `/api/admin/**`
- Staff: `/api/staff/**`
- Reader: `/api/reader/**`
- Public: `/api/public/**`

Frontend nên gọi API qua các module theo vai trò trong `frontend/src/api/` để tránh trộn quyền và tránh phá hợp đồng prefix.

## Ghi Chú Vận Hành

- App desktop cần backend chạy ổn trước khi đăng nhập.
- Cấu hình database runtime được lưu trong `%APPDATA%\LibraDesk`.
- Log backend/frontend runtime cũng nằm trong `%APPDATA%\LibraDesk`.
- Không nên đổi vị trí `start-libradesk.bat` vì đây là entrypoint đơn giản cho người dùng cuối.
