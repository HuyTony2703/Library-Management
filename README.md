# LibraDesk

LibraDesk là ứng dụng desktop quản lý thư viện trên Windows. Hệ thống bao gồm:

- Backend REST API: Java 21, Spring Boot và Spring Security.
- Giao diện: React, Vite và Electron.
- Cơ sở dữ liệu: Microsoft SQL Server.

Ứng dụng hỗ trợ ba vai trò: quản trị viên, thủ thư và độc giả. Các chức năng chính gồm quản lý sách, độc giả, mượn trả, công nợ, thu tiền, đặt trước, đánh giá, bình luận, thông báo, quy định và báo cáo.

## Bắt đầu chạy

### 1. Chuẩn bị môi trường

Để chạy bản desktop đã đóng gói, máy cần:

- Windows 10 hoặc Windows 11.
- SQL Server đang hoạt động.
- Java 21 để chạy backend dạng JAR.

Để build từ source, cần thêm Node.js/npm và kết nối Internet trong lần cài dependency đầu tiên.

### 2. Khởi tạo database

Mở SQL Server Management Studio hoặc công cụ SQL tương đương, rồi chạy lần lượt:

1. `database/scripts/01_full_database.sql`
2. `database/scripts/02_seed_demo_data.sql`

Script đầu tiên tự tạo database `QuanLyThuVien` nếu database chưa tồn tại. Hướng dẫn chi tiết nằm tại [database/README_DATABASE.md](database/README_DATABASE.md).

### 3. Mở ứng dụng

Tại thư mục gốc, chạy:

```bat
start-libradesk.bat
```

Trong lần chạy đầu tiên, terminal sẽ yêu cầu host, tên database, tài khoản và mật khẩu SQL Server. Sau khi kết nối thành công, cấu hình được lưu riêng trong `%APPDATA%\LibraDesk` cho tài khoản Windows hiện tại.

Launcher thực hiện theo thứ tự:

1. Kiểm tra backend tại `http://localhost:8080/api/health`.
2. Khởi động backend nếu cần.
3. Warm-up backend.
4. Mở app desktop từ `release/` hoặc Electron local nếu có đủ dependency.

## Tài khoản demo

| Vai trò | Tên đăng nhập | Mật khẩu |
|---|---|---|
| Quản trị viên | `admin` | `123456` |
| Thủ thư | `thuthu01` | `123456` |
| Độc giả | `docgia01` | `123456` |

Dữ liệu thường dùng khi demo:

| Đối tượng | Mã mẫu |
|---|---|
| Nhân viên thủ thư | `NV_TT001` |
| Độc giả | `DG001` |
| Độc giả có nợ | `DG024`, `DG025`, `DG026` |
| Chi nhánh | `CN_TD` |
| Đầu sách | `F01` |
| Phương thức tiền mặt | `PT_TIEN_MAT` |

## Build từ source

### Backend

Build và cập nhật JAR trong `release/`:

```bat
scripts\build\build-backend-aot.bat
```

Kiểm tra compile nhanh trong lúc phát triển:

```bat
cd backend
.\mvnw.cmd -DskipTests compile
```

### Frontend web

```bat
cd frontend
npm ci
npm run build
```

### App desktop Electron

```bat
cd frontend
npm run dist:win
```

File portable được tạo tại `release/LibraDesk-1.0.0-portable.exe`.

### Lệnh tiện ích từ thư mục gốc

```bat
npm run dev
npm run build
npm run electron:dev
npm start
```

`npm start` gọi `start-libradesk.bat`. `npm run dev` chỉ mở Vite để phát triển giao diện, không thay thế launcher của toàn hệ thống.

## Cấu trúc thư mục

```text
Library-Management/
|-- backend/                  Spring Boot API
|-- frontend/                 React, Vite và Electron
|-- database/
|   |-- scripts/              Schema, seed và script kiểm thử
|   `-- notes/                Ghi chú database và nghiệp vụ
|-- docs/                     Tài liệu kỹ thuật và kiểm thử
|-- scripts/
|   |-- build/                Script build artifact
|   `-- runtime/              Script khởi động và cấu hình runtime
|-- release/                  JAR và app desktop đã đóng gói
|-- package.json              Lệnh npm dùng từ thư mục gốc
`-- start-libradesk.bat       Điểm khởi động chính trên Windows
```

## Tài liệu nên đọc

| Nhu cầu | Tài liệu |
|---|---|
| Cài và chạy database | [database/README_DATABASE.md](database/README_DATABASE.md) |
| Xử lý lỗi kết nối SQL Server | [database/notes/run-database-guide.md](database/notes/run-database-guide.md) |
| Phát triển frontend | [frontend/README.md](frontend/README.md) |
| Script build và runtime | [scripts/README.md](scripts/README.md) |
| Danh mục tài liệu kỹ thuật | [docs/README.md](docs/README.md) |
| Kiểm thử API bằng Postman | [docs/api/library-desktop-app.postman_collection.json](docs/api/library-desktop-app.postman_collection.json) |

## Quy ước API

| Nhóm endpoint | Prefix | Quyền truy cập chính |
|---|---|---|
| Public và đăng nhập | `/api/public/**`, `/api/auth/**` | Không yêu cầu đăng nhập hoặc tùy endpoint |
| Độc giả | `/api/reader/**` | Độc giả |
| Nghiệp vụ thư viện | `/api/staff/**` | Thủ thư và quản trị viên |
| Quản trị | `/api/admin/**` | Quản trị viên |

Frontend nên gọi API qua các module trong `frontend/src/api/` thay vì gọi `fetch` trực tiếp rải rác trong page.

## Xử lý lỗi thường gặp

### Không kết nối được SQL Server

1. Kiểm tra dịch vụ SQL Server đang chạy.
2. Với kết nối `localhost:1433`, kiểm tra TCP/IP và port `1433` đã được bật.
3. Kiểm tra SQL Authentication nếu dùng tài khoản `sa`.
4. Reset cấu hình đã lưu và chạy lại:

```bat
scripts\runtime\reset-db-config.bat
start-libradesk.bat
```

### Backend không khởi động

Chạy backend riêng để xem lỗi trực tiếp:

```bat
scripts\runtime\start-backend.bat
```

Log runtime nằm trong `%APPDATA%\LibraDesk`.

### Thiếu Vite hoặc Electron

```bat
cd frontend
npm ci
npm run build
```

Sau đó chạy lại `start-libradesk.bat`. Nếu cần đóng gói lại app desktop, chạy `npm run dist:win` trong thư mục `frontend`.

## Thành viên

| Họ tên | MSSV | Phụ trách chính |
|---|---:|---|
| Lê Trí Cao | 24520206 | Backend, database và nghiệp vụ |
| Tô Ngọc Huy | 24520698 | Frontend, Electron và giao diện |
| Lê Tuấn Dương | 24520359 | Tài liệu, kiểm thử và báo cáo |

## Lưu ý vận hành

- Không đưa mật khẩu database hoặc JWT secret thật lên Git.
- Không di chuyển `start-libradesk.bat` khỏi thư mục gốc nếu chưa cập nhật các đường dẫn tương ứng.
- Artifact trong `release/` phải được build lại sau khi thay đổi backend hoặc frontend.
- Source code và schema hiện tại là nguồn thông tin chính xác nhất nếu tài liệu thiết kế cũ có khác biệt.

## Checklist trước khi bàn giao

1. Chạy `git status` và xác nhận không có secret, log, `node_modules`, `frontend/dist` hoặc `backend/target` trong thay đổi.
2. Chạy `npm run build` để kiểm tra frontend.
3. Chạy `backend\\mvnw.cmd test` để kiểm tra backend khi môi trường database cho phép.
4. Khởi động bằng `start-libradesk.bat`, đăng nhập đủ ba vai trò và thử ít nhất một luồng đọc dữ liệu.
5. Nếu source đã thay đổi hành vi runtime, build lại artifact tương ứng trong `release/` và kiểm tra timestamp.
6. Cập nhật tài liệu API/database liên quan trong cùng đợt thay đổi.
