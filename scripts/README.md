# Script build và runtime

Người dùng không cần chạy trực tiếp phần lớn file trong thư mục này. Điểm khởi động chính của toàn hệ thống là `start-libradesk.bat` tại thư mục gốc.

## Runtime

| File | Khi nào dùng |
|---|---|
| `runtime/start-libradesk.ps1` | Script PowerShell được launcher chính gọi tự động |
| `runtime/start-backend.bat` | Chạy backend riêng để theo dõi lỗi hoặc cấu hình database |
| `runtime/reset-db-config.bat` | Xóa cấu hình SQL Server đã lưu để nhập lại |

### Luồng khởi động

`start-libradesk.bat` gọi `runtime/start-libradesk.ps1`. Script PowerShell sẽ:

1. Kiểm tra health endpoint của backend.
2. Gọi backend runner nếu backend chưa chạy.
3. Warm-up backend.
4. Ưu tiên mở app portable trong `release/`.
5. Nếu không có app portable, thử Electron local từ `frontend/dist`.

Browser fallback mặc định bị tắt. Chỉ bật khi thực sự cần:

```bat
set LIBRADESK_ALLOW_BROWSER_FALLBACK=1
start-libradesk.bat
```

## Build

| File | Kết quả |
|---|---|
| `build/build-backend-aot.bat` | Build JAR và copy vào `release/backend-0.0.1-SNAPSHOT.jar` |
| `build/build-backend-native.bat` | Build native executable khi máy có GraalVM Native Image và C++ build tools |

Build backend JAR thông thường:

```bat
scripts\build\build-backend-aot.bat
```

Build app desktop:

```bat
cd frontend
npm ci
npm run dist:win
```

## Artifact runtime

Launcher ưu tiên các file:

```text
release/backend-0.0.1-SNAPSHOT.jar
release/LibraDesk-1.0.0-portable.exe
```

Khi thay đổi backend hoặc frontend, cần build lại artifact tương ứng trước khi phát hành hoặc demo.

## Log & cấu hình

Runtime lưu dữ liệu cục bộ tại:

```text
%APPDATA%\LibraDesk
```

Các file thường gặp:

- `db-config.properties`: host, port và tên database.
- `db-password.dpapi`: mật khẩu database đã được bảo vệ.
- `backend.out.log`, `backend.err.log`: log backend.
- `frontend.out.log`, `frontend.err.log`: log browser fallback nếu được bật.

Không commit các file runtime này vào Git.

## Xử lý lỗi nhanh

### Backend không sẵn sàng

```bat
scripts\runtime\start-backend.bat
```

### Sai cấu hình database

```bat
scripts\runtime\reset-db-config.bat
start-libradesk.bat
```

### Thiếu app desktop

```bat
cd frontend
npm ci
npm run dist:win
```

### File portable bị Windows chặn

Thử chạy Electron local sau khi đã cài dependency và build frontend. Nếu máy thuộc tổ chức có Application Control policy, cần nhờ quản trị hệ thống cho phép file thực thi; không nên tắt chính sách bảo mật toàn máy.

## Quy tắc an toàn khi sửa script

- Luôn tính đường dẫn từ vị trí script hoặc root project, không phụ thuộc thư mục terminal hiện tại.
- Mọi nhánh lỗi phải trả exit code khác `0` và in rõ file/log cần kiểm tra.
- Không ghi password database ra console hoặc log; password runtime phải tiếp tục dùng DPAPI.
- Không tự động mở browser trừ khi người dùng bật `LIBRADESK_ALLOW_BROWSER_FALLBACK=1`.
- Không tự động xóa artifact release; script build chỉ thay artifact sau khi build thành công.
- Sau khi sửa, kiểm tra cả trường hợp backend đã chạy và trường hợp backend chưa chạy.
