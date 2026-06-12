import { useEffect, useState } from "react";
import { libraryApi } from "../api/libraryApi";
import DataTable from "../components/DataTable";
import PageHeader from "../components/PageHeader";
import StatusBadge from "../components/StatusBadge";
import { useToast } from "../components/ToastProvider";

export default function PaymentsPage() {
    const toast = useToast();

    const [maDocGia, setMaDocGia] = useState("DG003");
    const [debts, setDebts] = useState([]);
    const [form, setForm] = useState({
        maPhieuThu: `PT_FE_${Date.now().toString().slice(-8)}`,
        maKhoanNo: "NO_DG003_TRE_01",
        maPhuongThuc: "PT_TIEN_MAT",
        soTienThu: 3000
    });

    function update(key, value) {
        setForm((prev) => ({ ...prev, [key]: value }));
    }

    async function loadDebts(silent = false) {
        if (!maDocGia.trim()) {
            setDebts([]);
            return;
        }

        try {
            const data = await libraryApi.debts(maDocGia.trim());
            setDebts(data);
            if (!silent) {
                toast.success("Đã tải danh sách khoản nợ");
            }
        } catch (err) {
            setDebts([]);
            if (!silent) {
                toast.error(err.message);
            }
        }
    }

    useEffect(() => {
        const timer = window.setTimeout(() => {
            loadDebts(true);
        }, 350);

        return () => window.clearTimeout(timer);
    }, [maDocGia]);

    async function submit(e) {
        e.preventDefault();

        try {
            await libraryApi.createPayment({
                maPhieuThu: form.maPhieuThu,
                maDocGia: maDocGia.trim(),
                maNhanVienThu: "NV_TT001",
                maPhuongThuc: form.maPhuongThuc,
                soTienThu: Number(form.soTienThu),
                ghiChu: "Thu tiền từ desktop app",
                chiTietNo: [
                    {
                        maKhoanNo: form.maKhoanNo,
                        soTienApDung: Number(form.soTienThu)
                    }
                ]
            });

            toast.success("Thu tiền thành công");
            await loadDebts(true);
        } catch (err) {
            toast.error(err.message);
        }
    }

    return (
        <div>
            <PageHeader
                eyebrow="Finance"
                title="Khoản nợ / Phiếu thu"
                description="Theo dõi công nợ và lập phiếu thu tiền phạt."
            />

            <div className="panel form-panel compact-form">
                <label>Mã độc giả</label>
                <input value={maDocGia} onChange={(e) => setMaDocGia(e.target.value)} />
            </div>

            <DataTable
                data={debts}
                columns={[
                    { key: "maKhoanNo", title: "Mã nợ" },
                    { key: "lyDo", title: "Lý do" },
                    { key: "soTienPhatSinh", title: "Phát sinh", render: (r) => `${Number(r.soTienPhatSinh || 0).toLocaleString()}đ` },
                    { key: "soTienConLai", title: "Còn lại", render: (r) => `${Number(r.soTienConLai || 0).toLocaleString()}đ` },
                    { key: "trangThai", title: "Trạng thái", render: (r) => <StatusBadge value={r.trangThai} /> }
                ]}
            />

            <form className="panel form-panel" onSubmit={submit}>
                <div className="form-grid-3">
                    <div className="form-row">
                        <label>Mã phiếu thu</label>
                        <input value={form.maPhieuThu} onChange={(e) => update("maPhieuThu", e.target.value)} />
                    </div>

                    <div className="form-row">
                        <label>Mã khoản nợ</label>
                        <input value={form.maKhoanNo} onChange={(e) => update("maKhoanNo", e.target.value)} />
                    </div>

                    <div className="form-row">
                        <label>Số tiền thu</label>
                        <input type="number" value={form.soTienThu} onChange={(e) => update("soTienThu", e.target.value)} />
                    </div>

                    <div className="form-row">
                        <label>Phương thức</label>
                        <select value={form.maPhuongThuc} onChange={(e) => update("maPhuongThuc", e.target.value)}>
                            <option value="PT_TIEN_MAT">Tiền mặt</option>
                            <option value="PT_CHUYEN_KHOAN">Chuyển khoản</option>
                            <option value="PT_VI_DIEN_TU">Ví điện tử</option>
                        </select>
                    </div>
                </div>

                <button className="primary-button">Tạo phiếu thu</button>
            </form>
        </div>
    );
}
