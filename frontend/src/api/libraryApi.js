import { apiFetch } from "./apiClient";

export const libraryApi = {
    health: () => apiFetch("/api/health"),

    books: () => libraryApi.booksPage({ page: 1, pageSize: 100 }).then((page) => page.items || []),
    booksPage: (params, options = {}) => {
        const query = new URLSearchParams();
        Object.entries(params || {}).forEach(([key, value]) => {
            if (Array.isArray(value)) {
                value.filter((item) => item !== null && item !== undefined && item !== "")
                    .forEach((item) => query.append(key, String(item)));
                return;
            }
            if (value !== null && value !== undefined && value !== "") {
                query.set(key, String(value));
            }
        });
        return apiFetch(`/api/books?${query.toString()}`, options);
    },
    exportBooks: (payload) => apiFetch("/api/books/export", {
        method: "POST",
        body: JSON.stringify(payload)
    }),
    book: (id, options = {}) => apiFetch(`/api/books/${id}`, options),
    checkBookIsbn: (isbn, excludeId, options = {}) => {
        const params = new URLSearchParams({ isbn });
        if (excludeId) params.set("excludeId", excludeId);
        return apiFetch(`/api/books/isbn-check?${params.toString()}`, options);
    },
    createBook: (payload) =>
        apiFetch("/api/books", {
            method: "POST",
            body: JSON.stringify(payload)
        }),
    updateBook: (id, payload) =>
        apiFetch(`/api/books/${id}`, {
            method: "PUT",
            body: JSON.stringify(payload)
        }),
    bookCopiesByBranch: (id, options = {}) =>
        apiFetch(`/api/books/${id}/copies-by-branch`, options),
    bookHistory: (id, options = {}) =>
        apiFetch(`/api/books/${id}/history`, options),
    bookDeletePreflight: (id, options = {}) =>
        apiFetch(`/api/books/${id}/delete-preflight`, options),
    deactivateBook: (id, reason) =>
        apiFetch(`/api/books/${id}/deactivate`, {
            method: "POST",
            body: JSON.stringify({ reason })
        }),
    reactivateBook: (id, reason) =>
        apiFetch(`/api/books/${id}/reactivate`, {
            method: "POST",
            body: JSON.stringify({ reason })
        }),
    deleteBook: (id) =>
        apiFetch(`/api/books/${id}`, {
            method: "DELETE"
        }),
    searchBookTitles: (query, { signal, limit = 15, activeOnly = true } = {}) => {
        const params = new URLSearchParams({
            q: query,
            limit: String(limit),
            activeOnly: String(activeOnly)
        });
        return apiFetch(`/api/staff/catalog/titles/search?${params.toString()}`, { signal });
    },
    searchBookAuthors: (query, { signal, limit = 15 } = {}) =>
        searchCatalogOptions("authors", query, signal, limit),
    searchBookCategories: (query, { signal, limit = 15 } = {}) =>
        searchCatalogOptions("categories", query, signal, limit),
    searchBookPublishers: (query, { signal, limit = 15 } = {}) =>
        searchCatalogOptions("publishers", query, signal, limit),

    bookCopies: () => libraryApi.bookCopiesPage({ page: 1, pageSize: 100 }).then((page) => page.items || []),
    bookCopiesPage: (params, options = {}) => {
        const query = new URLSearchParams();
        Object.entries(params || {}).forEach(([key, value]) => {
            if (Array.isArray(value)) {
                value.filter(Boolean).forEach((item) => query.append(key, String(item)));
            } else if (value !== null && value !== undefined && value !== "") {
                query.set(key, String(value));
            }
        });
        return apiFetch(`/api/book-copies?${query.toString()}`, options);
    },
    exportBookCopies: (payload) => apiFetch("/api/book-copies/export", {
        method: "POST",
        body: JSON.stringify(payload)
    }),
    bookCopy: (id, options = {}) => apiFetch(`/api/book-copies/${id}`, options),
    bookCopyLocationFilters: (branchIds = [], options = {}) => {
        const query = new URLSearchParams();
        branchIds.filter(Boolean).forEach((id) => query.append("branchIds", id));
        return apiFetch(`/api/book-copies/location-filter-options?${query.toString()}`, options);
    },
    createBookCopy: (payload) =>
        apiFetch("/api/book-copies", {
            method: "POST",
            body: JSON.stringify(payload)
        }),
    updateBookCopy: (id, payload) =>
        apiFetch(`/api/book-copies/${id}`, {
            method: "PUT",
            body: JSON.stringify(payload)
        }),
    deleteBookCopy: (id, mode = "soft") =>
        apiFetch(`/api/book-copies/${id}?mode=${encodeURIComponent(mode)}`, {
            method: "DELETE"
        }),
    restoreBookCopy: (id) =>
        apiFetch(`/api/book-copies/${id}/restore`, {
            method: "PATCH"
        }),
    applyBookCopyCondition: (id, payload) =>
        apiFetch(`/api/book-copies/${id}/condition-events`, {
            method: "POST",
            body: JSON.stringify(payload)
        }),
    moveBookCopy: (id, payload) =>
        apiFetch(`/api/book-copies/${id}/move-location`, {
            method: "POST",
            body: JSON.stringify(payload)
        }),

    readers: () => libraryApi.readersPage({ page: 1, pageSize: 100 }).then((page) => page.items || []),
    readersPage: (params, options = {}) => {
        const query = new URLSearchParams();
        Object.entries(params || {}).forEach(([key, value]) => {
            if (Array.isArray(value)) {
                value.filter(Boolean).forEach((item) => query.append(key, String(item)));
            } else if (value !== null && value !== undefined && value !== "") {
                query.set(key, String(value));
            }
        });
        return apiFetch(`/api/readers?${query.toString()}`, options);
    },
    exportReaders: (payload) => apiFetch("/api/readers/export", {
        method: "POST",
        body: JSON.stringify(payload)
    }),
    exportJob: (jobId, options = {}) => apiFetch(`/api/export-jobs/${jobId}`, options),
    reader: (id) => apiFetch(`/api/readers/${id}`),
    readerOverview: (id, options = {}) => apiFetch(`/api/readers/${id}/overview`, options),
    readerMemberships: (id, options = {}) => apiFetch(`/api/readers/${id}/memberships`, options),
    readerCurrentLoansDetail: (id, options = {}) => apiFetch(`/api/readers/${id}/current-loans-detail`, options),
    readerDebtsDetail: (id, options = {}) => apiFetch(`/api/readers/${id}/debts-detail`, options),
    readerTransactions: (id, page = 1, pageSize = 20, options = {}) =>
        apiFetch(`/api/readers/${id}/transactions?page=${page}&pageSize=${pageSize}`, options),
    readerBorrowingContext: (id, options = {}) => apiFetch(`/api/staff/readers/${id}/borrowing-context`, options),
    lockReader: (id, payload) => apiFetch(`/api/readers/${id}/locks`, { method: "POST", body: JSON.stringify(payload) }),
    unlockReader: (id, payload) => apiFetch(`/api/readers/${id}/unlock`, { method: "POST", body: JSON.stringify(payload) }),
    deactivateReader: (id, payload) => apiFetch(`/api/readers/${id}/deactivate`, { method: "POST", body: JSON.stringify(payload) }),
    reactivateReader: (id, payload) => apiFetch(`/api/readers/${id}/reactivate`, { method: "POST", body: JSON.stringify(payload) }),
    resetReaderPassword: (id, payload) => apiFetch(`/api/readers/${id}/password-reset`, { method: "POST", body: JSON.stringify(payload) }),
    createReader: (payload) =>
        apiFetch("/api/readers", {
            method: "POST",
            body: JSON.stringify(payload)
        }),
    updateReader: (id, payload) =>
        apiFetch(`/api/readers/${id}`, {
            method: "PUT",
            body: JSON.stringify(payload)
        }),
    createBookCopyBatch: (payload) =>
        apiFetch("/api/book-copies/batch", {
            method: "POST",
            body: JSON.stringify(payload)
        }),
    previewBookCopyBarcodeLabels: (payload) =>
        apiFetch("/api/book-copies/barcode-labels/preview", {
            method: "POST",
            body: JSON.stringify(payload)
        }),
    printBookCopyBarcodeLabels: (payload) =>
        apiFetch("/api/book-copies/barcode-labels/print", {
            method: "POST",
            body: JSON.stringify(payload)
        }),
    updateReaderMembership: (id, payload) =>
        apiFetch(`/api/readers/${id}/membership`, {
            method: "PATCH",
            body: JSON.stringify(payload)
        }),
    deleteReader: (id, mode = "soft") =>
        apiFetch(`/api/readers/${id}?mode=${encodeURIComponent(mode)}`, {
            method: "DELETE"
        }),
    restoreReader: (id) =>
        apiFetch(`/api/readers/${id}/restore`, {
            method: "PATCH"
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
    bookStatuses: () => apiFetch("/api/options/book-statuses"),
    readerGroups: () => apiFetch("/api/options/reader-groups"),
    readerProfileStatuses: () => apiFetch("/api/options/reader-profile-statuses"),
    membershipPlans: () => apiFetch("/api/options/membership-plans"),
    paymentMethods: () => apiFetch("/api/options/payment-methods")
};

function searchCatalogOptions(entity, query, signal, limit) {
    const params = new URLSearchParams({ q: query, limit: String(limit) });
    return apiFetch(`/api/staff/catalog/${entity}/search?${params.toString()}`, { signal });
}
