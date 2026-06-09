import { NavLink, Outlet } from "react-router-dom";

const menuItems = [
  { to: "/reader", label: "Trang chủ" },
  { to: "/reader/books", label: "Tra cứu sách" },
  { to: "/reader/loans", label: "Sách đang mượn" },
  { to: "/reader/reservations", label: "Đặt trước" },
  { to: "/reader/notifications", label: "Thông báo" },
  { to: "/reader/membership", label: "Gói thành viên" },
  { to: "/reader/favorites", label: "Sách yêu thích" },
  { to: "/reader/recommendations", label: "Gợi ý sách" },
  { to: "/reader/guide", label: "Hướng dẫn" }
];

export default function ReaderLayout() {
  return (
    <div className="reader-layout">
      <aside className="reader-sidebar">
        <h2>LibraDesk Reader</h2>

        <nav>
          {menuItems.map((item) => (
            <NavLink key={item.to} to={item.to} end={item.to === "/reader"}>
              {item.label}
            </NavLink>
          ))}
        </nav>
      </aside>

      <main className="reader-main">
        <Outlet />
      </main>
    </div>
  );
}