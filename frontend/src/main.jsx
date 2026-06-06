import React from "react";
import ReactDOM from "react-dom/client";
import { HashRouter } from "react-router-dom";
import App from "./App";
import DesktopBootGate from "./components/DesktopBootGate";
import { ToastProvider } from "./components/ToastProvider";
import { AuthProvider } from "./context/AuthContext";
import "./index.css";

ReactDOM.createRoot(document.getElementById("root")).render(
    <React.StrictMode>
        <HashRouter>
            <ToastProvider>
                <DesktopBootGate>
                    <AuthProvider>
                        <App />
                    </AuthProvider>
                </DesktopBootGate>
            </ToastProvider>
        </HashRouter>
    </React.StrictMode>
);
