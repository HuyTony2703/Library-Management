import { Pencil, X } from "lucide-react";
import { useEffect, useState } from "react";
import { libraryApi } from "../api/libraryApi";
import { formatDate } from "../utils/displayUtils";
import StatusBadge from "./StatusBadge";

export default function BookCopyDetailDrawer({ copyId, onClose, onEdit }) {
    const [copy, setCopy] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [retryKey, setRetryKey] = useState(0);

    useEffect(() => {
        const controller = new AbortController();
        libraryApi.bookCopy(copyId, { signal: controller.signal })
            .then(setCopy)
            .catch((requestError) => {
                if (requestError.name !== "AbortError") setError(requestError.message || "Không tải được chi tiết cuốn sách");
            })
            .finally(() => {
                if (!controller.signal.aborted) setLoading(false);
            });
        return () => controller.abort();
    }, [copyId, retryKey]);

    function retry() {
        setLoading(true);
        setError("");
        setRetryKey((value) => value + 1);
    }

    return (
        <div className="drawer-backdrop" role="presentation" onMouseDown={onClose}>
            <aside className="copy-detail-drawer" role="dialog" aria-modal="true" aria-label="Chi tiết cuốn sách" onMouseDown={(event) => event.stopPropagation()}>
                <header>
                    <div><span>Cuốn sách</span><h2>{copy?.tenDauSach || copyId}</h2><small>{copyId}</small></div>
                    <button className="icon-button" type="button" onClick={onClose} aria-label="Đóng chi tiết"><X size={19} /></button>
                </header>

                {loading && <DrawerState loading text="Đang tải chi tiết..." />}
                {!loading && error && <DrawerState text={error} action={retry} />}
                {!loading && !error && !copy && <DrawerState text="Không tìm thấy cuốn sách" />}
                {!loading && !error && copy && (
                    <div className="copy-detail-content">
                        <button className="soft-button copy-detail-edit" type="button" onClick={() => onEdit(copy)}><Pencil size={15} /> Sửa thông tin</button>
                        <section>
                            <h3>Nhận diện</h3>
                            <div className="copy-detail-grid">
                                <Info label="Mã cuốn" value={copy.maCuonSach} />
                                <Info label="Trạng thái"><StatusBadge value={copy.tenTrangThai} /></Info>
                                <Info label="Đầu sách" value={copy.tenDauSach} secondary={[copy.maDauSach, copy.isbn].filter(Boolean).join(" · ")} />
                                <Info label="Ngày nhập" value={formatDate(copy.ngayNhapSach)} />
                                <Info label="Barcode" value={copy.maVach || "Chưa có"} />
                                <Info label="QR" value={copy.maQrCode || "Chưa có"} />
                            </div>
                        </section>
                        <section>
                            <h3>Chi nhánh và vị trí</h3>
                            <div className="copy-detail-grid">
                                <Info label="Chi nhánh" value={copy.tenChiNhanh} secondary={copy.maChiNhanh} />
                                <Info label="Khu / kho" value={copy.tenKhu || "—"} secondary={copy.maKhu} />
                                <Info label="Kệ" value={copy.tenKeSach || "—"} secondary={copy.maKeSach} />
                                <Info label="Vị trí" value={copy.viTriLabel || "—"} secondary={copy.maViTri} />
                            </div>
                        </section>
                        <section><h3>Ghi chú</h3><p>{copy.ghiChu || "Chưa có ghi chú."}</p></section>
                    </div>
                )}
            </aside>
        </div>
    );
}

function Info({ label, value, secondary, children }) {
    return <div><span>{label}</span>{children || <strong>{value || "—"}</strong>}{secondary && <small>{secondary}</small>}</div>;
}

function DrawerState({ text, loading = false, action }) {
    return <div className="book-drawer-state">{loading && <span className="table-spinner" />}<p>{text}</p>{action && <button className="soft-button" type="button" onClick={action}>Thử lại</button>}</div>;
}
