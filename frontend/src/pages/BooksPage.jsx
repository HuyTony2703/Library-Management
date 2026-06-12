import { EyeOff, Pencil, Plus, RotateCcw, Trash2 } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { libraryApi } from "../api/libraryApi";
import DataTable from "../components/DataTable";
import InlineActionMenu from "../components/InlineActionMenu";
import PageHeader from "../components/PageHeader";
import ResultModal from "../components/ResultModal";
import StatusBadge from "../components/StatusBadge";
import { useActionDialog } from "../components/ActionDialogProvider";
import { useToast } from "../components/ToastProvider";
import { formatMoney } from "../utils/displayUtils";

export default function BooksPage() {
    const toast = useToast();
    const actionDialog = useActionDialog();
    const [searchParams, setSearchParams] = useSearchParams();
    const [data, setData] = useState([]);
    const [loading, setLoading] = useState(false);
    const [search, setSearch] = useState(searchParams.get("search") || "");
    const [showModal, setShowModal] = useState(false);
    const [editingBook, setEditingBook] = useState(null);
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
    }, [search, setSearchParams]);

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

    function updateField(field, value) {
        setForm((prev) => ({ ...prev, [field]: value }));
    }

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

    function parseIds(value) {
        return value.split(",").map((item) => item.trim()).filter(Boolean);
    }

    function openCreateModal() {
        setEditingBook(null);
        setForm(buildDefaultBookForm());
        setShowModal(true);
    }

    function openEditModal(row) {
        setEditingBook(row);
        setForm({
            maDauSach: row.maDauSach || "",
            maNhaXuatBan: row.maNhaXuatBan || "",
            tenDauSach: row.tenDauSach || "",
            isbn: row.isbn || "",
            namXuatBan: row.namXuatBan || new Date().getFullYear(),
            ngonNgu: row.ngonNgu || "Tiếng Việt",
            soTrang: row.soTrang || 120,
            triGia: row.triGia || 0,
            maTacGiasText: (row.maTacGias || []).join(", "),
            maTheLoaisText: (row.maTheLoais || []).join(", "),
            moTa: row.moTa || ""
        });
        setShowModal(true);
    }

    function closeModal() {
        setShowModal(false);
        setEditingBook(null);
        setForm(buildDefaultBookForm());
    }

    function buildPayload() {
        const maTacGias = parseIds(form.maTacGiasText);
        const maTheLoais = parseIds(form.maTheLoaisText);

        if (maTacGias.length === 0 || maTheLoais.length === 0) {
            throw new Error("Vui lòng nhập ít nhất một tác giả và một thể loại");
        }

        return {
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
        };
    }

    async function saveBook(event) {
        event.preventDefault();
        setLoading(true);

        try {
            const payload = buildPayload();

            if (editingBook) {
                await libraryApi.updateBook(editingBook.maDauSach, payload);
            } else {
                await libraryApi.createBook(payload);
            }

            toast.success(editingBook ? "Cập nhật sách thành công" : "Thêm sách thành công");
            closeModal();
            await load();
        } catch (err) {
            toast.error(err.message || "Lưu sách thất bại");
        } finally {
            setLoading(false);
        }
    }

    async function hideBook(maDauSach) {
        const confirmed = await actionDialog.confirm({
            title: `Ẩn đầu sách ${maDauSach}`,
            message: "Đầu sách sẽ ngừng hiển thị nhưng dữ liệu vẫn được giữ lại.",
            confirmLabel: "Ẩn"
        });

        if (!confirmed) return;

        setLoading(true);
        try {
            await libraryApi.deleteBook(maDauSach, "soft");
            toast.success("Đã ẩn đầu sách");
            await load();
        } catch (err) {
            toast.error(err.message || "Ẩn đầu sách thất bại");
        } finally {
            setLoading(false);
        }
    }

    async function hardDeleteBook(maDauSach) {
        const confirmed = await actionDialog.confirm({
            title: `Xóa đầu sách ${maDauSach}`,
            message: "Thao tác này sẽ xóa vĩnh viễn nếu dữ liệu chưa có liên kết nghiệp vụ.",
            confirmLabel: "Xóa",
            danger: true
        });

        if (!confirmed) return;

        setLoading(true);
        try {
            await libraryApi.deleteBook(maDauSach, "hard");
            toast.success("Đã xóa đầu sách");
            setSelectedIds((prev) => prev.filter((id) => id !== maDauSach));
            await load();
        } catch (err) {
            toast.error(err.message || "Xóa đầu sách thất bại");
        } finally {
            setLoading(false);
        }
    }

    async function restoreBook(maDauSach) {
        const confirmed = await actionDialog.confirm({
            title: `Khôi phục đầu sách ${maDauSach}`,
            message: "Đầu sách sẽ được đưa về trạng thái hoạt động.",
            confirmLabel: "Khôi phục"
        });

        if (!confirmed) return;

        setLoading(true);
        try {
            await libraryApi.restoreBook(maDauSach);
            toast.success("Đã khôi phục đầu sách");
            await load();
        } catch (err) {
            toast.error(err.message || "Khôi phục đầu sách thất bại");
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

        if (!mode) return;

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

            {showModal && (
                <ResultModal title={editingBook ? "Sửa thông tin sách" : "Thêm sách"} onClose={closeModal} className="form-modal-card">
                    <form className="form-panel modal-form" onSubmit={saveBook}>
                        <div className="form-grid-3">
                            <div className="form-row">
                                <label>Mã đầu sách</label>
                                <input value={form.maDauSach} onChange={(e) => updateField("maDauSach", e.target.value)} disabled={Boolean(editingBook)} />
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
                            {editingBook ? "Lưu thông tin" : "Thêm sách"}
                        </button>
                    </form>
                </ResultModal>
            )}

            <div className="list-toolbar">
                <button className="primary-button" type="button" onClick={openCreateModal}>
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
                        width: "120px",
                        render: (row) => (
                            <InlineActionMenu
                                label={`Mở thao tác cho đầu sách ${row.maDauSach}`}
                                disabled={loading}
                                actions={[
                                    { key: "edit", label: "Sửa thông tin", icon: Pencil, onClick: () => openEditModal(row) },
                                    { key: "hide", label: "Ẩn", icon: EyeOff, onClick: () => hideBook(row.maDauSach), disabled: row.trangThai === "Ngừng hiển thị" },
                                    { key: "restore", label: "Khôi phục", icon: RotateCcw, onClick: () => restoreBook(row.maDauSach), disabled: row.trangThai === "Hoạt động" },
                                    { key: "delete", label: "Xóa", icon: Trash2, danger: true, onClick: () => hardDeleteBook(row.maDauSach) }
                                ]}
                            />
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
