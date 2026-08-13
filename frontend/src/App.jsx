import { Navigate, Route, Routes, useLocation } from "react-router-dom";
import AppLayout from "./components/AppLayout";
import { useAuth } from "./context/AuthContext";
import { adminRoutes } from "./routes/adminRoutes";
import { readerRoutes } from "./routes/readerRoutes";
import { staffRoutes } from "./routes/staffRoutes";
import { isReaderUser } from "./utils/authRole";
import { normalizeRole } from "./utils/roleUtils";

import LoginPage from "./pages/LoginPage";
import DashboardPage from "./pages/DashboardPage";
import BooksPage from "./pages/BooksPage";
import BookCopiesPage from "./pages/BookCopiesPage";
import ReadersPage from "./pages/ReadersPage";
import LoansPage from "./pages/LoansPage";
import ReturnsPage from "./pages/ReturnsPage";
import PaymentsPage from "./pages/PaymentsPage";
import SettingsPage from "./pages/SettingsPage";

function ProtectedRoute({ children, allowedRoles = [] }) {
    const { user, loadingUser } = useAuth();
    const location = useLocation();

    if (loadingUser) {
        return <div className="boot-screen">Đang khởi động LibraDesk...</div>;
    }

    if (!user) {
        return <Navigate to="/login" replace />;
    }

    if (isReaderUser(user)) {
        return <Navigate to="/reader" replace />;
    }

    if (allowedRoles.length > 0 && !allowedRoles.includes(normalizeRole(user))) {
        return <Navigate to="/" replace />;
    }

    if (user.mustChangePassword && location.pathname !== "/settings") {
        return <Navigate to="/settings#security" replace />;
    }

    return children;
}

export default function App() {
    return (
        <Routes>
            <Route path="/login" element={<LoginPage />} />

            {readerRoutes}

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
                <Route path="/reports" element={<Navigate to="/" replace />} />
                <Route path="/settings" element={<SettingsPage />} />
                {staffRoutes.map((route) => (
                    <Route key={route.path} path={route.path} element={<ProtectedRoute allowedRoles={["ADMIN", "STAFF"]}>{route.element}</ProtectedRoute>} />
                ))}
                {adminRoutes.map((route) => (
                    <Route key={route.path} path={route.path} element={<ProtectedRoute allowedRoles={["ADMIN"]}>{route.element}</ProtectedRoute>} />
                ))}
            </Route>
        </Routes>
    );
}
