import { Navigate, Route } from "react-router-dom";
import ReaderOnlyRoute from "../components/reader/ReaderOnlyRoute";
import ReaderLayout from "../pages/reader/ReaderLayout";
import ReaderHomePage from "../pages/reader/ReaderHomePage";
import ReaderBooksPage from "../pages/reader/ReaderBooksPage";
import ReaderBookDetailPage from "../pages/reader/ReaderBookDetailPage";
import ReaderLoansPage from "../pages/reader/ReaderLoansPage";
import ReaderRenewalHistoryPage from "../pages/reader/ReaderRenewalHistoryPage";
import ReaderReservationsPage from "../pages/reader/ReaderReservationsPage";
import ReaderNotificationsPage from "../pages/reader/ReaderNotificationsPage";
import ReaderMembershipPage from "../pages/reader/ReaderMembershipPage";
import ReaderFavoritesPage from "../pages/reader/ReaderFavoritesPage";
import ReaderGuidePage from "../pages/reader/ReaderGuidePage";
import ReaderRulesPage from "../pages/reader/ReaderRulesPage";
import ReaderSettingsPage from "../pages/reader/ReaderSettingsPage";

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
        <Route path="notifications" element={<ReaderNotificationsPage />} />
        <Route path="membership" element={<ReaderMembershipPage />} />
        <Route path="favorites" element={<ReaderFavoritesPage />} />
        <Route path="recommendations" element={<Navigate to="/reader" replace />} />
        <Route path="guide" element={<ReaderGuidePage />} />
        <Route path="rules" element={<ReaderRulesPage />} />
<<<<<<< HEAD
        <Route path="penalty-rules" element={<Navigate to="/reader/rules#penalty-rules" replace />} />
=======
        <Route path="penalty-rules" element={<PenaltyRulesPage />} />
        <Route path="settings" element={<ReaderSettingsPage />} />
>>>>>>> 8a8a9f48a34e370233fbe7ef2e3cfcdb910310e3
    </Route>
);
