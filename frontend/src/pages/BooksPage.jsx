import { RefreshCcw, Plus, Trash2 } from "lucide-react";
import { useEffect, useState } from "react";
import { libraryApi } from "../api/libraryApi";
import DataTable from "../components/DataTable";
import PageHeader from "../components/PageHeader";
import StatusBadge from "../components/StatusBadge";
import { useToast } from "../components/ToastProvider";
import { formatMoney } from "../utils/displayUtils";

export default function BooksPage() {
    const toast = useToast();
    const [data, setData] = useState([]);
    const [loading, setLoading] = useState(false);
    const [form, setForm] = useState({
        maDauSach: `BOOK_${Date.now().toString().slice(-6)}`,
        maNhaXuatBan: "NXB_KIMDONG",
        tenDauSach: "Sách test nghiệp vụ",
        isbn: "",
        namXuatBan: new Date().getFullYear(),
        ngonNgu: "Tiếng Việt",
        soTrang: 120,
        triGia: 50000,
        maTacGiasText: "TG_NNA",
        maTheLoaisText: "TL_VANHOC",
        moTa: ""
    });

    async function load() {
        try {
            setData(await libraryApi.books());
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

    function parseIds(value) {
        return value.split(",").map((item) => item.trim()).filter(Boolean);
    }

    async function createBook(e) {
        e.preventDefault();

        const maTacGias = parseIds(form.maTacGiasText);
        const maTheLoais = parseIds(form.maTheLoaisText);

        if (maTacGias.length === 0 || maTheLoais.length === 0) {
            toast.error("Vui lòng nhập ít nhất một tác giả và một thể loại");
            return;
        }

        setLoading(true);

        try {
            await libraryApi.createBook({
                maDauSach: form.maDauSach,
                maNhaXuatBan: form.maNhaXuatBan || null,
                tenDauSach: form.tenDauSach,
                isbn: form.isbn || null,
                namXuatBan: Number(form.namXuatBan),
                ngonNgu: form.ngonNgu,
                soTrang: Number(form.soTrang),
                moTa: form.moTa,
                anhBia: null,
                triGia: Number(form.triGia),
                maTacGias,
                maTheLoais
            });

            toast.success("Thêm sách thành công");
            updateField("maDauSach", `BOOK_${Date.now().toString().slice(-6)}`);
            await load();
        } catch (err) {
            toast.error(err.message || "Thêm sách thất bại");
        } finally {
            setLoading(false);
        }
    }

    async function deleteBook(maDauSach) {
        if (!window.confirm(`Xóa/ngừng hiển thị đầu sách ${maDauSach}?`)) {
            return;
        }

        setLoading(true);

        try {
            await libraryApi.deleteBook(maDauSach);
            toast.success("Đã ngừng hiển thị đầu sách");
            await load();
        } catch (err) {
            toast.error(err.message || "Xóa sách thất bại");
        } finally {
            setLoading(false);
        }
    }

    return (
        <div>
            <PageHeader
                eyebrow="Catalog"
                title="Quản lý đầu sách"
                description="Danh sách đầu sách, ISBN, trị giá và trạng thái hiển thị."
                right={<button className="soft-button" onClick={load}><RefreshCcw size={17} /> Tải lại</button>}
            />

            <form className="panel form-panel" onSubmit={createBook}>
                <div className="panel-title">
                    <h2>Thêm sách</h2>
                    <Plus size={20} />
                </div>

                <div className="form-grid-3">
                    <div className="form-row">
                        <label>Mã đầu sách</label>
                        <input value={form.maDauSach} onChange={(e) => updateField("maDauSach", e.target.value)} />
                    </div>
                    <div className="form-row">
                        <label>Tên đầu sách</label>
                        <input value={form.tenDauSach} onChange={(e) => updateField("tenDauSach", e.target.value)} />
                    </div>
                    <div className="form-row">
                        <label>Nhà xuất bản</label>
                        <input value={form.maNhaXuatBan} onChange={(e) => updateField("maNhaXuatBan", e.target.value)} />
                    </div>
                </div>

                <div className="form-grid-3">
                    <div className="form-row">
                        <label>ISBN</label>
                        <input value={form.isbn} onChange={(e) => updateField("isbn", e.target.value)} />
                    </div>
                    <div className="form-row">
                        <label>Năm xuất bản</label>
                        <input type="number" value={form.namXuatBan} onChange={(e) => updateField("namXuatBan", e.target.value)} />
                    </div>
                    <div className="form-row">
                        <label>Trị giá</label>
                        <input type="number" value={form.triGia} onChange={(e) => updateField("triGia", e.target.value)} />
                    </div>
                </div>

                <div className="form-grid-3">
                    <div className="form-row">
                        <label>Ngôn ngữ</label>
                        <input value={form.ngonNgu} onChange={(e) => updateField("ngonNgu", e.target.value)} />
                    </div>
                    <div className="form-row">
                        <label>Số trang</label>
                        <input type="number" value={form.soTrang} onChange={(e) => updateField("soTrang", e.target.value)} />
                    </div>
                    <div className="form-row">
                        <label>Tác giả</label>
                        <input value={form.maTacGiasText} onChange={(e) => updateField("maTacGiasText", e.target.value)} placeholder="TG001, TG002" />
                    </div>
                </div>

                <div className="form-grid-2">
                    <div className="form-row">
                        <label>Thể loại</label>
                        <input value={form.maTheLoaisText} onChange={(e) => updateField("maTheLoaisText", e.target.value)} placeholder="TL_MANGA, TL_VANHOC" />
                    </div>
                    <div className="form-row">
                        <label>Mô tả</label>
                        <input value={form.moTa} onChange={(e) => updateField("moTa", e.target.value)} />
                    </div>
                </div>

                <button className="primary-button" disabled={loading}>
                    Thêm sách
                </button>
            </form>

            <DataTable
                data={data}
                columns={[
                    { key: "maDauSach", title: "Mã" },
                    { key: "tenDauSach", title: "Tên đầu sách" },
                    { key: "isbn", title: "ISBN" },
                    { key: "namXuatBan", title: "Năm XB" },
                    { key: "triGia", title: "Trị giá", render: (row) => formatMoney(row.triGia) },
                    { key: "trangThai", title: "Trạng thái", render: (row) => <StatusBadge value={row.trangThai} /> },
                    {
                        key: "actions",
                        title: "Thao tác",
                        render: (row) => (
                            <button className="soft-button danger-button" onClick={() => deleteBook(row.maDauSach)}>
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
