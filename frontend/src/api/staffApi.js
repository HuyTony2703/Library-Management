import { apiFetch } from "./apiClient";

export const staffApi = {
    createLoan: (payload) =>
        apiFetch("/api/staff/loans", {
            method: "POST",
            body: JSON.stringify(payload)
        }),

    getLoan: (id) => apiFetch(`/api/staff/loans/${id}`),

    createReturn: (payload) =>
        apiFetch("/api/staff/returns", {
            method: "POST",
            body: JSON.stringify(payload)
        }),

    getReturn: (id) => apiFetch(`/api/staff/returns/${id}`),

    getReaderDebts: (readerId) =>
        apiFetch(`/api/staff/readers/${readerId}/debts`),

    getReaderCurrentLoans: (readerId) =>
        apiFetch(`/api/staff/readers/${readerId}/current-loans`),

    createPayment: (payload) =>
        apiFetch("/api/staff/payments", {
            method: "POST",
            body: JSON.stringify(payload)
        }),

    getPayment: (id) => apiFetch(`/api/staff/payments/${id}`),

    getReaderPayments: (readerId) =>
        apiFetch(`/api/staff/readers/${readerId}/payments`)
};
