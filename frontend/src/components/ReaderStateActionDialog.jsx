import { useRef, useState } from "react";
import { libraryApi } from "../api/libraryApi";
import ResultModal from "./ResultModal";

const CONFIG = {
    LOCK_BORROWING: { title: "Khóa quyền mượn", submit: "Xác nhận khóa", scopes: ["BORROWING"] },
    LOCK_LOGIN: { title: "Khóa đăng nhập", submit: "Xác nhận khóa", scopes: ["LOGIN"] },
    UNLOCK: { title: "Mở khóa", submit: "Xác nhận mở khóa" },
    DEACTIVATE: { title: "Ngừng hoạt động", submit: "Xác nhận ngừng hoạt động" },
    REACTIVATE: { title: "Khôi phục hồ sơ", submit: "Xác nhận khôi phục" }
};

export default function ReaderStateActionDialog({ reader, action, activeLocks, onClose, onSuccess }) {
    const submittingRef = useRef(false);
    const config = CONFIG[action];
    const [reason, setReason] = useState("");
    const [note, setNote] = useState("");
    const [lockedUntil, setLockedUntil] = useState("");
    const [unlockScopes, setUnlockScopes] = useState(() => activeLocks.map((item) => item.scope));
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState("");

    function toggleScope(scope) {
        setUnlockScopes((current) => current.includes(scope)
            ? current.filter((item) => item !== scope)
            : [...current, scope]);
    }

    async function submit(event) {
        event.preventDefault();
        if (submittingRef.current) return;
        submittingRef.current = true;
        setSubmitting(true);
        setError("");
        try {
            let result;
            if (action.startsWith("LOCK_")) {
                result = await libraryApi.lockReader(reader.readerId, {
                    scopes: config.scopes,
                    reason: reason.trim(),
                    lockedUntil: lockedUntil || null,
                    note: note.trim() || null
                });
            } else if (action === "UNLOCK") {
                result = await libraryApi.unlockReader(reader.readerId, { scopes: unlockScopes, reason: reason.trim() });
            } else if (action === "DEACTIVATE") {
                result = await libraryApi.deactivateReader(reader.readerId, { reason: reason.trim() });
            } else {
                result = await libraryApi.reactivateReader(reader.readerId, { reason: reason.trim() });
            }
            onSuccess(result);
        } catch (requestError) {
            setError(requestError.message || "Không thực hiện được hành động");
        } finally {
            submittingRef.current = false;
            setSubmitting(false);
        }
    }

    const lockAction = action.startsWith("LOCK_");
    return <ResultModal title={`${config.title}: ${reader.readerId}`} onClose={submitting ? undefined : onClose} className="reader-state-modal">
        <form className="copy-action-form" onSubmit={submit}>
            <div className="copy-action-context"><strong>{reader.fullName}</strong><span>Hồ sơ: {reader.profileStatus}</span></div>
            {lockAction && <>
                <label>Khóa đến ngày (để trống nếu không thời hạn)
                    <input type="date" min={new Date().toISOString().slice(0, 10)} value={lockedUntil} onChange={(event) => setLockedUntil(event.target.value)} disabled={submitting} />
                </label>
                <label>Ghi chú (không bắt buộc)
                    <textarea maxLength="1000" value={note} onChange={(event) => setNote(event.target.value)} disabled={submitting} />
                </label>
            </>}
            {action === "UNLOCK" && <fieldset className="reader-scope-options"><legend>Phạm vi mở khóa</legend>
                {activeLocks.map((lock) => <label key={lock.scope}><input type="checkbox" checked={unlockScopes.includes(lock.scope)} onChange={() => toggleScope(lock.scope)} disabled={submitting} />{scopeLabel(lock.scope)}</label>)}
            </fieldset>}
            <label>Lý do
                <textarea autoFocus maxLength="500" required value={reason} onChange={(event) => setReason(event.target.value)} disabled={submitting} />
            </label>
            {error && <div className="copy-action-error" role="alert">{error}</div>}
            <div className="copy-action-buttons">
                <button className="ghost-button" type="button" onClick={onClose} disabled={submitting}>Hủy</button>
                <button className="primary-button" disabled={submitting || (action === "UNLOCK" && !unlockScopes.length)}>{submitting ? "Đang xử lý..." : config.submit}</button>
            </div>
        </form>
    </ResultModal>;
}

function scopeLabel(scope) { return scope === "BORROWING" ? "Quyền mượn" : "Đăng nhập"; }
