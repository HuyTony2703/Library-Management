import { useEffect, useState } from "react";
import { RefreshCcw, Search } from "lucide-react";
import { libraryApi } from "../../api/libraryApi";
import { staffApi } from "../../api/staffApi";
import PageHeader from "../../components/PageHeader";
import DataTable from "../../components/DataTable";
import StatusBadge from "../../components/StatusBadge";
import { useToast } from "../../components/ToastProvider";
import { displayCode, formatDateTime } from "../../utils/displayUtils";

export default function StaffLoansPage() {
    const toast = useToast();

    const [form, setForm] = useState({
        maPhieuMuon: `PM_STAFF_${Date.now().toString().slice(-6)}`,
        maDocGia: "DG001",
        maNhanVienLap: "NV_TT001",
        maChiNhanh: "CN_TD",
        maCuonSachText: "F01-001",
        ghiChu: "Mượn sách từ giao diện thủ thư"
    });

    const [currentLoans, setCurrentLoans] = useState([]);
    const [availableCopies, setAvailableCopies] = useState([]);
    const [result, setResult] = useState(null);
    const [loading, setLoading] = useState(false);

    function updateField(field, value) {
        setForm((prev) => ({ ...prev, [field]: value }));
    }

    function regenerateCode() {
        updateField("maPhieuMuon", `PM_STAFF_${Date.now().toString().slice(-6)}`);
    }

    function getBookCopyIds() {
        return form.maCuonSachText
            .split(",")
            .map((item) => item.trim())
            .filter(Boolean);
    }

    async function loadAvailableCopies() {
        try {
            const data = await libraryApi.bookCopies();
            setAvailableCopies((Array.isArray(data) ? data : []).filter((item) => item.maTrangThai === "TT_SANCO"));
        } catch (err) {
            toast.error(err.message || "Không tải được danh sách sách sẵn có");
        }
    }

    useEffect(() => {
        loadAvailableCopies();
    }, []);

    async function loadCurrentLoans() {
        try {
            const data = await staffApi.getReaderCurrentLoans(form.maDocGia);
            setCurrentLoans(Array.isArray(data) ? data : []);
            toast.success("Đã tải sách đang mượn");
        } catch (err) {
            toast.error(err.message || "Không tải được sách đang mượn");
        }
    }

    async function handleSubmit(e) {
        e.preventDefault();

        const maCuonSachs = getBookCopyIds();

        if (maCuonSachs.length === 0) {
            toast.error("Vui lòng nhập ít nhất một mã cuốn sách");
            return;
        }

        setLoading(true);

        try {
            const data = await staffApi.createLoan({
                maPhieuMuon: form.maPhieuMuon,
                maDocGia: form.maDocGia,
                maNhanVienLap: form.maNhanVienLap,
                maChiNhanh: form.maChiNhanh,
                maCuonSachs,
                ghiChu: form.ghiChu
            });

            setResult(data);
            toast.success("Tạo phiếu mượn thành công");
            await loadAvailableCopies();
            await loadCurrentLoans();
        } catch (err) {
            toast.error(err.message || "Tạo phiếu mượn thất bại");
        } finally {
            setLoading(false);
        }
    }

    return (
        <div>
            <PageHeader
                eyebrow="Staff"
                title="Mượn sách"
                description="Lập phiếu mượn cho độc giả, tự kiểm tra thẻ, nợ, quy định mượn và trạng thái cuốn sách."
                right={
                    <button className="soft-button" onClick={loadCurrentLoans}>
                        <Search size={17} />
                        Xem sách đang mượn
                    </button>
                }
            />

            <div className="form-layout">
                <form className="panel form-panel" onSubmit={handleSubmit}>
                    <div className="form-row">
                        <label>Mã phiếu mượn</label>
                        <div className="inline-control">
                            <input value={form.maPhieuMuon} onChange={(e) => updateField("maPhieuMuon", e.target.value)} />
                            <button type="button" className="icon-button" onClick={regenerateCode}>
                                <RefreshCcw size={17} />
                            </button>
                        </div>
                    </div>

                    <div className="form-grid-2">
                        <div className="form-row">
                            <label>Mã độc giả</label>
                            <input value={form.maDocGia} onChange={(e) => updateField("maDocGia", e.target.value)} />
                        </div>

                        <div className="form-row">
                            <label>Mã nhân viên lập</label>
                            <input value={form.maNhanVienLap} onChange={(e) => updateField("maNhanVienLap", e.target.value)} />
                        </div>
                    </div>

                    <div className="form-grid-2">
                        <div className="form-row">
                            <label>Mã chi nhánh</label>
                            <input value={form.maChiNhanh} onChange={(e) => updateField("maChiNhanh", e.target.value)} />
                        </div>

                        <div className="form-row">
                            <label>Mã cuốn sách</label>
                            <input
                                value={form.maCuonSachText}
                                onChange={(e) => updateField("maCuonSachText", e.target.value)}
                                placeholder="Ví dụ: F01-001 hoặc F01-001, F01-002"
                            />
                        </div>
                    </div>

                    <div className="form-row">
                        <label>Ghi chú</label>
                        <textarea value={form.ghiChu} onChange={(e) => updateField("ghiChu", e.target.value)} />
                    </div>

                    <button className="primary-button" disabled={loading}>
                        {loading ? "Đang tạo phiếu..." : "Tạo phiếu mượn"}
                    </button>
                </form>

                {result ? <LoanResultPanel result={result} /> : <AvailableCopiesPanel copies={availableCopies} />}
            </div>

            <div className="panel">
                <div className="panel-title">
                    <h2>Sách đang mượn của {form.maDocGia}</h2>
                    <span>{currentLoans.length} cuốn</span>
                </div>

                <DataTable
                    data={currentLoans}
                    columns={[
                        { key: "maChiTietMuon", title: "Mã CT mượn" },
                        { key: "maCuonSach", title: "Mã cuốn" },
                        { key: "maQuyDinhMuon", title: "Quy định", render: (row) => displayCode(row.maQuyDinhMuon) },
                        { key: "ngayMuon", title: "Ngày mượn", render: (row) => formatDateTime(row.ngayMuon) },
                        { key: "hanTra", title: "Hạn trả", render: (row) => formatDateTime(row.hanTra) },
                        { key: "trangThai", title: "Trạng thái", render: (row) => <StatusBadge value={row.trangThai} /> }
                    ]}
                />
            </div>
        </div>
    );
}

function AvailableCopiesPanel({ copies }) {
    return (
        <div className="panel preview-panel">
            <div className="panel-title">
                <h2>Cuốn sách đang sẵn có</h2>
                <span>{copies.length} cuốn</span>
            </div>

            <DataTable
                data={copies.slice(0, 8)}
                columns={[
                    { key: "maCuonSach", title: "Mã cuốn" },
                    { key: "maDauSach", title: "Đầu sách" },
                    { key: "maChiNhanh", title: "Chi nhánh" },
                    { key: "maTrangThai", title: "Trạng thái", render: (row) => <StatusBadge value={row.maTrangThai} /> }
                ]}
            />
        </div>
    );
}

function LoanResultPanel({ result }) {
    return (
        <div className="panel preview-panel">
            <h2>Kết quả phiếu mượn</h2>

            {!result ? (
                <p className="muted-text">Chưa có dữ liệu</p>
            ) : (
                <div className="result-stack">
                    <div className="result-grid">
                        <ResultItem label="Mã phiếu" value={result.maPhieuMuon} />
                        <ResultItem label="Độc giả" value={result.maDocGia} />
                        <ResultItem label="Nhân viên lập" value={result.maNhanVienLap} />
                        <ResultItem label="Chi nhánh" value={result.maChiNhanh} />
                        <ResultItem label="Ngày mượn" value={formatDateTime(result.ngayMuon)} />
                        <ResultItem label="Trạng thái" value={<StatusBadge value={result.trangThai} />} />
                    </div>

                    <DataTable
                        data={result.chiTiet || []}
                        columns={[
                            { key: "maChiTietMuon", title: "Mã chi tiết" },
                            { key: "maCuonSach", title: "Mã cuốn" },
                            { key: "maQuyDinhMuon", title: "Quy định", render: (row) => displayCode(row.maQuyDinhMuon) },
                            { key: "hanTra", title: "Hạn trả", render: (row) => formatDateTime(row.hanTra) },
                            { key: "trangThai", title: "Trạng thái", render: (row) => <StatusBadge value={row.trangThai} /> }
                        ]}
                    />
                </div>
            )}
        </div>
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
