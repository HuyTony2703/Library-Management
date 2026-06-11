import { useEffect, useMemo, useState } from "react";
import { RefreshCcw, Search } from "lucide-react";
import { libraryApi } from "../../api/libraryApi";
import { staffApi } from "../../api/staffApi";
import PageHeader from "../../components/PageHeader";
import DataTable from "../../components/DataTable";
import ResultModal from "../../components/ResultModal";
import StatusBadge from "../../components/StatusBadge";
import { useToast } from "../../components/ToastProvider";
import { displayCode, formatDateTime, formatMoney } from "../../utils/displayUtils";

export default function StaffPaymentsPage() {
    const toast = useToast();

    const [maDocGia, setMaDocGia] = useState("DG001");
    const [debts, setDebts] = useState([]);
    const [debtors, setDebtors] = useState([]);
    const [selected, setSelected] = useState({});
    const [result, setResult] = useState(null);
    const [showResult, setShowResult] = useState(false);
    const [loading, setLoading] = useState(false);

    const [form, setForm] = useState({
        maPhieuThu: `PT_STAFF_${Date.now().toString().slice(-6)}`,
        maNhanVienThu: "NV_TT001",
        maPhuongThuc: "PT_TIEN_MAT",
        soTienThu: 0,
        ghiChu: "Thu tiền phạt từ giao diện thủ thư"
    });

    function updateField(field, value) {
        setForm((prev) => ({ ...prev, [field]: value }));
    }

    function regenerateCode() {
        updateField("maPhieuThu", `PT_STAFF_${Date.now().toString().slice(-6)}`);
    }

    function getDebtRemaining(row) {
        return Number(row.soTienConLai ?? row.SoTienConLai ?? row.conLai ?? 0);
    }

    async function loadDebts() {
        try {
            const data = await staffApi.getReaderDebts(maDocGia);
            setDebts(Array.isArray(data) ? data : []);
            setSelected({});
            toast.success("Đã tải khoản nợ");
        } catch (err) {
            toast.error(err.message || "Không tải được khoản nợ");
        }
    }

    async function loadDebtors() {
        try {
            const data = await libraryApi.debtReport();
            setDebtors(Array.isArray(data) ? data.filter((item) => Number(item.tongNoConLai || 0) > 0) : []);
        } catch (err) {
            toast.error(err.message || "Không tải được danh sách độc giả còn nợ");
        }
    }

    useEffect(() => {
        loadDebtors();
    }, []);

    function toggleDebt(row) {
        const remaining = getDebtRemaining(row);

        setSelected((prev) => {
            const copy = { ...prev };

            if (copy[row.maKhoanNo] !== undefined) {
                delete copy[row.maKhoanNo];
            } else {
                copy[row.maKhoanNo] = remaining;
            }

            return copy;
        });
    }

    function updateDebtAmount(maKhoanNo, value) {
        setSelected((prev) => ({ ...prev, [maKhoanNo]: Number(value) }));
    }

    const selectedTotal = useMemo(() => {
        return Object.values(selected).reduce((sum, value) => sum + Number(value || 0), 0);
    }, [selected]);

    const selectedDebtCount = Object.keys(selected).length;

    function syncSelectedTotalToForm() {
        updateField("soTienThu", selectedTotal);
    }

    function selectAllDebts() {
        const nextSelected = {};
        let total = 0;

        debts.forEach((row) => {
            const remaining = getDebtRemaining(row);

            if (remaining > 0) {
                nextSelected[row.maKhoanNo] = remaining;
                total += remaining;
            }
        });

        setSelected(nextSelected);
        updateField("soTienThu", total);
    }

    function clearSelectedDebts() {
        setSelected({});
        updateField("soTienThu", 0);
    }

    function buildSelectedDetails() {
        return Object.entries(selected).map(([maKhoanNo, soTienApDung]) => ({
            maKhoanNo,
            soTienApDung: Number(soTienApDung)
        }));
    }

    async function createManualPayment(e) {
        e.preventDefault();

        const chiTietNo = buildSelectedDetails();

        if (chiTietNo.length === 0) {
            toast.error("Vui lòng chọn ít nhất một khoản nợ");
            return;
        }

        if (Number(form.soTienThu) <= 0) {
            toast.error("Số tiền thu phải lớn hơn 0");
            return;
        }

        if (chiTietNo.some((item) => Number(item.soTienApDung) <= 0)) {
            toast.error("Tiền áp dụng cho từng khoản nợ phải lớn hơn 0");
            return;
        }

        if (Number(form.soTienThu) !== selectedTotal) {
            toast.error("Số tiền thu phải bằng tổng tiền áp dụng vào các khoản nợ");
            return;
        }

        setLoading(true);

        try {
            const data = await staffApi.createPayment({
                maPhieuThu: form.maPhieuThu,
                maDocGia,
                maNhanVienThu: form.maNhanVienThu,
                maPhuongThuc: form.maPhuongThuc,
                soTienThu: Number(form.soTienThu),
                ghiChu: form.ghiChu,
                chiTietNo
            });

            setResult(data);
            setShowResult(true);
            toast.success("Thu tiền thành công");
            await loadDebtors();
            await loadDebts();
        } catch (err) {
            toast.error(err.message || "Thu tiền thất bại");
        } finally {
            setLoading(false);
        }
    }

    async function createAutoPayment() {
        if (Number(form.soTienThu) <= 0) {
            toast.error("Số tiền thu phải lớn hơn 0");
            return;
        }

        setLoading(true);

        try {
            const data = await staffApi.createPayment({
                maPhieuThu: form.maPhieuThu,
                maDocGia,
                maNhanVienThu: form.maNhanVienThu,
                maPhuongThuc: form.maPhuongThuc,
                soTienThu: Number(form.soTienThu),
                ghiChu: form.ghiChu,
                chiTietNo: []
            });

            setResult(data);
            setShowResult(true);
            toast.success("Tự động phân bổ thu tiền thành công");
            await loadDebtors();
            await loadDebts();
        } catch (err) {
            toast.error(err.message || "Tự động phân bổ thất bại");
        } finally {
            setLoading(false);
        }
    }

    return (
        <div>
            <PageHeader
                eyebrow="Staff"
                title="Thu tiền phạt"
                description="Xem khoản nợ của độc giả, chọn khoản nợ cụ thể để thu hoặc để hệ thống tự động phân bổ."
                right={
                    result ? (
                        <button className="soft-button" type="button" onClick={() => setShowResult(true)}>
                            Xem lại kết quả
                        </button>
                    ) : null
                }
            />

            <div className="panel">
                <div className="panel-title">
                    <h2>Danh sách khoản nợ</h2>
                    <span>{debts.length} khoản</span>
                </div>

                <div className="payment-debt-search">
                    <label>Mã độc giả</label>
                    <input value={maDocGia} onChange={(e) => setMaDocGia(e.target.value)} />
                    <button className="soft-button" type="button" onClick={loadDebts}>
                        <Search size={17} />
                        Xem nợ
                    </button>
                </div>

                <div className="selection-toolbar">
                    <button type="button" className="soft-button" onClick={selectAllDebts}>
                        Chọn tất cả
                    </button>
                    <button type="button" className="primary-button compact-primary-button" onClick={clearSelectedDebts}>
                        Bỏ chọn tất cả
                    </button>
                </div>

                <DataTable
                    data={debts}
                    columns={[
                        {
                            key: "chon",
                            title: `${selectedDebtCount} mục`,
                            className: "selection-count-cell",
                            render: (row) => (
                                <input
                                    className="table-checkbox"
                                    type="checkbox"
                                    checked={selected[row.maKhoanNo] !== undefined}
                                    onChange={() => toggleDebt(row)}
                                />
                            )
                        },
                        { key: "maKhoanNo", title: "Mã nợ" },
                        { key: "maLoaiKhoanNo", title: "Loại" },
                        { key: "lyDo", title: "Lý do" },
                        { key: "soTienPhatSinh", title: "Phát sinh", render: (row) => formatMoney(row.soTienPhatSinh) },
                        { key: "soTienDaThanhToan", title: "Đã trả", render: (row) => formatMoney(row.soTienDaThanhToan) },
                        { key: "soTienConLai", title: "Còn lại", render: (row) => formatMoney(row.soTienConLai) },
                        {
                            key: "soTienApDung",
                            title: "Tiền áp dụng",
                            render: (row) =>
                                selected[row.maKhoanNo] !== undefined ? (
                                    <input
                                        type="number"
                                        value={selected[row.maKhoanNo]}
                                        onChange={(e) => updateDebtAmount(row.maKhoanNo, e.target.value)}
                                    />
                                ) : (
                                    "-"
                                )
                        },
                        { key: "trangThai", title: "Trạng thái", render: (row) => <StatusBadge value={row.trangThai} /> }
                    ]}
                />
            </div>

            <div className="form-layout">
                <form className="panel form-panel" onSubmit={createManualPayment}>
                    <div className="form-row">
                        <label>Mã phiếu thu</label>
                        <div className="inline-control">
                            <input value={form.maPhieuThu} onChange={(e) => updateField("maPhieuThu", e.target.value)} />
                            <button type="button" className="icon-button" onClick={regenerateCode}>
                                <RefreshCcw size={17} />
                            </button>
                        </div>
                    </div>

                    <div className="form-grid-2">
                        <div className="form-row">
                            <label>Mã nhân viên thu</label>
                            <input value={form.maNhanVienThu} onChange={(e) => updateField("maNhanVienThu", e.target.value)} />
                        </div>

                        <div className="form-row">
                            <label>Phương thức</label>
                            <input value={form.maPhuongThuc} onChange={(e) => updateField("maPhuongThuc", e.target.value)} />
                        </div>
                    </div>

                    <div className="form-row">
                        <label>Số tiền thu</label>
                        <div className="inline-control">
                            <input type="number" value={form.soTienThu} onChange={(e) => updateField("soTienThu", e.target.value)} />
                            <button type="button" className="soft-button" onClick={syncSelectedTotalToForm}>
                                Lấy tổng đã chọn
                            </button>
                        </div>
                        <small>Tổng tiền đang chọn: {formatMoney(selectedTotal)}</small>
                    </div>

                    <div className="form-row">
                        <label>Ghi chú</label>
                        <textarea value={form.ghiChu} onChange={(e) => updateField("ghiChu", e.target.value)} />
                    </div>

                    <button className="primary-button" disabled={loading}>
                        Thu khoản đã chọn
                    </button>

                    <button type="button" className="soft-button" disabled={loading} onClick={createAutoPayment}>
                        Tự động phân bổ
                    </button>
                </form>

                <DebtorOverviewPanel debtors={debtors} />
            </div>

            {result && showResult && (
                <PaymentResultPanel
                    result={result}
                    onClose={() => setShowResult(false)}
                />
            )}
        </div>
    );
}

function DebtorOverviewPanel({ debtors }) {
    return (
        <div className="panel preview-panel">
            <div className="panel-title">
                <h2>Độc giả còn nợ</h2>
                <span>{debtors.length} độc giả</span>
            </div>

            <DataTable
                data={debtors}
                columns={[
                    { key: "maDocGia", title: "Mã độc giả" },
                    { key: "hoTen", title: "Họ tên" },
                    { key: "tongNoConLai", title: "Tổng nợ", render: (row) => formatMoney(row.tongNoConLai) }
                ]}
            />
        </div>
    );
}

function PaymentResultPanel({ result, onClose }) {
    return (
        <ResultModal title="Kết quả phiếu thu" onClose={onClose}>
            <h2>Kết quả phiếu thu</h2>

            {!result ? (
                <p className="muted-text">Chưa có dữ liệu</p>
            ) : (
                <div className="result-stack">
                    <div className="result-grid">
                        <ResultItem label="Mã phiếu" value={result.maPhieuThu} />
                        <ResultItem label="Độc giả" value={result.maDocGia} />
                        <ResultItem label="Nhân viên thu" value={result.maNhanVienThu} />
                        <ResultItem label="Phương thức" value={displayCode(result.maPhuongThuc)} />
                        <ResultItem label="Loại thu" value={result.loaiThu} />
                        <ResultItem label="Số tiền thu" value={formatMoney(result.soTienThu)} />
                        <ResultItem label="Ngày thu" value={formatDateTime(result.ngayThu)} />
                        <ResultItem label="Trạng thái" value={<StatusBadge value={result.trangThai} />} />
                    </div>

                    <DataTable
                        data={result.chiTietNo || []}
                        columns={[
                            { key: "maChiTietPhieuThu", title: "Mã chi tiết" },
                            { key: "maKhoanNo", title: "Khoản nợ" },
                            { key: "soTienApDung", title: "Tiền áp dụng", render: (row) => formatMoney(row.soTienApDung) }
                        ]}
                    />
                </div>
            )}
        </ResultModal>
    );
}

function ResultItem({ label, value }) {
    return (
        <div className="result-item">
            <span>{label}</span>
            <strong>{value || "-"}</strong>
        </div>
    );
}
