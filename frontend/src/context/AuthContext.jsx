import { createContext, useContext, useEffect, useState } from "react";
import { getToken } from "../api/apiClient";
import { loginApi, logoutApi, meApi } from "../api/authApi";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
    const [user, setUser] = useState(null);
    const [loadingUser, setLoadingUser] = useState(true);

    async function refreshUser() {
        if (!getToken()) {
            setUser(null);
            localStorage.removeItem("user");
            return null;
        }

        const data = await meApi();
        setUser(data);
        localStorage.setItem("user", JSON.stringify(data));
        return data;
    }

    useEffect(() => {
        async function loadUser() {
            try {
                await refreshUser();
            } catch {
                setUser(null);
                localStorage.removeItem("user");
            } finally {
                setLoadingUser(false);
            }
        }

        loadUser();
    }, []);

    async function login(usernameOrEmail, password) {
        const data = await loginApi(usernameOrEmail, password);
        setUser(data);
        localStorage.setItem("user", JSON.stringify(data));
        return data;
    }

    function logout() {
        logoutApi();
        localStorage.removeItem("user");
        localStorage.removeItem("token");
        setUser(null);
    }

    return (
        <AuthContext.Provider
            value={{
                user,
                loadingUser,
                login,
                logout,
                refreshUser,
                isAuthenticated: Boolean(user || getToken())
            }}
        >
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    const context = useContext(AuthContext);

    if (!context) {
        throw new Error("useAuth must be used inside AuthProvider");
    }

    return context;
}
