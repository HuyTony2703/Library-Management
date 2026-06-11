import React from "react";
import ReactDOM from "react-dom/client";
import { HashRouter } from "react-router-dom";
import App from "./App";
import { ActionDialogProvider } from "./components/ActionDialogProvider";
import { ToastProvider } from "./components/ToastProvider";
import { AuthProvider } from "./context/AuthContext";
import "./index.css";

applyStoredPreferences();

ReactDOM.createRoot(document.getElementById("root")).render(
    <React.StrictMode>
        <HashRouter>
            <ToastProvider>
                <ActionDialogProvider>
                    <AuthProvider>
                        <App />
                    </AuthProvider>
                </ActionDialogProvider>
            </ToastProvider>
        </HashRouter>
    </React.StrictMode>
);

function applyStoredPreferences() {
    try {
        const preferences = JSON.parse(localStorage.getItem("library_ui_preferences") || "{}");
        const theme = preferences.theme === "system"
            ? (window.matchMedia?.("(prefers-color-scheme: dark)").matches ? "dark" : "light")
            : preferences.theme;

        document.documentElement.dataset.theme = theme || "light";
        document.body.dataset.density = preferences.density || "comfortable";
        document.documentElement.dataset.accent = preferences.accent || "blue";
    } catch {
        document.documentElement.dataset.theme = "light";
        document.body.dataset.density = "comfortable";
        document.documentElement.dataset.accent = "blue";
    }
}
