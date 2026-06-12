import { Plus, Trash2 } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { libraryApi } from "../api/libraryApi";
import DataTable from "../components/DataTable";
import PageHeader from "../components/PageHeader";
import ResultModal from "../components/ResultModal";
import StatusBadge from "../components/StatusBadge";
import { useToast } from "../components/ToastProvider";
import { useActionDialog } from "../components/ActionDialogProvider";
import { formatMoney } from "../utils/displayUtils";

export default function BooksPage() {
    const toast = useToast();
    const actionDialog = useActionDialog();
    const [searchParams, setSearchParams] = useSearchParams();
    const [data, setData] = useState([]);
    const [loading, setLoading] = useState(false);
    const [search, setSearch] = useState(searchParams.get("search") || "");
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [selectedIds, setSelectedIds] = useState([]);
    const [form, setForm] = useState(() => buildDefaultBookForm());

    async function load() {
        try {
            setData(await libraryApi.books());
        } catch (err) {
            toast.error(err.message || "Không tải được danh sách đầu sách");
        }
    }

    useEffect(() => {
        load();
    }, []);

    useEffect(() => {
        const timer = window.setTimeout(() => {
            setSearchParams(search.trim() ? { search: search.trim() } : {}, { replace: true });
        }, 250);

        return () => window.clearTimeout(timer);
    }, [search]);

    const filteredData = useMemo(() => {
        const keyword = search.trim().toLowerCase();

        if (!keyword) {
            return data;
        }

        return data.filter((row) =>
            [
                row.maDauSach,
                row.tenDauSach,
                row.isbn,
                row.namXuatBan,
                row.triGia,
                row.trangThai,
                ...(row.maTacGias || []),
                ...(row.maTheLoais || [])
            ]
                .filter((value) => value !== null && value !== undefined)
                .some((value) => String(value).toLowerCase().includes(keyword))
        );
    }, [data, search]);

    useEffect(() => {
        setSelectedIds((prev) => prev.filter((id) => data.some((row) => row.maDauSach === id)));
    }, [data]);

    function toggleSelected(id) {
        setSelectedIds((prev) => prev.includes(id)
            ? prev.filter((value) => value !== id)
            : [...prev, id]);
    }

    function selectAllVisible() {
        setSelectedIds(filteredData.map((row) => row.maDauSach));
    }

    function clearSelected() {
        setSelectedIds([]);
    }

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
            setForm(buildDefaultBookForm());
            setShowCreateModal(false);
            await load();
        } catch (err) {
            toast.error(err.message || "Thêm sách thất bại");
        } finally {
            setLoading(false);
        }
    }

    async function deleteBook(maDauSach) {
        const mode = await actionDialog.chooseDeleteMode(`đầu sách ${maDauSach}`);

        if (!mode) {
            return;
        }

        setLoading(true);

        try {
            await libraryApi.deleteBook(maDauSach, mode);
            toast.success(mode === "hard" ? "Đã xóa đầu sách" : "Đã ngừng hiển thị đầu sách");
            setSelectedIds((prev) => prev.filter((id) => id !== maDauSach));
            await load();
        } catch (err) {
            toast.error(err.message || "Xóa sách thất bại");
        } finally {
            setLoading(false);
        }
    }

    async function deleteSelectedBooks() {
        if (selectedIds.length === 0) {
            toast.error("Vui lòng chọn ít nhất một đầu sách");
            return;
        }

        const mode = await actionDialog.chooseDeleteMode(`${selectedIds.length} đầu sách đã chọn`);

        if (!mode) {
            return;
        }

        setLoading(true);

        try {
            for (const id of selectedIds) {
                await libraryApi.deleteBook(id, mode);
            }

            toast.success(mode === "hard" ? "Đã xóa các đầu sách đã chọn" : "Đã ngừng hiển thị các đầu sách đã chọn");
            setSelectedIds([]);
            await load();
        } catch (err) {
            toast.error(err.message || "Xóa các đầu sách đã chọn thất bại");
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
            />

            <div className="panel search-panel">
                <input
                    value={search}
                    onChange={(event) => setSearch(event.target.value)}
                    placeholder="Tìm theo mã, tên sách, ISBN, tác giả, thể loại..."
                />
            </div>

            {showCreateModal && (
                <ResultModal title="Thêm sách" onClose={() => setShowCreateModal(false)} className="form-modal-card">
                    <form className="form-panel modal-form" onSubmit={createBook}>
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
                </ResultModal>
            )}

            <div className="list-toolbar">
                <button className="primary-button" type="button" onClick={() => setShowCreateModal(true)}>
                    <Plus size={17} />
                    Thêm sách
                </button>

                <div className="selection-toolbar">
                    <button className="ghost-button" type="button" onClick={selectAllVisible}>
                        Chọn tất cả
                    </button>
                    <button className="soft-button" type="button" onClick={clearSelected}>
                        Bỏ chọn tất cả
                    </button>
                    <button className="soft-button danger-button" type="button" onClick={deleteSelectedBooks} disabled={selectedIds.length === 0 || loading}>
                        <Trash2 size={15} />
                        Xóa
                    </button>
                </div>
            </div>

            <DataTable
                data={filteredData}
                rowClassName={(row) => selectedIds.includes(row.maDauSach) ? "selected-row" : ""}
                columns={[
                    {
                        key: "select",
                        title: "",
                        className: "selection-count-cell",
                        width: "76px",
                        render: (row) => (
                            <input
                                className="table-checkbox"
                                type="checkbox"
                                checked={selectedIds.includes(row.maDauSach)}
                                onChange={() => toggleSelected(row.maDauSach)}
                            />
                        )
                    },
                    { key: "maDauSach", title: "Mã" },
                    { key: "tenDauSach", title: "Tên đầu sách" },
                    { key: "isbn", title: "ISBN" },
                    { key: "namXuatBan", title: "Năm XB" },
                    { key: "triGia", title: "Trị giá", render: (row) => formatMoney(row.triGia) },
                    { key: "trangThai", title: "Trạng thái", render: (row) => <StatusBadge value={row.trangThai} /> },
                    {
                        key: "actions",
                        title: "Thao tác",
                        width: "130px",
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

function buildDefaultBookForm() {
    const suffix = Date.now().toString().slice(-6);

    return {
        maDauSach: `BOOK_${suffix}`,
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
    };
}
