import { apiFetch } from "./apiClient";

function buildQuery(params = {}) {
  const searchParams = new URLSearchParams();

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      searchParams.append(key, value);
    }
  });

  const query = searchParams.toString();
  return query ? `?${query}` : "";
}

export const readerApi = {
  // Profile
  me: () => apiFetch("/api/reader/me"),

  // Books
  books: (params = {}) => apiFetch(`/api/reader/books${buildQuery(params)}`),
  bookDetail: (maDauSach) => apiFetch(`/api/reader/books/${maDauSach}`),
  bookCopies: (maDauSach) => apiFetch(`/api/reader/books/${maDauSach}/copies`),

  // Current loans + renewal
  currentLoans: () => apiFetch("/api/reader/loans/current"),
  renewalHistory: () => apiFetch("/api/reader/loans/renewal-history"),
  renewLoan: (maChiTietMuon) =>
    apiFetch(`/api/reader/loans/${maChiTietMuon}/renew`, {
      method: "POST"
    }),

  // Reservations
  reservations: () => apiFetch("/api/reader/reservations"),
  reserveByTitle: (payload) =>
    apiFetch("/api/reader/reservations/by-title", {
      method: "POST",
      body: JSON.stringify(payload)
    }),
  reserveByCopy: (payload) =>
    apiFetch("/api/reader/reservations/by-copy", {
      method: "POST",
      body: JSON.stringify(payload)
    }),
  cancelReservation: (maPhieuDatTruoc) =>
    apiFetch(`/api/reader/reservations/${maPhieuDatTruoc}`, {
      method: "DELETE"
    }),

  // Notifications
  notifications: () => apiFetch("/api/reader/notifications"),
  getNotifications: () => apiFetch("/api/reader/notifications"),
  unreadNotificationCount: () => apiFetch("/api/reader/notifications/unread-count"),
  getUnreadNotificationCount: () => apiFetch("/api/reader/notifications/unread-count"),
  markNotificationRead: (maThongBao) =>
    apiFetch(`/api/reader/notifications/${maThongBao}/read`, {
      method: "PATCH"
    }),
  markNotificationAsRead: (maThongBao) =>
    apiFetch(`/api/reader/notifications/${maThongBao}/read`, {
      method: "PATCH"
    }),
  markAllNotificationsRead: () =>
    apiFetch("/api/reader/notifications/read-all", {
      method: "PATCH"
    }),
  markAllNotificationsAsRead: () =>
    apiFetch("/api/reader/notifications/read-all", {
      method: "PATCH"
    }),

  // Membership
  currentMembership: () => apiFetch("/api/reader/membership/current"),
  getCurrentMembership: () => apiFetch("/api/reader/membership/current"),
  membershipPlans: () => apiFetch("/api/reader/membership/plans"),
  getMembershipPlans: () => apiFetch("/api/reader/membership/plans"),
  membershipHistory: () => apiFetch("/api/reader/membership/history"),
  getMembershipHistory: () => apiFetch("/api/reader/membership/history"),
  purchaseMembership: (payload) =>
    apiFetch("/api/reader/membership/purchase", {
      method: "POST",
      body: JSON.stringify(payload)
    }),

  // Comments and ratings
  comments: (maDauSach) => apiFetch(`/api/reader/books/${maDauSach}/comments`),
  getBookComments: (maDauSach) => apiFetch(`/api/reader/books/${maDauSach}/comments`),
  createComment: (maDauSach, payload) =>
    apiFetch(`/api/reader/books/${maDauSach}/comments`, {
      method: "POST",
      body: JSON.stringify(payload)
    }),
  createBookComment: (maDauSach, payload) =>
    apiFetch(`/api/reader/books/${maDauSach}/comments`, {
      method: "POST",
      body: JSON.stringify(payload)
    }),
  updateComment: (maBinhLuan, payload) =>
    apiFetch(`/api/reader/comments/${maBinhLuan}`, {
      method: "PUT",
      body: JSON.stringify(payload)
    }),
  deleteComment: (maBinhLuan) =>
    apiFetch(`/api/reader/comments/${maBinhLuan}`, {
      method: "DELETE"
    }),
  ratingSummary: (maDauSach) => apiFetch(`/api/reader/books/${maDauSach}/ratings/summary`),
  getRatingSummary: (maDauSach) => apiFetch(`/api/reader/books/${maDauSach}/ratings/summary`),
  rateBook: (maDauSach, payload) =>
    apiFetch(`/api/reader/books/${maDauSach}/ratings`, {
      method: "POST",
      body: JSON.stringify(payload)
    }),
  createRating: (maDauSach, payload) =>
    apiFetch(`/api/reader/books/${maDauSach}/ratings`, {
      method: "POST",
      body: JSON.stringify(payload)
    }),
  updateMyRating: (maDauSach, payload) =>
    apiFetch(`/api/reader/books/${maDauSach}/ratings/me`, {
      method: "PUT",
      body: JSON.stringify(payload)
    }),

  // Favorites
  favorites: () => apiFetch("/api/reader/favorites"),
  addFavorite: (maDauSach) =>
    apiFetch(`/api/reader/favorites/${maDauSach}`, {
      method: "POST"
    }),
  removeFavorite: (maDauSach) =>
    apiFetch(`/api/reader/favorites/${maDauSach}`, {
      method: "DELETE"
    }),
  favoriteExists: (maDauSach) => apiFetch(`/api/reader/favorites/${maDauSach}/exists`),

  // Recommendations
  randomRecommendations: (limit = 6) =>
    apiFetch(`/api/reader/recommendations/random?limit=${limit}`),

  // Rules and guide
  currentRules: () => apiFetch("/api/reader/rules/current")
};
