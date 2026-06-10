import React from "react";
import ReactDOM from "react-dom/client";
import { HashRouter } from "react-router-dom";
import App from "./App";
import { ActionDialogProvider } from "./components/ActionDialogProvider";
import { ToastProvider } from "./components/ToastProvider";
import { AuthProvider } from "./context/AuthContext";
import "./index.css";

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
