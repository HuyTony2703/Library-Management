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

`start-libradesk.ps1` ưu tiên mở app desktop trong `release/`. Nếu cần fallback tạm sang Vite preview trên trình duyệt, đặt:

```bat
set LIBRADESK_ALLOW_BROWSER_FALLBACK=1
start-libradesk.bat
```

Khi chưa có app release, `start-libradesk.bat` có thể tự chạy `npm ci` và `npm run build` để chuẩn bị frontend local.

## Build

```text
scripts/build/build-backend-aot.bat
scripts/build/build-backend-native.bat
```

- `build-backend-aot.bat`: build backend artifact dùng cho release thông thường.
- `build-backend-native.bat`: build native khi môi trường đã có đủ công cụ native tương ứng.

Artifact release đang dùng khi demo:

```text
release/backend-0.0.1-SNAPSHOT.jar
release/LibraDesk-1.0.0-portable.exe
```

## Entry Point Chính

Không cần vào thư mục này để chạy app. Dùng:

```bat
start-libradesk.bat
```
