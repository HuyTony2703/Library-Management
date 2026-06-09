import { apiFetch } from "./apiClient";

export const adminApi = {
    getLibrarians: () => apiFetch("/api/admin/librarians"),

    getLibrarian: (id) => apiFetch(`/api/admin/librarians/${id}`),

    createLibrarian: (payload) =>
        apiFetch("/api/admin/librarians", {
            method: "POST",
            body: JSON.stringify(payload)
        }),

    updateLibrarian: (id, payload) =>
        apiFetch(`/api/admin/librarians/${id}`, {
            method: "PUT",
            body: JSON.stringify(payload)
        }),

    updateLibrarianStatus: (id, payload) =>
        apiFetch(`/api/admin/librarians/${id}/status`, {
            method: "PATCH",
            body: JSON.stringify(payload)
        }),

    resetLibrarianPassword: (id, payload) =>
        apiFetch(`/api/admin/librarians/${id}/reset-password`, {
            method: "POST",
            body: JSON.stringify(payload)
        }),

    deleteLibrarian: (id) =>
        apiFetch(`/api/admin/librarians/${id}`, {
            method: "DELETE"
        }),

    getCurrentRule: () => apiFetch("/api/admin/rules/current"),

    getRuleHistory: () => apiFetch("/api/admin/rules/history"),

    getRule: (id) => apiFetch(`/api/admin/rules/${id}`),

    createRule: (payload) =>
        apiFetch("/api/admin/rules", {
            method: "POST",
            body: JSON.stringify(payload)
        }),

    activateRule: (id) =>
        apiFetch(`/api/admin/rules/${id}/activate`, {
            method: "POST"
        }),

    getReportOverview: (month, year) =>
        apiFetch(`/api/admin/reports/overview?month=${month}&year=${year}`),

    getDebtReport: () => apiFetch("/api/admin/reports/debts"),

    getCurrentLoansReport: () => apiFetch("/api/admin/reports/current-loans"),

    getBorrowByCategoryReport: (month, year) =>
        apiFetch(`/api/admin/reports/borrow-by-category?month=${month}&year=${year}`),

    getLateReturnsReport: (month, year) =>
        apiFetch(`/api/admin/reports/late-returns?month=${month}&year=${year}`),

    getPaymentsReport: (month, year) =>
        apiFetch(`/api/admin/reports/payments?month=${month}&year=${year}`),

    getComments: ({ status, maDauSach, keyword } = {}) => {
        const params = new URLSearchParams();

        if (status && status !== "Tất cả") {
            params.append("status", status);
        }

        if (maDauSach) {
            params.append("maDauSach", maDauSach);
        }

        if (keyword) {
            params.append("keyword", keyword);
        }

        const query = params.toString();

        return apiFetch(`/api/admin/comments${query ? `?${query}` : ""}`);
    },

    getComment: (id) => apiFetch(`/api/admin/comments/${id}`),

    hideComment: (id, payload) =>
        apiFetch(`/api/admin/comments/${id}/hide`, {
            method: "PATCH",
            body: JSON.stringify(payload)
        }),

    deleteComment: (id, payload) =>
        apiFetch(`/api/admin/comments/${id}/delete`, {
            method: "PATCH",
            body: JSON.stringify(payload)
        }),

    restoreComment: (id, payload) =>
        apiFetch(`/api/admin/comments/${id}/restore`, {
            method: "PATCH",
            body: JSON.stringify(payload)
        })
};
