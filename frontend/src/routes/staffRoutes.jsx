import StaffLoansPage from "../pages/staff/StaffLoansPage";
import StaffReturnsPage from "../pages/staff/StaffReturnsPage";
import StaffPaymentsPage from "../pages/staff/StaffPaymentsPage";
import UsageGuidePage from "../pages/UsageGuidePage";

export const staffRoutes = [
    {
        path: "/staff/loans",
        element: <StaffLoansPage />
    },
    {
        path: "/staff/returns",
        element: <StaffReturnsPage />
    },
    {
        path: "/staff/payments",
        element: <StaffPaymentsPage />
    },
    {
        path: "/guide",
        element: <UsageGuidePage />
    }
];
