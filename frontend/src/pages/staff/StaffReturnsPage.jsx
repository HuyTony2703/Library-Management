import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { CheckSquare, RefreshCw, ScanLine, Trash2 } from "lucide-react";
import { staffApi } from "../../api/staffApi";
import PageHeader from "../../components/PageHeader";
import DataTable from "../../components/DataTable";
import ResultModal from "../../components/ResultModal";
import StatusBadge from "../../components/StatusBadge";
import { useToast } from "../../components/ToastProvider";
import { useAuth } from "../../context/AuthContext";
import { formatDateTime, formatMoney } from "../../utils/displayUtils";

const RETURN_NORMAL = "B\u00ecnh th\u01b0\u1eddng";
const RETURN_DAMAGED = "H\u1ecfng";
const RETURN_LOST = "M\u1ea5t";

const DAMAGE_TYPES = [
    { value: "BIA", label: "Bia" },
    { value: "TRANG", label: "Trang" },
    { value: "GAY", label: "Gãy sách" },
    { value: "NUOC", label: "Ướt/bẩn" },
    { value: "KHAC", label: "Khác" }
];

const DAMAGE_SEVERITIES = [
    { value: "LOW", label: "Nhẹ" },
    { value: "MEDIUM", label: "Vừa" },
    { value: "HIGH", label: "Nặng" }
];

export default function StaffReturnsPage() {
    const toast = useToast();
    const navigate = useNavigate();
    const { staffContext } = useAuth();
    const [scanCode, setScanCode] = useState("");
    const [scanLoading, setScanLoading] = useState(false);
    const [scanError, setScanError] = useState("");
    const [selectedReader, setSelectedReader] = useState(null);
    const [openLoans, setOpenLoans] = useState([]);
    const [openLoansLoading, setOpenLoansLoading] = useState(false);
    const [openLoansError, setOpenLoansError] = useState("");
    const [cart, setCart] = useState([]);
    const [note, setNote] = useState("");
    const [preview, setPreview] = useState(null);
    const [previewLoading, setPreviewLoading] = useState(false);
    const [previewError, setPreviewError] = useState("");
    const [returnIdempotencyKey, setReturnIdempotencyKey] = useState("");
    const [submitting, setSubmitting] = useState(false);
    const [result, setResult] = useState(null);
    const [showResult, setShowResult] = useState(false);

    async function refreshOpenLoans(readerId) {
        setOpenLoansLoading(true);
        setOpenLoansError("");
        try {
            const rows = await staffApi.getReaderOpenLoans(readerId);
            setOpenLoans(Array.isArray(rows) ? rows : []);
        } catch (error) {
            setOpenLoans([]);
            setOpenLoansError(error.message || "Không tải được sách đang mượn của độc giả");
        } finally {
            setOpenLoansLoading(false);
        }
    }

    async function refreshPreview(nextCart = cart, nextReader = selectedReader, nextNote = note) {
        if (!nextReader || nextCart.length === 0) {
            setPreview(null);
            setPreviewError("");
            return;
        }

        setPreviewLoading(true);
        setPreviewError("");
        try {
            const data = await staffApi.previewReturn(buildReturnPayload(nextCart, nextReader, nextNote));
            setPreview(data);
        } catch (error) {
            setPreview(null);
            setPreviewError(error.message || "Không tính được preview phiếu trả");
        } finally {
            setPreviewLoading(false);
        }
    }

    async function handleScanSubmit(event) {
        event.preventDefault();
        const code = scanCode.trim();
        if (!code || scanLoading) return;

        setScanLoading(true);
        setScanError("");
        try {
            const item = await staffApi.getOpenLoanByCode(code);
            await addReturnItem(item);
            setScanCode("");
        } catch (error) {
            setScanError(error.message || "Không tìm thấy sách đang mượn theo mã quét");
        } finally {
            setScanLoading(false);
        }
    }

    async function addReturnItem(item) {
        if (!item?.loanDetailId) return;

        if (selectedReader && selectedReader.id !== item.readerId) {
            const message = "Không thể trộn nhiều độc giả trong một phiếu trả";
            setScanError(message);
            toast.error(message);
            return;
        }

        if (cart.length > 0 && cart[0].branchId !== item.branchId) {
            const message = "Các cuốn trong giỏ trả phải cùng chi nhánh mượn";
            setScanError(message);
            toast.error(message);
            return;
        }

        if (cart.some((row) => row.loanDetailId === item.loanDetailId)) {
            toast.info("Chi tiết mượn đã có trong giỏ trả");
            return;
        }

        const reader = selectedReader || { id: item.readerId, name: item.readerName };
        if (!selectedReader) {
            setSelectedReader(reader);
            await refreshOpenLoans(reader.id);
        }

        const nextCart = [...cart, withDefaultReturnState(item)];
        setCart(nextCart);
        await refreshPreview(nextCart, reader);
    }

    function toggleLoan(row) {
        if (cart.some((item) => item.loanDetailId === row.loanDetailId)) {
            removeFromCart(row.loanDetailId);
            return;
        }
        addReturnItem(row);
    }

    function removeFromCart(loanDetailId) {
        const nextCart = cart.filter((row) => row.loanDetailId !== loanDetailId);
        setCart(nextCart);
        refreshPreview(nextCart);
    }

    function updateCartItem(loanDetailId, patch) {
        const nextCart = cart.map((row) => {
            if (row.loanDetailId !== loanDetailId) return row;
            const nextRow = { ...row, ...patch };
            if (patch.returnCondition === RETURN_NORMAL) {
                nextRow.damageTypes = [];
                nextRow.damageSeverity = "";
                nextRow.damageDescription = "";
                nextRow.adjustmentFine = "";
                nextRow.adjustmentReason = "";
            }
            if (patch.returnCondition === RETURN_LOST) {
                nextRow.damageTypes = [];
                nextRow.damageSeverity = "";
            }
            return nextRow;
        });
        setCart(nextCart);
        refreshPreview(nextCart);
    }

    function resetTransaction() {
        setScanCode("");
        setScanError("");
        setSelectedReader(null);
        setOpenLoans([]);
        setOpenLoansError("");
        setCart([]);
        setNote("");
        setPreview(null);
        setPreviewError("");
        setReturnIdempotencyKey("");
        setResult(null);
        setShowResult(false);
    }

    async function handleSubmit(event) {
        event.preventDefault();
        if (submitting) return;

        if (!staffContext?.operational) {
            toast.error("Tài khoản chưa có staff context hợp lệ để nhận trả sách");
            return;
        }
        if (!selectedReader || cart.length === 0) {
            toast.error("Vui lòng quét hoặc chọn ít nhất một sách đang mượn");
            return;
        }
        if (previewLoading) {
            toast.info("Đang tính preview phiếu trả");
            return;
        }
        if (!preview || previewError) {
            toast.error("Vui lòng tính preview hợp lệ trước khi tạo phiếu trả");
            return;
        }

        setSubmitting(true);
        try {
            const idempotencyKey = returnIdempotencyKey || createReturnIdempotencyKey();
            setReturnIdempotencyKey(idempotencyKey);
            const data = await staffApi.createReturn(
                buildReturnPayload(cart, selectedReader, note),
                idempotencyKey
            );
            setResult(data);
            setShowResult(true);
            toast.success("Tạo phiếu trả thành công");
            await refreshOpenLoans(selectedReader.id);
            setCart([]);
            setPreview(null);
        } catch (error) {
            toast.error(error.message || "Tạo phiếu trả thất bại");
        } finally {
            setSubmitting(false);
        }
    }

    const cartIds = new Set(cart.map((item) => item.loanDetailId));
    const previewByLoanDetailId = new Map((preview?.chiTiet || []).map((item) => [item.maChiTietMuon, item]));

    return (
        <div className="staff-return-workspace">
            <PageHeader
                eyebrow="Staff"
                title="Trả sách"
                description="Quét barcode, mã cuốn hoặc mã chi tiết mượn để lập giỏ trả cho một độc giả."
                right={result ? (
                    <button className="soft-button" type="button" onClick={() => setShowResult(true)}>
                        Xem lại kết quả
                    </button>
                ) : null}
            />

            <section className="panel return-scan-panel">
                <form className="return-scan-form" onSubmit={handleScanSubmit}>
                    <span className="return-scan-icon"><ScanLine size={22} /></span>
                    <label>
                        <span>Quét barcode / mã cuốn / mã chi tiết mượn</span>
                        <input
                            value={scanCode}
                            onChange={(event) => setScanCode(event.target.value)}
                            placeholder="Nhập mã rồi Enter"
                            disabled={scanLoading}
                            autoFocus
                        />
                    </label>
                    <button className="primary-button" type="submit" disabled={scanLoading || !scanCode.trim()}>
                        {scanLoading ? "Đang trả..." : "Thêm vào giỏ"}
                    </button>
                </form>
                {scanError && <div className="loan-message is-error" role="alert">{scanError}</div>}
                {selectedReader ? (
                    <div className="return-reader-lock">
                        <strong>{selectedReader.name || selectedReader.id}</strong>
                        <span>{selectedReader.id}</span>
                        <button className="soft-button" type="button" onClick={resetTransaction} disabled={submitting}>
                            Giao dịch mới
                        </button>
                    </div>
                ) : (
                    <div className="return-empty-hint">
                        Cuốn đầu tiên sẽ tự suy ra độc giả. Không thể trộn nhiều độc giả trong một phiếu.
                    </div>
                )}
            </section>

            <div className="return-two-column">
                <section className="panel">
                    <div className="panel-title">
                        <h2>Sách đang mượn của độc giả</h2>
                        <span>{openLoans.length} cuốn</span>
                    </div>
                    {!selectedReader && <p className="loan-state-message">Quét một cuốn sách để tải danh sách đang mượn của độc giả.</p>}
                    {openLoansLoading && <p className="loan-state-message" role="status">Đang tải sách đang mượn...</p>}
                    {openLoansError && <div className="loan-message is-error" role="alert">{openLoansError}</div>}
                    {selectedReader && !openLoansLoading && !openLoansError && (
                        <DataTable
                            data={openLoans}
                            columns={[
                                {
                                    key: "select",
                                    title: "",
                                    render: (row) => (
                                        <input
                                            className="table-checkbox"
                                            type="checkbox"
                                            checked={cartIds.has(row.loanDetailId)}
                                            onChange={() => toggleLoan(row)}
                                            aria-label={`Chọn trả ${row.copyId}`}
                                        />
                                    )
                                },
                                { key: "loanDetailId", title: "CT mượn" },
                                {
                                    key: "copy",
                                    title: "Cuốn sách",
                                    render: (row) => <CopyCell row={row} />
                                },
                                { key: "borrowedAt", title: "Ngày mượn", render: (row) => formatDateTime(row.borrowedAt) },
                                { key: "dueAt", title: "Hạn trả", render: (row) => formatDateTime(row.dueAt) },
                                { key: "overdueDays", title: "Ngày trễ", render: (row) => <OverdueBadge days={row.overdueDays} /> },
                                { key: "status", title: "Trạng thái", render: (row) => <StatusBadge value={row.status} /> }
                            ]}
                        />
                    )}
                </section>

                <section className="panel return-cart-panel">
                    <div className="panel-title">
                        <h2>Giỏ trả</h2>
                        <span>{cart.length} cuốn</span>
                    </div>

                    {cart.length === 0 ? (
                        <div className="return-cart-empty">
                            <CheckSquare size={28} />
                            <p>Chưa có sách trong giỏ trả.</p>
                        </div>
                    ) : (
                        <div className="return-cart-list">
                            {cart.map((item) => (
                                <ReturnCartItem
                                    key={item.loanDetailId}
                                    item={item}
                                    previewItem={previewByLoanDetailId.get(item.loanDetailId)}
                                    onChange={(patch) => updateCartItem(item.loanDetailId, patch)}
                                    onRemove={() => removeFromCart(item.loanDetailId)}
                                />
                            ))}
                        </div>
                    )}

                    <form className="return-submit-box" onSubmit={handleSubmit}>
                        <label>
                            <span>Ghi chú phiếu trả</span>
                            <textarea
                                value={note}
                                onChange={(event) => {
                                    const nextNote = event.target.value;
                                    setNote(nextNote);
                                    refreshPreview(cart, selectedReader, nextNote);
                                }}
                                rows={3}
                            />
                        </label>
                        <PreviewSummary
                            preview={preview}
                            loading={previewLoading}
                            error={previewError}
                            onRefresh={() => refreshPreview()}
                        />
                        <button
                            className="primary-button"
                            type="submit"
                            disabled={submitting || previewLoading || cart.length === 0 || !preview || Boolean(previewError)}
                        >
                            {submitting ? "Đang tạo phiếu..." : "Tạo phiếu trả"}
                        </button>
                    </form>
                </section>
            </div>

            {result && showResult && (
                <ReturnResultPanel
                    result={result}
                    onClose={() => setShowResult(false)}
                    onPrint={() => window.print()}
                    onCollectPayment={() => navigate("/staff/payments", {
                        state: {
                            readerId: result.paymentSuggestion?.readerId || result.maDocGia,
                            debtIds: result.paymentSuggestion?.debtIds || []
                        }
                    })}
                />
            )}
        </div>
    );
}

function ReturnCartItem({ item, previewItem, onChange, onRemove }) {
    const isDamaged = item.returnCondition === RETURN_DAMAGED;
    const isLost = item.returnCondition === RETURN_LOST;

    return (
        <article className="return-cart-item">
            <div className="return-cart-main">
                <CopyCell row={item} />
                <button className="icon-button" type="button" onClick={onRemove} aria-label={`Xóa ${item.copyId} khỏi giỏ trả`}>
                    <Trash2 size={17} />
                </button>
            </div>
            <div className="return-cart-meta">
                <span><small>Ngày mượn</small><b>{formatDateTime(item.borrowedAt)}</b></span>
                <span><small>Hạn trả</small><b>{formatDateTime(item.dueAt)}</b></span>
                <span><small>Ngày trễ</small><b>{previewItem?.soNgayTre ?? item.overdueDays ?? 0}</b></span>
            </div>
            <div className="return-cart-controls">
                <label>
                    <span>Tình trạng</span>
                    <select value={item.returnCondition} onChange={(event) => onChange({ returnCondition: event.target.value })}>
                        <option value={RETURN_NORMAL}>Bình thường</option>
                        <option value={RETURN_DAMAGED}>Hỏng</option>
                        <option value={RETURN_LOST}>Mất</option>
                    </select>
                </label>
                {isDamaged && (
                    <label>
                        <span>Mức độ</span>
                        <select value={item.damageSeverity} onChange={(event) => onChange({ damageSeverity: event.target.value })}>
                            <option value="">Chọn mức độ</option>
                            {DAMAGE_SEVERITIES.map((severity) => (
                                <option key={severity.value} value={severity.value}>{severity.label}</option>
                            ))}
                        </select>
                    </label>
                )}
            </div>
            {isDamaged && (
                <div className="return-damage-types">
                    {DAMAGE_TYPES.map((type) => (
                        <label key={type.value}>
                            <input
                                type="checkbox"
                                checked={item.damageTypes.includes(type.value)}
                                onChange={(event) => {
                                    const nextTypes = event.target.checked
                                        ? [...item.damageTypes, type.value]
                                        : item.damageTypes.filter((value) => value !== type.value);
                                    onChange({ damageTypes: nextTypes });
                                }}
                            />
                            <span>{type.label}</span>
                        </label>
                    ))}
                </div>
            )}
            {(isDamaged || isLost) && (
                <label className="return-note-field">
                    <span>Mô tả tình trạng</span>
                    <textarea
                        value={item.damageDescription}
                        onChange={(event) => onChange({ damageDescription: event.target.value })}
                        rows={2}
                    />
                </label>
            )}
            <div className="return-preview-line">
                <span>Phạt trễ: <strong>{formatMoney(previewItem?.tienPhatTre || 0)}</strong></span>
                <span>Phạt hỏng/mất: <strong>{formatMoney(previewItem?.tienPhatHongMat || 0)}</strong></span>
                <span>Tổng: <strong>{formatMoney(previewItem?.tongPhat || 0)}</strong></span>
            </div>
            {(isDamaged || isLost) && (
                <div className="return-adjustment-grid">
                    <label>
                        <span>Đề nghị điều chỉnh</span>
                        <input
                            type="number"
                            min="0"
                            value={item.adjustmentFine}
                            onChange={(event) => onChange({ adjustmentFine: event.target.value })}
                            placeholder="Bỏ trống nếu không điều chỉnh"
                        />
                    </label>
                    <label>
                        <span>Lý do điều chỉnh</span>
                        <input
                            value={item.adjustmentReason}
                            onChange={(event) => onChange({ adjustmentReason: event.target.value })}
                            placeholder="Bắt buộc nếu có điều chỉnh"
                        />
                    </label>
                </div>
            )}
            <label className="return-note-field">
                <span>Ghi chú dòng trả</span>
                <input value={item.returnNote} onChange={(event) => onChange({ returnNote: event.target.value })} />
            </label>
        </article>
    );
}

function PreviewSummary({ preview, loading, error, onRefresh }) {
    return (
        <div className="return-preview-summary">
            <div className="return-preview-header">
                <strong>Preview tổng phạt</strong>
                <button className="icon-button" type="button" onClick={onRefresh} disabled={loading} aria-label="Tính lại preview">
                    <RefreshCw size={16} />
                </button>
            </div>
            {loading && <p className="loan-state-message" role="status">Đang tính tiền phạt...</p>}
            {error && <div className="loan-message is-error" role="alert">{error}</div>}
            {!loading && !error && !preview && <p className="loan-state-message">Thêm sách vào giỏ để xem preview.</p>}
            {preview && !error && (
                <div className="return-total-row">
                    <span>Phạt trễ: <strong>{formatMoney(preview.tongPhatTre)}</strong></span>
                    <span>Phạt hỏng/mất: <strong>{formatMoney(preview.tongPhatHongMat)}</strong></span>
                    <span>Tổng: <strong>{formatMoney(preview.tongPhat)}</strong></span>
                </div>
            )}
        </div>
    );
}

function CopyCell({ row }) {
    return (
        <div className="return-copy-cell">
            <strong>{row.titleName || row.titleId || "Không rõ đầu sách"}</strong>
            <span>{row.copyId}{row.barcode ? ` - ${row.barcode}` : ""}</span>
            <small>{row.loanDetailId}</small>
        </div>
    );
}

function OverdueBadge({ days }) {
    const value = Number(days || 0);
    return value > 0 ? <span className="loan-overdue-badge">Trễ {value} ngày</span> : <span>0</span>;
}

function ReturnResultPanel({ result, onClose, onPrint, onCollectPayment }) {
    const hasDebt = result.paymentSuggestion?.hasDebt;

    return (
        <ResultModal title="Kết quả phiếu trả" onClose={onClose}>
            <h2>Kết quả phiếu trả</h2>
            <div className="result-stack">
                <div className="result-grid">
                    <ResultItem label="Mã phiếu" value={result.maPhieuTra} />
                    <ResultItem label="Độc giả" value={result.maDocGia} />
                    <ResultItem label="Nhân viên nhận" value={result.maNhanVienNhan} />
                    <ResultItem label="Chi nhánh" value={result.maChiNhanh} />
                    <ResultItem label="Ngày trả" value={formatDateTime(result.ngayTra)} />
                    <ResultItem label="Tổng phạt" value={formatMoney(getReturnFineTotal(result))} />
                </div>

                <DataTable
                    data={result.chiTiet || []}
                    columns={[
                        { key: "maChiTietTra", title: "CT trả" },
                        { key: "maChiTietMuon", title: "CT mượn" },
                        { key: "tinhTrangKhiTra", title: "Tình trạng" },
                        { key: "soNgayTre", title: "Ngày trễ" },
                        { key: "tienPhatTre", title: "Phạt trễ", render: (row) => formatMoney(row.tienPhatTre) },
                        { key: "tienPhatHongMat", title: "Phạt hỏng/mất", render: (row) => formatMoney(row.tienPhatHongMat) }
                    ]}
                />
                <div className="return-result-actions">
                    <button className="soft-button" type="button" onClick={onPrint}>
                        In phiếu
                    </button>
                    {hasDebt && (
                        <button className="primary-button" type="button" onClick={onCollectPayment}>
                            Chuyển sang thu tiền
                        </button>
                    )}
                </div>
            </div>
        </ResultModal>
    );
}

function buildReturnPayload(rows, reader, note) {
    return {
        maDocGia: reader.id,
        maChiNhanh: rows[0]?.branchId,
        ghiChu: note.trim() || null,
        chiTiet: rows.map((item) => ({
            maChiTietMuon: item.loanDetailId,
            tinhTrangKhiTra: item.returnCondition,
            loaiHuHong: item.returnCondition === RETURN_DAMAGED ? item.damageTypes : [],
            mucDoHuHong: item.returnCondition === RETURN_DAMAGED ? item.damageSeverity || null : null,
            moTaHuHong: item.damageDescription?.trim() || null,
            tienPhatHongMatDieuChinh: item.adjustmentFine === "" ? null : Number(item.adjustmentFine),
            lyDoDieuChinhTienPhat: item.adjustmentReason?.trim() || null,
            ghiChu: item.returnNote || note || null
        }))
    };
}

function withDefaultReturnState(item) {
    return {
        ...item,
        returnCondition: RETURN_NORMAL,
        damageTypes: [],
        damageSeverity: "",
        damageDescription: "",
        adjustmentFine: "",
        adjustmentReason: "",
        returnNote: ""
    };
}

function getReturnFineTotal(result) {
    return (result?.chiTiet || []).reduce(
        (sum, row) => sum + Number(row.tienPhatTre || 0) + Number(row.tienPhatHongMat || 0),
        0
    );
}

function createReturnIdempotencyKey() {
    if (typeof crypto !== "undefined" && typeof crypto.randomUUID === "function") {
        return crypto.randomUUID();
    }
    return `return-${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

function ResultItem({ label, value }) {
    return (
        <div className="result-item">
            <span>{label}</span>
            <strong>{value || "-"}</strong>
        </div>
    );
}
