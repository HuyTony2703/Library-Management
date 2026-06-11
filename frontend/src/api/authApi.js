import { apiFetch, clearToken, setToken } from "./apiClient";

export async function loginApi(usernameOrEmail, password) {
    const data = await apiFetch("/api/auth/login", {
        method: "POST",
        body: JSON.stringify({ usernameOrEmail, password })
    });

    if (data?.token) {
        setToken(data.token);
    }

    return data;
}

export function meApi() {
    return apiFetch("/api/auth/me");
}

export function changePasswordApi(payload) {
    return apiFetch("/api/auth/change-password", {
        method: "POST",
        body: JSON.stringify(payload)
    });
}

export function updateProfileApi(payload) {
    return apiFetch("/api/auth/profile", {
        method: "PUT",
        body: JSON.stringify(payload)
    });
}

export function logoutApi() {
    clearToken();
}
