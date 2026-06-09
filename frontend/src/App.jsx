import { Navigate, Route, Routes } from "react-router-dom";
import AppLayout from "./components/AppLayout";
import ProtectedRoute from "./components/ProtectedRoute";

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

export default function App() {
    return (
        <Routes>
            <Route path="/login" element={<LoginPage />} />

            <Route
                element={
                    <ProtectedRoute allowedRoles={["ADMIN", "STAFF"]}>
                        <AppLayout />
                    </ProtectedRoute>
                }
            >
                <Route path="/dashboard" element={<DashboardPage />} />

                <Route
                    path="/admin/librarians"
                    element={
                        <ProtectedRoute allowedRoles={["ADMIN"]}>
                            <AdminLibrariansPage />
                        </ProtectedRoute>
                    }
                />
                <Route
                    path="/admin/rules"
                    element={
                        <ProtectedRoute allowedRoles={["ADMIN"]}>
                            <AdminRulesPage />
                        </ProtectedRoute>
                    }
                />
                <Route
                    path="/admin/reports"
                    element={
                        <ProtectedRoute allowedRoles={["ADMIN"]}>
                            <AdminReportsPage />
                        </ProtectedRoute>
                    }
                />
                <Route
                    path="/admin/comments"
                    element={
                        <ProtectedRoute allowedRoles={["ADMIN"]}>
                            <CommentModerationPage />
                        </ProtectedRoute>
                    }
                />

                <Route
                    path="/staff/loans"
                    element={
                        <ProtectedRoute allowedRoles={["ADMIN", "STAFF"]}>
                            <StaffLoansPage />
                        </ProtectedRoute>
                    }
                />
                <Route
                    path="/staff/returns"
                    element={
                        <ProtectedRoute allowedRoles={["ADMIN", "STAFF"]}>
                            <StaffReturnsPage />
                        </ProtectedRoute>
                    }
                />
                <Route
                    path="/staff/payments"
                    element={
                        <ProtectedRoute allowedRoles={["ADMIN", "STAFF"]}>
                            <StaffPaymentsPage />
                        </ProtectedRoute>
                    }
                />

                <Route
                    path="/books"
                    element={
                        <ProtectedRoute allowedRoles={["ADMIN", "STAFF"]}>
                            <BooksPage />
                        </ProtectedRoute>
                    }
                />
                <Route
                    path="/book-copies"
                    element={
                        <ProtectedRoute allowedRoles={["ADMIN", "STAFF"]}>
                            <BookCopiesPage />
                        </ProtectedRoute>
                    }
                />
                <Route
                    path="/readers"
                    element={
                        <ProtectedRoute allowedRoles={["ADMIN", "STAFF"]}>
                            <ReadersPage />
                        </ProtectedRoute>
                    }
                />

                <Route
                    path="/loans"
                    element={
                        <ProtectedRoute allowedRoles={["ADMIN", "STAFF"]}>
                            <LoansPage />
                        </ProtectedRoute>
                    }
                />
                <Route
                    path="/returns"
                    element={
                        <ProtectedRoute allowedRoles={["ADMIN", "STAFF"]}>
                            <ReturnsPage />
                        </ProtectedRoute>
                    }
                />
                <Route
                    path="/payments"
                    element={
                        <ProtectedRoute allowedRoles={["ADMIN", "STAFF"]}>
                            <PaymentsPage />
                        </ProtectedRoute>
                    }
                />
                <Route
                    path="/reports"
                    element={
                        <ProtectedRoute allowedRoles={["ADMIN"]}>
                            <ReportsPage />
                        </ProtectedRoute>
                    }
                />
            </Route>

            <Route path="/" element={<Navigate to="/dashboard" replace />} />
            <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
    );
}
