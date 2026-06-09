import { Route } from "react-router-dom";
import ReaderOnlyRoute from "../components/reader/ReaderOnlyRoute";
import ReaderLayout from "../pages/reader/ReaderLayout";
import ReaderHomePage from "../pages/reader/ReaderHomePage";
import ReaderBooksPage from "../pages/reader/ReaderBooksPage";
import ReaderBookDetailPage from "../pages/reader/ReaderBookDetailPage";
import ReaderLoansPage from "../pages/reader/ReaderLoansPage";
import ReaderRenewalHistoryPage from "../pages/reader/ReaderRenewalHistoryPage";
import ReaderReservationsPage from "../pages/reader/ReaderReservationsPage";

function PlaceholderPage({ title }) {
    return (
        <div className="reader-card">
            <h1>{title}</h1>
            <p>Trang này sẽ được hoàn thiện trong các bước tiếp theo.</p>
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
        <Route path="books" element={<ReaderBooksPage />} />
        <Route path="books/:maDauSach" element={<ReaderBookDetailPage />} />
        <Route path="loans" element={<ReaderLoansPage />} />
        <Route path="loans/renewal-history" element={<ReaderRenewalHistoryPage />} />
        <Route path="reservations" element={<ReaderReservationsPage />} />
        <Route path="notifications" element={<PlaceholderPage title="Thông báo" />} />
        <Route path="membership" element={<PlaceholderPage title="Gói thành viên" />} />
        <Route path="favorites" element={<PlaceholderPage title="Sách yêu thích" />} />
        <Route path="recommendations" element={<PlaceholderPage title="Gợi ý sách" />} />
        <Route path="guide" element={<PlaceholderPage title="Điều khoản và hướng dẫn" />} />
    </Route>
);
