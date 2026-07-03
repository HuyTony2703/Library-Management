import { apiFetch } from "./apiClient";

export const staffApi = {
    getContext: () => apiFetch("/api/staff/me/context"),

    searchReaders: (query, { signal, limit = 15 } = {}) => {
        const params = new URLSearchParams({ q: query, limit: String(limit) });
        return apiFetch(`/api/staff/readers/search?${params.toString()}`, { signal });
    },

    getReaderBorrowingContext: (readerId, options = {}) =>
        apiFetch(`/api/staff/readers/${encodeURIComponent(readerId)}/borrowing-context`, options),

    searchDebtors: (query, { signal, limit = 15, outstandingOnly = true } = {}) => {
        const params = new URLSearchParams({
            q: query,
            limit: String(limit),
            outstandingOnly: String(outstandingOnly)
        });
        return apiFetch(`/api/staff/debtors/search?${params.toString()}`, { signal });
    },

    getReaderDebtContext: (readerId, options = {}) =>
        apiFetch(`/api/staff/readers/${encodeURIComponent(readerId)}/debt-context`, options),

    getReaderCurrentLoansDetail: (readerId, options = {}) =>
        apiFetch(`/api/staff/readers/${encodeURIComponent(readerId)}/current-loans`, options),

    searchLoanCopies: (query, readerId, { signal, limit = 15 } = {}) => {
        const params = new URLSearchParams({ q: query, readerId, limit: String(limit) });
        return apiFetch(`/api/staff/book-copies/search?${params.toString()}`, { signal });
    },

    getLoanCopyByCode: (code, readerId, options = {}) => {
        const params = new URLSearchParams({ readerId });
        return apiFetch(`/api/staff/book-copies/by-barcode/${encodeURIComponent(code)}?${params.toString()}`, options);
    },

    previewLoan: (payload, options = {}) =>
        apiFetch("/api/staff/loans/preview", {
            ...options,
            method: "POST",
            body: JSON.stringify(payload)
        }),

    createLoan: (payload, idempotencyKey) =>
        apiFetch("/api/staff/loans", {
            method: "POST",
            headers: { "Idempotency-Key": idempotencyKey },
            body: JSON.stringify(payload)
        }),

    getLoan: (id) => apiFetch(`/api/staff/loans/${id}`),

    createReturn: (payload, idempotencyKey) =>
        apiFetch("/api/staff/returns", {
            method: "POST",
            headers: { "Idempotency-Key": idempotencyKey },
            body: JSON.stringify(payload)
        }),

    previewReturn: (payload, options = {}) =>
        apiFetch("/api/staff/returns/preview", {
            ...options,
            method: "POST",
            body: JSON.stringify(payload)
        }),

    getReturn: (id) => apiFetch(`/api/staff/returns/${id}`),

    getOpenLoanByCode: (code, options = {}) =>
        apiFetch(`/api/staff/open-loans/by-barcode/${encodeURIComponent(code)}`, options),

    getReaderOpenLoans: (readerId, options = {}) =>
        apiFetch(`/api/staff/readers/${encodeURIComponent(readerId)}/open-loans`, options),

    getReaderDebts: (readerId, options = {}) =>
        apiFetch(`/api/staff/readers/${encodeURIComponent(readerId)}/debts`, options),

    getReaderCurrentLoans: (readerId) =>
        apiFetch(`/api/staff/readers/${readerId}/current-loans`),

    previewPayment: (payload, options = {}) =>
        apiFetch("/api/staff/payments/preview", {
            ...options,
            method: "POST",
            body: JSON.stringify(payload)
        }),

    createPayment: (payload, idempotencyKey) =>
        apiFetch("/api/staff/payments", {
            method: "POST",
            headers: { "Idempotency-Key": idempotencyKey },
            body: JSON.stringify(payload)
        }),

    getPayment: (id) => apiFetch(`/api/staff/payments/${id}`),

    getReaderPayments: (readerId) =>
        apiFetch(`/api/staff/readers/${readerId}/payments`)
};
