import { useMemo, useState } from "react";
import { RefreshCcw, Search } from "lucide-react";
import { staffApi } from "../../api/staffApi";
import PageHeader from "../../components/PageHeader";
import DataTable from "../../components/DataTable";
import StatusBadge from "../../components/StatusBadge";
import { useToast } from "../../components/ToastProvider";

export default function StaffPaymentsPage() {
    const toast = useToast();

    const [maDocGia, setMaDocGia] = useState("DG001");
    const [debts, setDebts] = useState([]);
    const [selected, setSelected] = useState({});
    const [result, setResult] = useState(null);
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
        return Number(row.soTienConLai ?? 0);
    }

    async function loadDebts() {
        try {
            const data = await staffApi.getReaderDebts(maDocGia);
            const list = Array.isArray(data) ? data : [];
            setDebts(list);
            setSelected({});
            toast.success("Đã tải khoản nợ");
        } catch (err) {
            toast.error(err.message || "Không tải được khoản nợ");
        }
    }

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
        setSelected((prev) => ({
            ...prev,
            [maKhoanNo]: Number(value)
        }));
    }

    const selectedTotal = useMemo(() => {
        return Object.values(selected).reduce((sum, value) => sum + Number(value || 0), 0);
    }, [selected]);

    function syncSelectedTotalToForm() {
        updateField("soTienThu", selectedTotal);
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
            toast.success("Thu tiền thành công");
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
            toast.success("Tự động phân bổ thu tiền thành công");
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
                    <button className="soft-button" onClick={loadDebts}>
                        <Search size={17} />
                        Xem nợ
                    </button>
                }
            />

            <div className="panel compact-form">
                <label>Mã độc giả</label>
                <input value={maDocGia} onChange={(e) => setMaDocGia(e.target.value)} />
                <button className="soft-button" onClick={loadDebts}>
                    Tải nợ
                </button>
            </div>

            <div className="panel">
                <div className="panel-title">
                    <h2>Danh sách khoản nợ</h2>
                    <span>{debts.length} khoản</span>
                </div>

                <DataTable
                    data={debts}
                    columns={[
                        {
                            key: "chon",
                            title: "Chọn",
                            render: (row) => (
                                <input
                                    type="checkbox"
                                    checked={selected[row.maKhoanNo] !== undefined}
                                    onChange={() => toggleDebt(row)}
                                />
                            )
                        },
                        { key: "maKhoanNo", title: "Mã nợ" },
                        { key: "maLoaiKhoanNo", title: "Loại" },
                        { key: "lyDo", title: "Lý do" },
                        {
                            key: "soTienPhatSinh",
                            title: "Phát sinh",
                            render: (row) => `${Number(row.soTienPhatSinh || 0).toLocaleString()}đ`
                        },
                        {
                            key: "soTienDaThanhToan",
                            title: "Đã trả",
                            render: (row) => `${Number(row.soTienDaThanhToan || 0).toLocaleString()}đ`
                        },
                        {
                            key: "soTienConLai",
                            title: "Còn lại",
                            render: (row) => `${Number(row.soTienConLai || 0).toLocaleString()}đ`
                        },
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
                        {
                            key: "trangThai",
                            title: "Trạng thái",
                            render: (row) => <StatusBadge value={row.trangThai} />
                        }
                    ]}
                />
            </div>

            <div className="form-layout">
                <form className="panel form-panel" onSubmit={createManualPayment}>
                    <div className="form-row">
                        <label>Mã phiếu thu</label>
                        <div className="inline-control">
                            <input
                                value={form.maPhieuThu}
                                onChange={(e) => updateField("maPhieuThu", e.target.value)}
                            />
                            <button type="button" className="icon-button" onClick={regenerateCode}>
                                <RefreshCcw size={17} />
                            </button>
                        </div>
                    </div>

                    <div className="form-grid-2">
                        <div className="form-row">
                            <label>Mã nhân viên thu</label>
                            <input
                                value={form.maNhanVienThu}
                                onChange={(e) => updateField("maNhanVienThu", e.target.value)}
                            />
                        </div>

                        <div className="form-row">
                            <label>Phương thức</label>
                            <input
                                value={form.maPhuongThuc}
                                onChange={(e) => updateField("maPhuongThuc", e.target.value)}
                            />
                        </div>
                    </div>

                    <div className="form-row">
                        <label>Số tiền thu</label>
                        <div className="inline-control">
                            <input
                                type="number"
                                value={form.soTienThu}
                                onChange={(e) => updateField("soTienThu", e.target.value)}
                            />
                            <button type="button" className="soft-button" onClick={syncSelectedTotalToForm}>
                                Lấy tổng đã chọn
                            </button>
                        </div>
                        <small>Tổng tiền đang chọn: {selectedTotal.toLocaleString()}đ</small>
                    </div>

                    <div className="form-row">
                        <label>Ghi chú</label>
                        <textarea
                            value={form.ghiChu}
                            onChange={(e) => updateField("ghiChu", e.target.value)}
                        />
                    </div>

                    <button className="primary-button" disabled={loading}>
                        Thu khoản đã chọn
                    </button>

                    <button
                        type="button"
                        className="soft-button"
                        disabled={loading}
                        onClick={createAutoPayment}
                    >
                        Tự động phân bổ
                    </button>
                </form>

                <div className="panel preview-panel">
                    <h2>Kết quả phiếu thu</h2>
                    <pre>{result ? JSON.stringify(result, null, 2) : "Chưa có dữ liệu"}</pre>
                </div>
            </div>
        </div>
    );
}
