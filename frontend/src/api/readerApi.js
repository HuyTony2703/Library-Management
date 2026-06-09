import { apiFetch } from "./apiClient";

export const readerApi = {
    me: () => apiFetch("/api/reader/me")
};
