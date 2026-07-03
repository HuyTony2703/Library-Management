import { createContext, useContext, useEffect, useState } from "react";
import { getToken } from "../api/apiClient";
import { loginApi, logoutApi, meApi } from "../api/authApi";
import { staffApi } from "../api/staffApi";
import { isAdmin, isStaff } from "../utils/roleUtils";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
    const [user, setUser] = useState(null);
    const [staffContext, setStaffContext] = useState(null);
    const [loadingUser, setLoadingUser] = useState(true);

    async function loadStaffContext(authenticatedUser) {
        if (authenticatedUser?.mustChangePassword) {
            setStaffContext(null);
            return null;
        }
        if (!isAdmin(authenticatedUser) && !isStaff(authenticatedUser)) {
            setStaffContext(null);
            return null;
        }

        const context = await staffApi.getContext();
        setStaffContext(context);
        return context;
    }

    async function refreshUser() {
        if (!getToken()) {
            setUser(null);
            setStaffContext(null);
            localStorage.removeItem("user");
            return null;
        }

        const data = await meApi();
        await loadStaffContext(data);
        setUser(data);
        localStorage.setItem("user", JSON.stringify(data));
        return data;
    }

    useEffect(() => {
        async function loadUser() {
            try {
                await refreshUser();
            } catch {
                logoutApi();
                setUser(null);
                setStaffContext(null);
                localStorage.removeItem("user");
            } finally {
                setLoadingUser(false);
            }
        }

        loadUser();
    }, []);

    async function login(usernameOrEmail, password) {
        try {
            const data = await loginApi(usernameOrEmail, password);
            await loadStaffContext(data);
            setUser(data);
            localStorage.setItem("user", JSON.stringify(data));
            return data;
        } catch (error) {
            logoutApi();
            setUser(null);
            setStaffContext(null);
            localStorage.removeItem("user");
            throw error;
        }
    }

    async function refreshStaffContext() {
        if (!user) {
            setStaffContext(null);
            return null;
        }
        return loadStaffContext(user);
    }

    function logout() {
        logoutApi();
        localStorage.removeItem("user");
        localStorage.removeItem("token");
        setUser(null);
        setStaffContext(null);
    }

    return (
        <AuthContext.Provider
            value={{
                user,
                staffContext,
                loadingUser,
                login,
                logout,
                refreshUser,
                refreshStaffContext,
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
