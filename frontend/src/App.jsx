import { Navigate, Route, Routes } from "react-router-dom";
import AppLayout from "./components/AppLayout";
import { useAuth } from "./context/AuthContext";

import LoginPage from "./pages/LoginPage";
import DashboardPage from "./pages/DashboardPage";
import BooksPage from "./pages/BooksPage";
import BookCopiesPage from "./pages/BookCopiesPage";
import ReadersPage from "./pages/ReadersPage";
import LoansPage from "./pages/LoansPage";
import ReturnsPage from "./pages/ReturnsPage";
import PaymentsPage from "./pages/PaymentsPage";
import ReportsPage from "./pages/ReportsPage";
import StaffLoansPage from "./pages/staff/StaffLoansPage";
import StaffReturnsPage from "./pages/staff/StaffReturnsPage";
import StaffPaymentsPage from "./pages/staff/StaffPaymentsPage";
import AdminLibrariansPage from "./pages/admin/AdminLibrariansPage";
import AdminRulesPage from "./pages/admin/AdminRulesPage";
import AdminReportsPage from "./pages/admin/AdminReportsPage";
import CommentModerationPage from "./pages/admin/CommentModerationPage";

function ProtectedRoute({ children }) {
    const { user, loadingUser } = useAuth();

    if (loadingUser) {
        return <div className="boot-screen">Đang khởi động LibraDesk...</div>;
    }

    if (!user) {
        return <Navigate to="/login" replace />;
    }

    return children;
}

export default function App() {
    return (
        <Routes>
            <Route path="/login" element={<LoginPage />} />

            <Route
                element={
                    <ProtectedRoute>
                        <AppLayout />
                    </ProtectedRoute>
                }
            >
                <Route path="/" element={<DashboardPage />} />
                <Route path="/books" element={<BooksPage />} />
                <Route path="/book-copies" element={<BookCopiesPage />} />
                <Route path="/readers" element={<ReadersPage />} />
                <Route path="/loans" element={<LoansPage />} />
                <Route path="/returns" element={<ReturnsPage />} />
                <Route path="/payments" element={<PaymentsPage />} />
                <Route path="/reports" element={<ReportsPage />} />
                <Route path="/staff/loans" element={<StaffLoansPage />} />
                <Route path="/staff/returns" element={<StaffReturnsPage />} />
                <Route path="/staff/payments" element={<StaffPaymentsPage />} />
                <Route path="/admin/librarians" element={<AdminLibrariansPage />} />
                <Route path="/admin/rules" element={<AdminRulesPage />} />
                <Route path="/admin/reports" element={<AdminReportsPage />} />
                <Route path="/admin/comments" element={<CommentModerationPage />} />
            </Route>
        </Routes>
    );
}
