# LibraDesk Frontend

Frontend của LibraDesk dùng React, Vite và Electron để đóng gói thành ứng dụng desktop Windows.

## Cấu Trúc Chính

```text
frontend/
├─ electron/       # Main process Electron
├─ public/         # Static assets
├─ src/
│  ├─ api/         # Module gọi API theo vai trò
│  ├─ assets/      # Tài nguyên giao diện
│  ├─ components/  # Component dùng chung
│  ├─ context/     # Context React
│  ├─ pages/       # Page theo nhóm admin/staff/reader
│  ├─ routes/      # Điều hướng
│  └─ utils/       # Helper frontend
├─ package.json
└─ vite.config.js
```

## Lệnh Thường Dùng

Cài dependency:

```bat
npm install
```

Chạy web dev server:

```bat
npm run dev
```

Build frontend:

```bat
npm run build
```

Chạy Electron khi phát triển:

```bat
npm run electron:dev
```

Build desktop portable:

```bat
npm run dist:win
```

Artifact desktop được xuất ra thư mục `../release`.

## Ghi Chú

- API base mặc định trỏ về backend local `http://localhost:8080`.
- Khi thay đổi route hoặc API, ưu tiên cập nhật module trong `src/api/` thay vì gọi `fetch` rải rác trong page.
- Root project có `start-libradesk.bat` để chạy app hoàn chỉnh sau khi đã có backend/frontend artifact.
