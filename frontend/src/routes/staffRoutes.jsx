import StaffLoansPage from "../pages/staff/StaffLoansPage";
import StaffReturnsPage from "../pages/staff/StaffReturnsPage";
import StaffPaymentsPage from "../pages/staff/StaffPaymentsPage";

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
    }
];
