import { RefreshCcw, Plus, Trash2 } from "lucide-react";
import { useEffect, useState } from "react";
import { libraryApi } from "../api/libraryApi";
import DataTable from "../components/DataTable";
import PageHeader from "../components/PageHeader";
import StatusBadge from "../components/StatusBadge";
import { useToast } from "../components/ToastProvider";
import { formatDate } from "../utils/displayUtils";

export default function BookCopiesPage() {
    const toast = useToast();
    const [data, setData] = useState([]);
    const [loading, setLoading] = useState(false);
    const [form, setForm] = useState({
        maCuonSach: `COPY_${Date.now().toString().slice(-6)}`,
        maDauSach: "F01",
        maChiNhanh: "CN_TD",
        maViTri: "VT_M01_N01",
        maTrangThai: "TT_SANCO",
        maVach: "",
        maQrCode: "",
        ngayNhapSach: new Date().toISOString().slice(0, 10),
        ghiChu: ""
    });

    async function load() {
        try {
            setData(await libraryApi.bookCopies());
        } catch (err) {
            toast.error(err.message);
        }
    }

    useEffect(() => {
        load();
    }, []);

    function updateField(field, value) {
        setForm((prev) => ({ ...prev, [field]: value }));
    }

    async function createBookCopy(e) {
        e.preventDefault();
        setLoading(true);

        try {
            await libraryApi.createBookCopy({
                ...form,
                maVach: form.maVach || null,
                maQrCode: form.maQrCode || null,
                ghiChu: form.ghiChu || null
            });

            toast.success("Thêm cuốn sách thành công");
            updateField("maCuonSach", `COPY_${Date.now().toString().slice(-6)}`);
            await load();
        } catch (err) {
            toast.error(err.message || "Thêm cuốn sách thất bại");
        } finally {
            setLoading(false);
        }
    }

    async function deleteBookCopy(maCuonSach) {
        if (!window.confirm(`Xóa/ngừng lưu thông cuốn sách ${maCuonSach}?`)) {
            return;
        }

        setLoading(true);

        try {
            await libraryApi.deleteBookCopy(maCuonSach);
            toast.success("Đã ngừng lưu thông cuốn sách");
            await load();
        } catch (err) {
            toast.error(err.message || "Xóa cuốn sách thất bại");
        } finally {
            setLoading(false);
        }
    }

    return (
        <div>
            <PageHeader
                eyebrow="Inventory"
                title="Quản lý cuốn sách"
                description="Theo dõi từng bản sách vật lý, vị trí, chi nhánh và trạng thái."
                right={<button className="soft-button" onClick={load}><RefreshCcw size={17} /> Tải lại</button>}
            />

            <form className="panel form-panel" onSubmit={createBookCopy}>
                <div className="panel-title">
                    <h2>Thêm cuốn sách</h2>
                    <Plus size={20} />
                </div>

                <div className="form-grid-3">
                    <div className="form-row">
                        <label>Mã cuốn sách</label>
                        <input value={form.maCuonSach} onChange={(e) => updateField("maCuonSach", e.target.value)} />
                    </div>
                    <div className="form-row">
                        <label>Mã đầu sách</label>
                        <input value={form.maDauSach} onChange={(e) => updateField("maDauSach", e.target.value)} />
                    </div>
                    <div className="form-row">
                        <label>Chi nhánh</label>
                        <input value={form.maChiNhanh} onChange={(e) => updateField("maChiNhanh", e.target.value)} />
                    </div>
                </div>

                <div className="form-grid-3">
                    <div className="form-row">
                        <label>Vị trí</label>
                        <input value={form.maViTri} onChange={(e) => updateField("maViTri", e.target.value)} />
                    </div>
                    <div className="form-row">
                        <label>Trạng thái</label>
                        <select value={form.maTrangThai} onChange={(e) => updateField("maTrangThai", e.target.value)}>
                            <option value="TT_SANCO">Sẵn có</option>
                            <option value="TT_DANGMUON">Đang mượn</option>
                            <option value="TT_DANGDATTRUOC">Đang đặt trước</option>
                            <option value="TT_HONG">Hỏng</option>
                            <option value="TT_MAT">Mất</option>
                            <option value="TT_NGUNGLUUTHONG">Ngừng lưu thông</option>
                        </select>
                    </div>
                    <div className="form-row">
                        <label>Ngày nhập sách</label>
                        <input type="date" value={form.ngayNhapSach} onChange={(e) => updateField("ngayNhapSach", e.target.value)} />
                    </div>
                </div>

                <div className="form-grid-3">
                    <div className="form-row">
                        <label>Mã vạch</label>
                        <input value={form.maVach} onChange={(e) => updateField("maVach", e.target.value)} />
                    </div>
                    <div className="form-row">
                        <label>Mã QR</label>
                        <input value={form.maQrCode} onChange={(e) => updateField("maQrCode", e.target.value)} />
                    </div>
                    <div className="form-row">
                        <label>Ghi chú</label>
                        <input value={form.ghiChu} onChange={(e) => updateField("ghiChu", e.target.value)} />
                    </div>
                </div>

                <button className="primary-button" disabled={loading}>
                    Thêm cuốn sách
                </button>
            </form>

            <DataTable
                data={data}
                columns={[
                    { key: "maCuonSach", title: "Mã cuốn" },
                    { key: "maDauSach", title: "Đầu sách" },
                    { key: "maChiNhanh", title: "Chi nhánh" },
                    { key: "maViTri", title: "Vị trí" },
                    { key: "maTrangThai", title: "Trạng thái", render: (row) => <StatusBadge value={row.maTrangThai} /> },
                    { key: "ngayNhapSach", title: "Ngày nhập", render: (row) => formatDate(row.ngayNhapSach) },
                    { key: "maVach", title: "Mã vạch" },
                    {
                        key: "actions",
                        title: "Thao tác",
                        render: (row) => (
                            <button className="soft-button danger-button" onClick={() => deleteBookCopy(row.maCuonSach)}>
                                <Trash2 size={15} />
                                Xóa
                            </button>
                        )
                    }
                ]}
            />
        </div>
    );
}
