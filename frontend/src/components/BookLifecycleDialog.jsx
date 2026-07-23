import { useState } from "react";
import ResultModal from "./ResultModal";

export default function BookLifecycleDialog({ book, action, onClose, onSubmit }) {
    const [reason, setReason] = useState("");
    const [error, setError] = useState("");
    const [submitting, setSubmitting] = useState(false);
    const deactivate = action === "deactivate";

    async function submit(event) {
        event.preventDefault();
        const normalized = reason.trim();
        if (!normalized) {
            setError("Vui lòng nhập lý do");
            return;
        }
        if (submitting) return;
        setSubmitting(true);
        setError("");
        try {
            await onSubmit(normalized);
        } catch (submitError) {
            setError(submitError.data?.fieldErrors?.reason || submitError.message || "Không cập nhật được trạng thái");
        } finally {
            setSubmitting(false);
        }
    }

    return (
        <ResultModal title={deactivate ? "Ngừng hiển thị đầu sách" : "Khôi phục đầu sách"} onClose={() => !submitting && onClose()} className="lifecycle-dialog-card">
            <form className="book-lifecycle-form" onSubmit={submit}>
                <p><strong>{book.tenDauSach}</strong><br /><span>{book.maDauSach}</span></p>
                <label>Lý do *</label>
                <textarea rows={4} maxLength={500} value={reason} onChange={(event) => { setReason(event.target.value); setError(""); }} autoFocus />
                {error && <span className="field-error" role="alert">{error}</span>}
                <div>
                    <button className="ghost-button" type="button" onClick={onClose} disabled={submitting}>Hủy</button>
                    <button className="primary-button" disabled={submitting}>{submitting ? "Đang xử lý..." : deactivate ? "Ngừng hiển thị" : "Khôi phục"}</button>
                </div>
            </form>
        </ResultModal>
    );
}
