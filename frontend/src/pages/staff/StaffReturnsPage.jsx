import { useEffect, useState } from "react";
import { RefreshCcw, Search } from "lucide-react";
import { libraryApi } from "../../api/libraryApi";
import { staffApi } from "../../api/staffApi";
import PageHeader from "../../components/PageHeader";
import DataTable from "../../components/DataTable";
import ResultModal from "../../components/ResultModal";
import StatusBadge from "../../components/StatusBadge";
import { useToast } from "../../components/ToastProvider";
import { displayCode, formatDateTime, formatMoney } from "../../utils/displayUtils";

export default function StaffReturnsPage() {
    const toast = useToast();

    const [form, setForm] = useState({
        maPhieuTra: `PTR_STAFF_${Date.now().toString().slice(-6)}`,
        maDocGia: "DG001",
        maNhanVienNhan: "NV_TT001",
        maChiNhanh: "CN_TD",
        maChiTietMuon: "",
        tinhTrangKhiTra: "Bình thường",
        tienPhatHongMat: 0,
        ghiChu: "Trả sách từ giao diện thủ thư"
    });

    const [currentLoans, setCurrentLoans] = useState([]);
    const [loanOverview, setLoanOverview] = useState([]);
    const [result, setResult] = useState(null);
    const [showResult, setShowResult] = useState(false);
    const [loading, setLoading] = useState(false);

    function updateField(field, value) {
        setForm((prev) => ({ ...prev, [field]: value }));
    }

    function regenerateCode() {
        updateField("maPhieuTra", `PTR_STAFF_${Date.now().toString().slice(-6)}`);
    }

    async function loadCurrentLoans() {
        try {
            const data = await staffApi.getReaderCurrentLoans(form.maDocGia);
            setCurrentLoans(Array.isArray(data) ? data : []);
            toast.success("Đã tải sách đang mượn");
        } catch (err) {
            toast.error(err.message || "Không tải được sách đang mượn");
        }
    }

    function selectLoan(row) {
        updateField("maChiTietMuon", row.maChiTietMuon);
    }

    async function loadLoanOverview() {
        try {
            const data = await libraryApi.currentLoansReport();
            setLoanOverview(Array.isArray(data) ? data : []);
        } catch (err) {
            toast.error(err.message || "Không tải được tổng quan sách đang mượn");
        }
    }

    useEffect(() => {
        loadLoanOverview();
    }, []);

    async function handleSubmit(e) {
        e.preventDefault();

        if (!form.maChiTietMuon) {
            toast.error("Vui lòng chọn hoặc nhập mã chi tiết mượn");
            return;
        }

        if ((form.tinhTrangKhiTra === "Hỏng" || form.tinhTrangKhiTra === "Mất") && Number(form.tienPhatHongMat) <= 0) {
            toast.error("Sách hỏng/mất phải nhập tiền phạt lớn hơn 0");
            return;
        }

        if (form.tinhTrangKhiTra === "Bình thường" && Number(form.tienPhatHongMat) > 0) {
            toast.error("Sách bình thường không được nhập tiền phạt hỏng/mất");
            return;
        }

        setLoading(true);

        try {
            const data = await staffApi.createReturn({
                maPhieuTra: form.maPhieuTra,
                maDocGia: form.maDocGia,
                maNhanVienNhan: form.maNhanVienNhan,
                maChiNhanh: form.maChiNhanh,
                ghiChu: form.ghiChu,
                chiTiet: [
                    {
                        maChiTietMuon: form.maChiTietMuon,
                        tinhTrangKhiTra: form.tinhTrangKhiTra,
                        tienPhatHongMat: Number(form.tienPhatHongMat),
                        ghiChu: form.ghiChu
                    }
                ]
            });

            setResult(data);
            setShowResult(true);
            toast.success("Tạo phiếu trả thành công");
            await loadLoanOverview();
            await loadCurrentLoans();
        } catch (err) {
            toast.error(err.message || "Tạo phiếu trả thất bại");
        } finally {
            setLoading(false);
        }
    }

    return (
        <div>
            <PageHeader
                eyebrow="Staff"
                title="Trả sách"
                description="Lập phiếu trả, tự tính trả trễ và tạo khoản phạt khi sách hỏng hoặc mất."
                right={
                    <div className="table-actions">
                        {result && (
                            <button className="soft-button" type="button" onClick={() => setShowResult(true)}>
                                Xem lại kết quả
                            </button>
                        )}
                        <button className="soft-button" type="button" onClick={loadCurrentLoans}>
                            <Search size={17} />
                            Xem sách đang mượn
                        </button>
                    </div>
                }
            />

            <div className="form-layout">
                <form className="panel form-panel" onSubmit={handleSubmit}>
                    <div className="form-row">
                        <label>Mã phiếu trả</label>
                        <div className="inline-control">
                            <input value={form.maPhieuTra} onChange={(e) => updateField("maPhieuTra", e.target.value)} />
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
                            <label>Mã nhân viên nhận</label>
                            <input value={form.maNhanVienNhan} onChange={(e) => updateField("maNhanVienNhan", e.target.value)} />
                        </div>
                    </div>

                    <div className="form-grid-2">
                        <div className="form-row">
                            <label>Mã chi nhánh</label>
                            <input value={form.maChiNhanh} onChange={(e) => updateField("maChiNhanh", e.target.value)} />
                        </div>

                        <div className="form-row">
                            <label>Mã chi tiết mượn</label>
                            <input
                                value={form.maChiTietMuon}
                                onChange={(e) => updateField("maChiTietMuon", e.target.value)}
                                placeholder="Ví dụ: CTM_PM_STAFF_001_01"
                            />
                        </div>
                    </div>

                    <div className="form-grid-2">
                        <div className="form-row">
                            <label>Tình trạng khi trả</label>
                            <select value={form.tinhTrangKhiTra} onChange={(e) => updateField("tinhTrangKhiTra", e.target.value)}>
                                <option value="Bình thường">Bình thường</option>
                                <option value="Hỏng">Hỏng</option>
                                <option value="Mất">Mất</option>
                            </select>
                        </div>

                        <div className="form-row">
                            <label>Tiền phạt hỏng/mất</label>
                            <input type="number" value={form.tienPhatHongMat} onChange={(e) => updateField("tienPhatHongMat", e.target.value)} />
                        </div>
                    </div>

                    <div className="form-row">
                        <label>Ghi chú</label>
                        <textarea value={form.ghiChu} onChange={(e) => updateField("ghiChu", e.target.value)} />
                    </div>

                    <button className="primary-button" disabled={loading}>
                        {loading ? "Đang tạo phiếu..." : "Tạo phiếu trả"}
                    </button>
                </form>

                <LoanOverviewPanel rows={loanOverview} />
            </div>

            {result && showResult && (
                <ReturnResultPanel
                    result={result}
                    onClose={() => setShowResult(false)}
                />
            )}

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
                        { key: "hanTra", title: "Hạn trả", render: (row) => formatDateTime(row.hanTra) },
                        { key: "trangThai", title: "Trạng thái", render: (row) => <StatusBadge value={row.trangThai} /> },
                        {
                            key: "action",
                            title: "Chọn",
                            render: (row) => (
                                <button type="button" className="soft-button" onClick={() => selectLoan(row)}>
                                    Chọn
                                </button>
                            )
                        }
                    ]}
                />
            </div>
        </div>
    );
}

function LoanOverviewPanel({ rows }) {
    return (
        <div className="panel preview-panel">
            <div className="panel-title">
                <h2>Độc giả đang mượn sách</h2>
                <span>{rows.length} dòng</span>
            </div>

            <DataTable
                data={rows}
                columns={[
                    { key: "maDocGia", title: "Mã độc giả" },
                    { key: "hoTen", title: "Họ tên" },
                    { key: "soSachDangMuon", title: "Số sách đang mượn" }
                ]}
            />
        </div>
    );
}

function ReturnResultPanel({ result, onClose }) {
    return (
        <ResultModal title="Kết quả phiếu trả" onClose={onClose}>
            <h2>Kết quả phiếu trả</h2>

            {!result ? (
                <p className="muted-text">Chưa có dữ liệu</p>
            ) : (
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
                            { key: "maChiTietTra", title: "Mã CT trả" },
                            { key: "maChiTietMuon", title: "Mã CT mượn" },
                            { key: "tinhTrangKhiTra", title: "Tình trạng" },
                            { key: "soNgayTre", title: "Số ngày trễ" },
                            { key: "tienPhatTre", title: "Phạt trễ", render: (row) => formatMoney(row.tienPhatTre) },
                            { key: "tienPhatHongMat", title: "Phạt hỏng/mất", render: (row) => formatMoney(row.tienPhatHongMat) }
                        ]}
                    />
                </div>
            )}
        </ResultModal>
    );
}

function getReturnFineTotal(result) {
    return (result?.chiTiet || []).reduce(
        (sum, row) => sum + Number(row.tienPhatTre || 0) + Number(row.tienPhatHongMat || 0),
        0
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
