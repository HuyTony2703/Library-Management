import { Check, Copy, KeyRound } from "lucide-react";
import { useRef, useState } from "react";
import { libraryApi } from "../api/libraryApi";
import ResultModal from "./ResultModal";

export default function ReaderPasswordResetDialog({ reader, onClose }) {
    const submittingRef = useRef(false);
    const [verificationEmail, setVerificationEmail] = useState("");
    const [verificationDateOfBirth, setVerificationDateOfBirth] = useState("");
    const [reason, setReason] = useState("");
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState("");
    const [result, setResult] = useState(null);
    const [copied, setCopied] = useState(false);

    async function submit(event) {
        event.preventDefault();
        if (submittingRef.current) return;
        submittingRef.current = true; setSubmitting(true); setError("");
        try {
            setResult(await libraryApi.resetReaderPassword(reader.readerId, {
                mode: "GENERATE_TEMPORARY", forceChange: true, revokeSessions: true,
                reason: reason.trim(), verificationEmail: verificationEmail.trim(),
                verificationDateOfBirth
            }));
        } catch (requestError) {
            setError(requestError.message || "Không reset được mật khẩu độc giả");
        } finally {
            submittingRef.current = false; setSubmitting(false);
        }
    }

    async function copySecret() {
        try {
            await navigator.clipboard.writeText(result.temporaryPassword);
            setCopied(true);
        } catch {
            setError("Không truy cập được clipboard; hãy sao chép thủ công");
        }
    }

    return <ResultModal title={`Reset mật khẩu: ${reader.readerId}`} onClose={submitting ? undefined : onClose} className="reader-password-reset-modal">
        {!result ? <form className="copy-action-form" onSubmit={submit}>
            <div className="copy-action-context"><strong>{reader.fullName}</strong><span>Xác minh tối thiểu hai thuộc tính trước khi reset</span></div>
            <label>Email độc giả để xác minh<input type="email" autoComplete="off" required maxLength="255" value={verificationEmail} onChange={(event) => setVerificationEmail(event.target.value)} disabled={submitting} /></label>
            <label>Ngày sinh để xác minh<input type="date" required value={verificationDateOfBirth} onChange={(event) => setVerificationDateOfBirth(event.target.value)} disabled={submitting} /></label>
            <label>Lý do<textarea autoFocus required maxLength="500" value={reason} onChange={(event) => setReason(event.target.value)} disabled={submitting} /></label>
            <p className="reader-reset-warning">Thao tác sẽ thu hồi mọi token cũ và buộc độc giả đổi mật khẩu ở lần đăng nhập tiếp theo.</p>
            {error && <div className="copy-action-error" role="alert">{error}</div>}
            <div className="copy-action-buttons"><button className="ghost-button" type="button" onClick={onClose} disabled={submitting}>Hủy</button><button className="primary-button" disabled={submitting}>{submitting ? "Đang reset..." : "Tạo mật khẩu tạm"}</button></div>
        </form> : <div className="reader-reset-result">
            <KeyRound size={30} /><strong>Mật khẩu tạm chỉ hiển thị lần này</strong>
            <code>{result.temporaryPassword}</code>
            <button className="soft-button" type="button" onClick={copySecret}>{copied ? <Check size={17} /> : <Copy size={17} />}{copied ? "Đã sao chép" : "Sao chép"}</button>
            <p>Hãy giao trực tiếp qua kênh đã xác minh. Đóng cửa sổ sẽ không thể xem lại mật khẩu này.</p>
            {error && <div className="copy-action-error" role="alert">{error}</div>}
            <div className="copy-action-buttons"><button className="primary-button" type="button" onClick={onClose}>Tôi đã lưu mật khẩu tạm</button></div>
        </div>}
    </ResultModal>;
}
