import { createContext, useContext, useMemo, useState } from "react";
import { CheckCircle2, XCircle, Info } from "lucide-react";

const ToastContext = createContext(null);

export function ToastProvider({ children }) {
    const [toasts, setToasts] = useState([]);

    function pushToast(type, message) {
        const id = crypto.randomUUID();

        setToasts((prev) => {
            if (prev.some((item) => item.type === type && item.message === message)) {
                return prev;
            }

            return [...prev, { id, type, message }];
        });

        setTimeout(() => {
            setToasts((prev) => prev.filter((item) => item.id !== id));
        }, 2600);
    }

    const value = useMemo(() => ({
        success: (message) => pushToast("success", message),
        error: (message) => pushToast("error", message),
        info: (message) => pushToast("info", message)
    }), []);

    return (
        <ToastContext.Provider value={value}>
            {children}

            <div className="toast-stack">
                {toasts.map((toast) => {
                    const Icon =
                        toast.type === "success" ? CheckCircle2 :
                            toast.type === "error" ? XCircle :
                                Info;

                    return (
                        <div className={`toast toast-${toast.type}`} key={toast.id}>
                            <Icon size={18} />
                            <span>{toast.message}</span>
                        </div>
                    );
                })}
            </div>
        </ToastContext.Provider>
    );
}

export function useToast() {
    return useContext(ToastContext);
}
