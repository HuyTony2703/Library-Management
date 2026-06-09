const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

export class ApiError extends Error {
    constructor(message, status, data) {
        super(message);
        this.name = "ApiError";
        this.status = status;
        this.data = data;
    }
}

export function getToken() {
    return localStorage.getItem("library_token");
}

export function setToken(token) {
    localStorage.setItem("library_token", token);
}

export function clearToken() {
    localStorage.removeItem("library_token");
}

export async function apiFetch(path, options = {}) {
    const token = getToken();

    const headers = {
        ...(options.body ? { "Content-Type": "application/json" } : {}),
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...(options.headers || {})
    };

    const response = await fetch(`${API_BASE_URL}${path}`, {
        ...options,
        headers
    });

    const contentType = response.headers.get("content-type") || "";
    const data = contentType.includes("application/json")
        ? await response.json().catch(() => null)
        : await response.text().catch(() => null);

    if (!response.ok) {
        const message = data?.message || data?.error || `Lỗi API ${response.status}`;

        if (response.status === 401) {
            clearToken();
        }

        throw new ApiError(message, response.status, data);
    }

    return data;
}
