# Frontend LibraDesk

Frontend sử dụng React, Vite và Electron. Cùng một code giao diện được dùng khi phát triển trên trình duyệt và khi đóng gói thành app desktop Windows.

## Cấu trúc

```text
frontend/
|-- electron/              Main process của Electron
|-- public/                Tài nguyên tĩnh
|-- src/
|   |-- api/               API client và module theo vai trò
|   |-- components/        Component dùng chung
|   |-- context/           React context, gồm trạng thái đăng nhập
|   |-- pages/             Trang chung và trang admin/staff/reader
|   |-- routes/            Route theo vai trò
|   `-- utils/             Helper hiển thị, vai trò và thông báo
|-- dist/                  Output của Vite, có thể tạo lại
|-- package.json
`-- vite.config.js
```

## Cài dependency

Chạy trong thư mục `frontend`:

```bat
npm ci
```

Nếu `package-lock.json` chưa phù hợp hoặc đang phát triển dependency mới, dùng `npm install` rồi kiểm tra thay đổi lockfile trước khi commit.

## Lệnh thường dùng

| Lệnh | Mục đích |
|---|---|
| `npm run dev` | Mở Vite dev server |
| `npm run build` | Build production vào `dist/` |
| `npm run preview` | Xem thử production build trên trình duyệt |
| `npm run electron:dev` | Chạy Vite và Electron cùng lúc |
| `npm run electron:preview` | Build rồi mở Electron local |
| `npm run dist:win` | Đóng gói app portable vào `../release` |

Từ thư mục gốc có thể dùng các lệnh rút gọn tương ứng như `npm run dev`, `npm run build` và `npm run electron:dev`.

## Kết nối backend

Backend local mặc định chạy tại:

```text
http://localhost:8080
```

Các nguyên tắc khi thêm API:

- Dùng `src/api/apiClient.js` cho xử lý request chung.
- Đặt endpoint theo vai trò trong `adminApi.js`, `staffApi.js` hoặc `readerApi.js`.
- Không gọi `fetch` trực tiếp rải rác trong page nếu API client hiện có đáp ứng được.
- Hiển thị lỗi nghiệp vụ bằng toast hoặc dialog nhất quán, không render JSON thô.
- Giá trị mã nội bộ có thể dùng trong request, nhưng nội dung hiển thị phải dùng tiếng Việt thân thiện.

## Route và phân quyền

- Route quản trị: `src/routes/adminRoutes.jsx`.
- Route thủ thư: `src/routes/staffRoutes.jsx`.
- Route độc giả: `src/routes/readerRoutes.jsx`.
- Bảo vệ route dùng `ProtectedRoute` và context đăng nhập.

Admin có toàn bộ quyền nghiệp vụ của thủ thư và thêm quyền quản trị. Không để trang admin-only xuất hiện hoặc gọi API admin khi đăng nhập bằng thủ thư.

## Quy ước giao diện

- Dùng component dùng chung như `DataTable`, `ResultModal`, `InlineActionMenu`, `StatusBadge` và các provider dialog/toast.
- Bảng dữ liệu phải giữ cột ổn định khi phân trang.
- Form thao tác chính nên mở trong modal/dialog thay vì chen vào góc trang.
- Trạng thái chọn, hover, loading, empty và error phải nhìn thấy rõ ở cả nền sáng và nền tối.
- Các thiết lập giao diện được lưu trên trình duyệt hiện tại.

## Build desktop

```bat
npm run dist:win
```

Quá trình này tự build Vite trước, sau đó dùng electron-builder tạo:

```text
release/LibraDesk-1.0.0-portable.exe
```

Sau khi đóng gói, chạy `start-libradesk.bat` tại thư mục gốc để kiểm tra toàn bộ backend và app desktop.

## Xử lý lỗi

### `vite` không được nhận diện

```bat
cd frontend
npm ci
npm run dev
```

Không cài Vite global; project đã khai báo Vite trong `devDependencies`.

### Electron tải lâu hoặc tự đóng

- Đảm bảo `npm ci` đã hoàn tất.
- Kiểm tra `frontend/node_modules/electron/dist/electron.exe`.
- Chạy `npm run electron:preview` để xem lỗi trực tiếp.
- Kiểm tra policy bảo mật Windows nếu file `.exe` bị chặn.

### Build thành công nhưng app hiển thị giao diện cũ

Build lại app portable bằng `npm run dist:win`, sau đó xác nhận timestamp của file trong `release/` đã thay đổi.

## Checklist trước khi build release

1. Chạy `npm ci` để dependency khớp `package-lock.json`.
2. Chạy `npm run build` và xử lý mọi lỗi import hoặc route.
3. Kiểm tra đăng nhập và điều hướng của admin, thủ thư, độc giả.
4. Kiểm tra API base vẫn trỏ tới backend mong muốn; không hard-code URL trong page/component.
5. Chạy `npm run dist:win` và xác nhận file portable mới nằm trong `../release`.
6. Mở app bằng `../start-libradesk.bat` để kiểm tra đúng luồng runtime thực tế.

Không commit `node_modules` hoặc `dist`; hai thư mục này luôn phải có thể tái tạo từ source và lockfile.
