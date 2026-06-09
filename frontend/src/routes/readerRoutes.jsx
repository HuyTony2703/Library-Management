import { Route } from "react-router-dom";
import ReaderOnlyRoute from "../components/reader/ReaderOnlyRoute";
import ReaderLayout from "../pages/reader/ReaderLayout";
import ReaderHomePage from "../pages/reader/ReaderHomePage";

function PlaceholderPage({ title }) {
    return (
        <div className="reader-card">
            <h1>{title}</h1>
            <p>Trang này sẽ được hoàn thiện ở module tiếp theo.</p>
        </div>
    );
}

export const readerRoutes = (
    <Route
        path="/reader"
        element={
            <ReaderOnlyRoute>
                <ReaderLayout />
            </ReaderOnlyRoute>
        }
    >
        <Route index element={<ReaderHomePage />} />
        <Route path="books" element={<PlaceholderPage title="Tra cứu sách" />} />
        <Route path="books/:maDauSach" element={<PlaceholderPage title="Chi tiết sách" />} />
        <Route path="loans" element={<PlaceholderPage title="Sách đang mượn" />} />
        <Route path="reservations" element={<PlaceholderPage title="Đặt trước sách" />} />
        <Route path="notifications" element={<PlaceholderPage title="Thông báo" />} />
        <Route path="membership" element={<PlaceholderPage title="Gói thành viên" />} />
        <Route path="favorites" element={<PlaceholderPage title="Sách yêu thích" />} />
        <Route path="recommendations" element={<PlaceholderPage title="Gợi ý sách" />} />
        <Route path="guide" element={<PlaceholderPage title="Điều khoản và hướng dẫn" />} />
    </Route>
);
