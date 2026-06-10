import AdminLibrariansPage from "../pages/admin/AdminLibrariansPage";
import AdminRulesPage from "../pages/admin/AdminRulesPage";
import AdminReportsPage from "../pages/admin/AdminReportsPage";
import CommentModerationPage from "../pages/admin/CommentModerationPage";

export const adminRoutes = [
    {
        path: "/admin/librarians",
        element: <AdminLibrariansPage />
    },
    {
        path: "/admin/rules",
        element: <AdminRulesPage />
    },
    {
        path: "/admin/reports",
        element: <AdminReportsPage />
    },
    {
        path: "/admin/comments",
        element: <CommentModerationPage />
    }
];
