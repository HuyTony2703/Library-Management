import { apiFetch } from "./apiClient";

export const libraryApi = {
    health: () => apiFetch("/api/health"),

    books: () => apiFetch("/api/books"),
    book: (id) => apiFetch(`/api/books/${id}`),
    createBook: (payload) =>
        apiFetch("/api/books", {
            method: "POST",
            body: JSON.stringify(payload)
        }),
    deleteBook: (id, mode = "soft") =>
        apiFetch(`/api/books/${id}?mode=${encodeURIComponent(mode)}`, {
            method: "DELETE"
        }),

    bookCopies: () => apiFetch("/api/book-copies"),
    bookCopy: (id) => apiFetch(`/api/book-copies/${id}`),
    createBookCopy: (payload) =>
        apiFetch("/api/book-copies", {
            method: "POST",
            body: JSON.stringify(payload)
        }),
    deleteBookCopy: (id, mode = "soft") =>
        apiFetch(`/api/book-copies/${id}?mode=${encodeURIComponent(mode)}`, {
            method: "DELETE"
        }),

    readers: () => apiFetch("/api/readers"),
    reader: (id) => apiFetch(`/api/readers/${id}`),
    createReader: (payload) =>
        apiFetch("/api/readers", {
            method: "POST",
            body: JSON.stringify(payload)
        }),
    deleteReader: (id, mode = "soft") =>
        apiFetch(`/api/readers/${id}?mode=${encodeURIComponent(mode)}`, {
            method: "DELETE"
        }),
    currentLoans: (readerId) => apiFetch(`/api/readers/${readerId}/current-loans`),

    createLoan: (payload) =>
        apiFetch("/api/loans", {
            method: "POST",
            body: JSON.stringify(payload)
        }),

    createReturn: (payload) =>
        apiFetch("/api/returns", {
            method: "POST",
            body: JSON.stringify(payload)
        }),

    debts: (readerId) => apiFetch(`/api/readers/${readerId}/debts`),

    createPayment: (payload) =>
        apiFetch("/api/payments", {
            method: "POST",
            body: JSON.stringify(payload)
        }),

    debtReport: () => apiFetch("/api/reports/debts"),
    currentLoansReport: () => apiFetch("/api/reports/current-loans"),
    borrowByCategoryReport: (month, year) =>
        apiFetch(`/api/reports/borrow-by-category?month=${month}&year=${year}`),
    lateReturnsReport: (month, year) =>
        apiFetch(`/api/reports/late-returns?month=${month}&year=${year}`),

    branches: () => apiFetch("/api/options/branches"),
    bookLocations: () => apiFetch("/api/options/book-locations"),
    bookCopyStatuses: () => apiFetch("/api/options/book-copy-statuses"),
    readerGroups: () => apiFetch("/api/options/reader-groups"),
    membershipPlans: () => apiFetch("/api/options/membership-plans"),
    paymentMethods: () => apiFetch("/api/options/payment-methods")
};
