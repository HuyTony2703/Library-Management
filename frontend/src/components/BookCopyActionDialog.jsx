import { useEffect, useRef, useState } from "react";
import { libraryApi } from "../api/libraryApi";
import ResultModal from "./ResultModal";

const ACTIONS = {
    MOVE_LOCATION: { title: "Chuyển vị trí", submit: "Xác nhận chuyển vị trí" },
    MARK_DAMAGED: { title: "Báo hỏng", submit: "Xác nhận báo hỏng" },
    MARK_LOST: { title: "Báo mất", submit: "Xác nhận báo mất" },
    WITHDRAW: { title: "Ngừng lưu thông", submit: "Xác nhận ngừng lưu thông" },
    RESTORE_AFTER_REPAIR: { title: "Khôi phục sau sửa chữa", submit: "Xác nhận khôi phục" },
    RESTORE_FOUND: { title: "Khôi phục sau khi tìm lại", submit: "Xác nhận khôi phục" }
};

export default function BookCopyActionDialog({ copy, action, onClose, onSuccess }) {
    const submittingRef = useRef(false);
    const config = ACTIONS[action];
    const [reason, setReason] = useState("");
    const [description, setDescription] = useState("");
    const [severity, setSeverity] = useState("");
    const [locationId, setLocationId] = useState("");
    const [locations, setLocations] = useState([]);
    const [loadingLocations, setLoadingLocations] = useState(action === "MOVE_LOCATION");
    const [locationError, setLocationError] = useState("");
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState("");
    const [retryKey, setRetryKey] = useState(0);

    useEffect(() => {
        if (action !== "MOVE_LOCATION") return undefined;
        const controller = new AbortController();
        // Location options synchronize with the copy branch and explicit retries.
        // eslint-disable-next-line react-hooks/set-state-in-effect
        setLoadingLocations(true);
        setLocationError("");
        libraryApi.bookCopyLocationFilters([copy.maChiNhanh], { signal: controller.signal })
            .then((result) => setLocations((result?.locations || []).filter((item) => item.value !== copy.maViTri)))
            .catch((requestError) => {
                if (requestError.name !== "AbortError") setLocationError(requestError.message || "Không tải được danh sách vị trí");
            })
            .finally(() => {
                if (!controller.signal.aborted) setLoadingLocations(false);
            });
        return () => controller.abort();
    }, [action, copy.maChiNhanh, copy.maViTri, retryKey]);

    async function submit(event) {
        event.preventDefault();
        if (submittingRef.current) return;
        submittingRef.current = true;
        setSubmitting(true);
        setError("");
        try {
            const result = action === "MOVE_LOCATION"
                ? await libraryApi.moveBookCopy(copy.maCuonSach, { locationId, reason: reason.trim() })
                : await libraryApi.applyBookCopyCondition(copy.maCuonSach, {
                    action,
                    severity: action === "MARK_DAMAGED" && severity ? severity : null,
                    damageTypes: [],
                    description: description.trim() || null,
                    reason: reason.trim()
                });
            onSuccess(result);
        } catch (requestError) {
            setError(requestError.message || "Không thực hiện được hành động");
        } finally {
            submittingRef.current = false;
            setSubmitting(false);
        }
    }

    return <ResultModal title={`${config.title}: ${copy.maCuonSach}`} onClose={submitting ? undefined : onClose} className="copy-action-modal">
        <form className="copy-action-form" onSubmit={submit}>
            <div className="copy-action-context">
                <strong>{copy.tenDauSach}</strong>
                <span>{copy.tenTrangThai} · {copy.tenChiNhanh}</span>
                <small>Vị trí hiện tại: {copy.viTriLabel || copy.maViTri}</small>
            </div>

            {action === "MOVE_LOCATION" && <label>Vị trí mới
                <select value={locationId} onChange={(event) => setLocationId(event.target.value)} required disabled={loadingLocations || Boolean(locationError) || submitting}>
                    <option value="">{loadingLocations ? "Đang tải vị trí..." : locations.length ? "Chọn vị trí mới" : "Không có vị trí khác phù hợp"}</option>
                    {locations.map((item) => <option key={item.value} value={item.value}>{item.label}</option>)}
                </select>
            </label>}

            {action === "MARK_DAMAGED" && <>
                <label>Mức độ hỏng (không bắt buộc)
                    <select value={severity} onChange={(event) => setSeverity(event.target.value)} disabled={submitting}>
                        <option value="">Chưa phân loại</option>
                        <option value="LOW">Nhẹ</option>
                        <option value="MEDIUM">Trung bình</option>
                        <option value="HIGH">Nặng</option>
                    </select>
                </label>
                <label>Mô tả hư hỏng (không bắt buộc)
                    <textarea value={description} onChange={(event) => setDescription(event.target.value)} maxLength="1000" disabled={submitting} />
                </label>
            </>}

            <label>Lý do
                <textarea value={reason} onChange={(event) => setReason(event.target.value)} maxLength="500" required autoFocus={action !== "MOVE_LOCATION"} disabled={submitting} />
            </label>

            {locationError && <div className="copy-action-error" role="alert"><span>{locationError}</span><button className="soft-button" type="button" onClick={() => setRetryKey((value) => value + 1)}>Thử lại</button></div>}
            {error && <div className="copy-action-error" role="alert">{error}</div>}
            <div className="copy-action-buttons">
                <button className="ghost-button" type="button" onClick={onClose} disabled={submitting}>Hủy</button>
                <button className="primary-button" disabled={submitting || loadingLocations || Boolean(locationError)}>{submitting ? "Đang xử lý..." : config.submit}</button>
            </div>
        </form>
    </ResultModal>;
}
