import { createContext, useContext, useMemo, useRef, useState } from "react";

const ActionDialogContext = createContext(null);

export function ActionDialogProvider({ children }) {
    const [dialog, setDialog] = useState(null);
    const resolverRef = useRef(null);

    function close(value) {
        if (resolverRef.current) {
            resolverRef.current(value);
        }

        resolverRef.current = null;
        setDialog(null);
    }

    function open(config) {
        return new Promise((resolve) => {
            resolverRef.current = resolve;
            setDialog(config);
        });
    }

    const value = useMemo(() => ({
        confirm: ({ title, message, confirmLabel = "Xác nhận", cancelLabel = "Hủy", danger = false, cancelPrimary = false }) =>
            open({
                type: "confirm",
                title,
                message,
                confirmLabel,
                cancelLabel,
                danger,
                cancelPrimary
            }),
        prompt: ({ title, message, defaultValue = "", confirmLabel = "Xác nhận", cancelLabel = "Hủy" }) =>
            open({
                type: "prompt",
                title,
                message,
                defaultValue,
                confirmLabel,
                cancelLabel
            }),
        chooseDeleteMode: async (label) => {
            const mode = await open({
                type: "choice",
                title: `Xóa ${label}`,
                message: "Chọn cách xử lý dữ liệu này.",
                choices: [
                    { value: "soft", label: "Ngừng hoạt động/hiển thị" },
                    { value: "hard", label: "Xóa vĩnh viễn", danger: true }
                ],
                cancelLabel: "Hủy"
            });

            if (mode !== "hard") {
                return mode;
            }

            const confirmed = await open({
                type: "confirm",
                title: `Xóa vĩnh viễn ${label}`,
                message: "Thao tác này chỉ thành công nếu dữ liệu chưa có liên kết nghiệp vụ. Bạn chắc chắn muốn tiếp tục?",
                confirmLabel: "Xóa vĩnh viễn",
                cancelLabel: "Hủy",
                danger: true
            });

            return confirmed ? "hard" : null;
        }
    }), []);

    return (
        <ActionDialogContext.Provider value={value}>
            {children}
            {dialog && <ActionDialog dialog={dialog} onClose={close} />}
        </ActionDialogContext.Provider>
    );
}

export function useActionDialog() {
    const context = useContext(ActionDialogContext);

    if (!context) {
        throw new Error("useActionDialog must be used inside ActionDialogProvider");
    }

    return context;
}

function ActionDialog({ dialog, onClose }) {
    const [inputValue, setInputValue] = useState(dialog.defaultValue || "");

    function submitPrompt(event) {
        event.preventDefault();
        onClose(inputValue);
    }

    return (
        <div className="modal-backdrop" role="presentation" onMouseDown={() => onClose(null)}>
            <div
                className="modal-card action-dialog-card"
                role="dialog"
                aria-modal="true"
                aria-label={dialog.title}
                onMouseDown={(event) => event.stopPropagation()}
            >
                <h2>{dialog.title}</h2>
                {dialog.message && <p className="muted-text">{dialog.message}</p>}

                {dialog.type === "choice" && (
                    <div className="action-dialog-actions">
                        {dialog.choices.map((choice) => (
                            <button
                                key={choice.value}
                                type="button"
                                className={choice.danger ? "soft-button danger-button" : "soft-button"}
                                onClick={() => onClose(choice.value)}
                            >
                                {choice.label}
                            </button>
                        ))}
                        <button type="button" className="ghost-button" onClick={() => onClose(null)}>
                            {dialog.cancelLabel}
                        </button>
                    </div>
                )}

                {dialog.type === "confirm" && (
                    <div className="action-dialog-actions">
                        <button
                            type="button"
                            className={dialog.danger ? "soft-button danger-button" : "primary-button"}
                            onClick={() => onClose(true)}
                        >
                            {dialog.confirmLabel}
                        </button>
                        <button
                            type="button"
                            className={dialog.cancelPrimary ? "primary-button" : "ghost-button"}
                            onClick={() => onClose(false)}
                        >
                            {dialog.cancelLabel}
                        </button>
                    </div>
                )}

                {dialog.type === "prompt" && (
                    <form className="action-dialog-form" onSubmit={submitPrompt}>
                        <input
                            autoFocus
                            value={inputValue}
                            onChange={(event) => setInputValue(event.target.value)}
                        />
                        <div className="action-dialog-actions">
                            <button type="submit" className="primary-button">
                                {dialog.confirmLabel}
                            </button>
                            <button type="button" className="ghost-button" onClick={() => onClose(null)}>
                                {dialog.cancelLabel}
                            </button>
                        </div>
                    </form>
                )}
            </div>
        </div>
    );
}
