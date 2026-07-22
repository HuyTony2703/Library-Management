import { EyeOff, Pencil, RotateCcw, Trash2, X } from "lucide-react";
import { useEffect, useState } from "react";
import { libraryApi } from "../api/libraryApi";
import StatusBadge from "./StatusBadge";
import { formatMoney } from "../utils/displayUtils";

const TABS = [
    { id: "overview", label: "Tổng quan" },
    { id: "copies", label: "Bản vật lý" },
    { id: "history", label: "Lịch sử" }
];

export default function BookDetailDrawer({
    bookId,
    onClose,
    onEdit,
    onDeactivate,
    onReactivate,
    onHardDelete,
    canHardDelete = false
}) {
    const [activeTab, setActiveTab] = useState("overview");
    const [book, setBook] = useState(null);
    const [tabData, setTabData] = useState({ copies: null, history: null });
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [reloadKey, setReloadKey] = useState(0);

    useEffect(() => {
        const controller = new AbortController();
        const request = activeTab === "overview"
            ? libraryApi.book(bookId, { signal: controller.signal })
            : activeTab === "copies"
                ? libraryApi.bookCopiesByBranch(bookId, { signal: controller.signal })
                : libraryApi.bookHistory(bookId, { signal: controller.signal });
        request.then((result) => {
            if (activeTab === "overview") setBook(result);
            else setTabData((current) => ({ ...current, [activeTab]: Array.isArray(result) ? result : [] }));
        }).catch((requestError) => {
            if (requestError.name !== "AbortError") setError(requestError.message || "Không tải được chi tiết đầu sách");
        }).finally(() => {
            if (!controller.signal.aborted) setLoading(false);
        });
        return () => controller.abort();
    }, [activeTab, bookId, reloadKey]);

    return (
        <div className="drawer-backdrop" role="presentation" onMouseDown={onClose}>
            <aside className="book-detail-drawer" role="dialog" aria-modal="true" aria-label="Chi tiết đầu sách" onMouseDown={(event) => event.stopPropagation()}>
                <header className="book-drawer-header">
                    <div>
                        <span>Đầu sách</span>
                        <h2>{book?.tenDauSach || bookId}</h2>
                        <small>{bookId}</small>
                    </div>
                    <button className="icon-button" type="button" onClick={onClose} aria-label="Đóng chi tiết"><X size={19} /></button>
                </header>

                {book && (
                    <div className="book-drawer-actions">
                        <button className="soft-button" type="button" onClick={() => onEdit(book)}><Pencil size={15} /> Sửa</button>
                        {book.trangThai === "Hoạt động" ? (
                            <button className="soft-button" type="button" onClick={() => onDeactivate(book)}><EyeOff size={15} /> Ngừng hiển thị</button>
                        ) : (
                            <button className="soft-button" type="button" onClick={() => onReactivate(book)}><RotateCcw size={15} /> Khôi phục</button>
                        )}
                        {canHardDelete && <button className="soft-button danger-button" type="button" onClick={() => onHardDelete(book)}><Trash2 size={15} /> Xóa cứng</button>}
                    </div>
                )}

                <nav className="book-drawer-tabs" aria-label="Nội dung chi tiết đầu sách">
                    {TABS.map((tab) => (
                        <button key={tab.id} type="button" className={activeTab === tab.id ? "is-active" : ""} onClick={() => { setLoading(true); setError(""); setActiveTab(tab.id); }}>{tab.label}</button>
                    ))}
                </nav>

                <div className="book-drawer-content">
                    {loading && <DrawerState text="Đang tải dữ liệu..." loading />}
                    {!loading && error && <DrawerState text={error} action={() => { setLoading(true); setError(""); setReloadKey((key) => key + 1); }} />}
                    {!loading && !error && activeTab === "overview" && <Overview book={book} />}
                    {!loading && !error && activeTab === "copies" && <Copies branches={tabData.copies || []} />}
                    {!loading && !error && activeTab === "history" && <History entries={tabData.history || []} />}
                </div>
            </aside>
        </div>
    );
}

function Overview({ book }) {
    if (!book) return <DrawerState text="Không tìm thấy thông tin đầu sách" />;
    return (
        <div className="book-overview">
            {book.anhBia && <img src={book.anhBia} alt={`Bìa ${book.tenDauSach}`} />}
            <div className="book-overview-grid">
                <Info label="Trạng thái"><StatusBadge value={book.trangThai} /></Info>
                <Info label="ISBN" value={book.isbn || "—"} />
                <Info label="Nhà xuất bản" value={book.tenNhaXuatBan || book.maNhaXuatBan || "—"} />
                <Info label="Năm xuất bản" value={book.namXuatBan || "—"} />
                <Info label="Ngôn ngữ" value={book.ngonNgu || "—"} />
                <Info label="Số trang" value={book.soTrang || "—"} />
                <Info label="Trị giá" value={formatMoney(book.triGia)} />
                <Info label="Tác giả" value={(book.tenTacGias || book.maTacGias || []).join(", ") || "—"} />
                <Info label="Thể loại" value={(book.tenTheLoais || book.maTheLoais || []).join(", ") || "—"} />
            </div>
            <section><h3>Mô tả</h3><p>{book.moTa || "Chưa có mô tả."}</p></section>
        </div>
    );
}

function Copies({ branches }) {
    if (!branches.length) return <DrawerState text="Không có bản vật lý trong các chi nhánh được phép xem" />;
    return <div className="book-branch-list">{branches.map((branch) => (
        <section key={branch.branchId}>
            <header><div><h3>{branch.branchName}</h3><small>{branch.branchId}</small></div><strong>{branch.availableCopies}/{branch.totalCopies} sẵn có</strong></header>
            <div className="book-copy-list">{branch.copies.map((copy) => (
                <article key={copy.id}>
                    <div><strong>{copy.barcode || copy.id}</strong><span>{copy.locationLabel || copy.locationId}</span></div>
                    <StatusBadge value={copy.statusName} />
                </article>
            ))}</div>
        </section>
    ))}</div>;
}

function History({ entries }) {
    if (!entries.length) return <DrawerState text="Chưa có lịch sử thay đổi" />;
    return <div className="book-history">{entries.map((entry) => {
        const detail = parseAudit(entry.moTaChiTiet);
        return <article key={entry.maNhatKy}>
            <div><strong>{entry.hanhDong}</strong><time>{formatDateTime(entry.thoiGian)}</time></div>
            <span>Tài khoản: {entry.maTaiKhoan || "Hệ thống"}</span>
            {detail?.reason && <p><b>Lý do:</b> {detail.reason}</p>}
            {detail && <div className="audit-before-after"><code>Trước: {displayStatus(detail.before)}</code><code>Sau: {displayStatus(detail.after)}</code></div>}
            {!detail && entry.moTaChiTiet && <p>{entry.moTaChiTiet}</p>}
        </article>;
    })}</div>;
}

function Info({ label, value, children }) { return <div><span>{label}</span>{children || <strong>{value}</strong>}</div>; }
function DrawerState({ text, loading = false, action }) { return <div className="book-drawer-state">{loading && <span className="table-spinner" />}<p>{text}</p>{action && <button className="soft-button" type="button" onClick={action}>Thử lại</button>}</div>; }
function parseAudit(value) { try { return value ? JSON.parse(value) : null; } catch { return null; } }
function displayStatus(value) { return value && Object.keys(value).length ? value.status || JSON.stringify(value) : "—"; }
function formatDateTime(value) { return value ? new Intl.DateTimeFormat("vi-VN", { dateStyle: "short", timeStyle: "short" }).format(new Date(value)) : "—"; }
