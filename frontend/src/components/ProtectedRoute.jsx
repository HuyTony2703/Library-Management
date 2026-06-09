import { Navigate } from "react-router-dom";
import { getToken } from "../api/apiClient";
import { useAuth } from "../context/AuthContext";
import { normalizeRole } from "../utils/roleUtils";

export default function ProtectedRoute({ children, allowedRoles }) {
    const { user, loadingUser, isAuthenticated } = useAuth();

    if (loadingUser) {
        return <div className="boot-screen">Đang kiểm tra đăng nhập...</div>;
    }

    const loggedIn = isAuthenticated || Boolean(getToken());

    if (!loggedIn || !user) {
        return <Navigate to="/login" replace />;
    }

    if (allowedRoles && allowedRoles.length > 0) {
        const role = normalizeRole(user);

        if (!allowedRoles.includes(role)) {
            const fallbackPath = role === "ADMIN" || role === "STAFF"
                ? "/dashboard"
                : "/login";

            return <Navigate to={fallbackPath} replace />;
        }
    }

    return children;
}
