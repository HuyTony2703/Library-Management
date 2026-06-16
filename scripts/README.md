# Scripts

Thư mục `scripts/` chứa các script phụ trợ để root project gọn hơn. Người dùng cuối vẫn chạy app bằng `start-libradesk.bat` ở root.

## Runtime

```text
scripts/runtime/start-libradesk.ps1
scripts/runtime/start-backend.bat
scripts/runtime/reset-db-config.bat
```

- `start-libradesk.ps1`: script chính được `start-libradesk.bat` gọi để kiểm tra backend, warm-up và mở frontend.
- `start-backend.bat`: chạy backend riêng, hữu ích khi cần xem log backend trực tiếp.
- `reset-db-config.bat`: xóa cấu hình database runtime trong `%APPDATA%\LibraDesk`.

## Build

```text
scripts/build/build-backend-aot.bat
scripts/build/build-backend-native.bat
```

- `build-backend-aot.bat`: build backend artifact dùng cho release thông thường.
- `build-backend-native.bat`: build native khi môi trường đã có đủ công cụ native tương ứng.

## Entry Point Chính

Không cần vào thư mục này để chạy app. Dùng:

```bat
start-libradesk.bat
```
