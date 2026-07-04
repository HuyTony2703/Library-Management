import { Download, EyeOff, Pencil, Plus, RotateCcw, Settings2, Trash2, X } from "lucide-react";
import { useCallback, useEffect, useRef, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { libraryApi } from "../api/libraryApi";
import AsyncEntityPicker from "../components/AsyncEntityPicker";
import BookDetailDrawer from "../components/BookDetailDrawer";
import BookLifecycleDialog from "../components/BookLifecycleDialog";
import DataTable from "../components/DataTable";
import InlineActionMenu from "../components/InlineActionMenu";
import PageHeader from "../components/PageHeader";
import ResultModal from "../components/ResultModal";
import StatusBadge from "../components/StatusBadge";
import { useActionDialog } from "../components/ActionDialogProvider";
import { useToast } from "../components/ToastProvider";
import { useAuth } from "../context/AuthContext";
import { formatMoney } from "../utils/displayUtils";
import { downloadCsvExport, resolveExportResult } from "../utils/exportUtils";
import { isAdmin } from "../utils/roleUtils";

const DEFAULT_VISIBLE_COLUMNS = ["isbn", "publisher", "copies", "status"];
const CUSTOMIZABLE_COLUMNS = [
    { key: "isbn", label: "ISBN" },
    { key: "publisher", label: "NXB / năm" },
    { key: "copies", label: "Bản vật lý" },
    { key: "categories", label: "Thể loại" },
    { key: "language", label: "Ngôn ngữ" },
    { key: "value", label: "Trị giá" },
    { key: "status", label: "Trạng thái" }
];

export default function BooksPage() {
    const toast = useToast();
    const actionDialog = useActionDialog();
    const { user } = useAuth();
    const adminUser = isAdmin(user);
    const [searchParams, setSearchParams] = useSearchParams();
    const [data, setData] = useState([]);
    const [pageInfo, setPageInfo] = useState({ totalItems: 0, totalPages: 0 });
    const [tableLoading, setTableLoading] = useState(false);
    const [tableError, setTableError] = useState(null);
    const [operationLoading, setOperationLoading] = useState(false);
    const [exportLoading, setExportLoading] = useState(false);
    const [reloadVersion, setReloadVersion] = useState(0);
    const requestSequence = useRef(0);
    const urlSearch = searchParams.get("search") || "";
    const [searchDraft, setSearchDraft] = useState({ urlValue: urlSearch, value: urlSearch });
    const search = searchDraft.urlValue === urlSearch ? searchDraft.value : urlSearch;
    const page = readPositiveInteger(searchParams.get("page"), 1);
    const pageSize = readPageSize(searchParams.get("pageSize"));
    const sort = readSort(searchParams.get("sort"));
    const requestSort = sort || { field: "title", direction: "asc" };
    const hasIsbn = readBooleanFilter(searchParams.get("hasIsbn"));
    const statusId = searchParams.get("statusIds") || "";
    const categoryId = searchParams.get("categoryIds") || "";
    const authorId = searchParams.get("authorIds") || "";
    const publisherId = searchParams.get("publisherIds") || "";
    const yearFrom = readYearFilter(searchParams.get("yearFrom"));
    const yearTo = readYearFilter(searchParams.get("yearTo"));
    const visibleColumnKeys = readVisibleColumns(searchParams.get("columns"));
    const selectionQueryKey = [
        urlSearch,
        sort?.field || "",
        sort?.direction || "",
        hasIsbn,
        statusId,
        categoryId,
        authorId,
        publisherId,
        yearFrom || "",
        yearTo || "",
        pageSize
    ].join("|");
    const [showModal, setShowModal] = useState(false);
    const [editingBook, setEditingBook] = useState(null);
    const [selectionMode, setSelectionMode] = useState(false);
    const [selection, setSelection] = useState({ queryKey: selectionQueryKey, ids: [] });
    const selectedIds = selection.queryKey === selectionQueryKey ? selection.ids : [];
    const [form, setForm] = useState(() => buildDefaultBookForm());
    const [initialFormSnapshot, setInitialFormSnapshot] = useState("");
    const [fieldErrors, setFieldErrors] = useState({});
    const [isbnNotice, setIsbnNotice] = useState(null);
    const [bookStatuses, setBookStatuses] = useState([]);
    const [statusOptionsLoading, setStatusOptionsLoading] = useState(true);
    const [statusOptionsError, setStatusOptionsError] = useState("");
    const [filterOptionLabels, setFilterOptionLabels] = useState({});
    const [drawerBookId, setDrawerBookId] = useState(null);
    const [lifecycleDialog, setLifecycleDialog] = useState(null);
    const formDirty = showModal && isBookFormDirty(form, initialFormSnapshot);

    useEffect(() => {
        if (!formDirty) return undefined;
        const warnBeforeUnload = (event) => {
            event.preventDefault();
            event.returnValue = "";
        };
        window.addEventListener("beforeunload", warnBeforeUnload);
        return () => window.removeEventListener("beforeunload", warnBeforeUnload);
    }, [formDirty]);

    const updateUrl = useCallback((changes, replace = false) => {
        setSearchParams((current) => {
            const next = new URLSearchParams(current);
            Object.entries(changes).forEach(([key, value]) => {
                if (value === null || value === undefined || value === "") next.delete(key);
                else next.set(key, String(value));
            });
            return next;
        }, { replace });
    }, [setSearchParams]);

    useEffect(() => {
        const controller = new AbortController();
        const sequence = ++requestSequence.current;
        // Data fetching is an external synchronization; loading must reset for each query.
        // eslint-disable-next-line react-hooks/set-state-in-effect
        setTableLoading(true);
        setTableError(null);

        libraryApi.booksPage({
            page,
            pageSize,
            search: urlSearch,
            sort: `${requestSort.field},${requestSort.direction}`,
            hasIsbn,
            statusIds: statusId || null,
            categoryIds: categoryId || null,
            authorIds: authorId || null,
            publisherIds: publisherId || null,
            yearFrom,
            yearTo
        }, { signal: controller.signal })
            .then((response) => {
                if (sequence !== requestSequence.current) return;
                setData(Array.isArray(response.items) ? response.items : []);
                setPageInfo({
                    totalItems: Number(response.totalItems) || 0,
                    totalPages: Number(response.totalPages) || 0
                });
                if (response.totalPages > 0 && page > response.totalPages) {
                    updateUrl({ page: response.totalPages });
                }
            })
            .catch((err) => {
                if (err.name === "AbortError" || sequence !== requestSequence.current) return;
                setTableError(err.message || "Không tải được danh sách đầu sách");
            })
            .finally(() => {
                if (sequence === requestSequence.current) setTableLoading(false);
            });

        return () => controller.abort();
    }, [
        page,
        pageSize,
        urlSearch,
        requestSort.field,
        requestSort.direction,
        hasIsbn,
        statusId,
        categoryId,
        authorId,
        publisherId,
        yearFrom,
        yearTo,
        reloadVersion,
        updateUrl
    ]);

    useEffect(() => {
        let active = true;
        libraryApi.bookStatuses()
            .then((options) => {
                if (!active) return;
                setBookStatuses(Array.isArray(options) ? options : []);
                setStatusOptionsError("");
            })
            .catch((err) => {
                if (!active) return;
                setBookStatuses([]);
                setStatusOptionsError(err.message || "Không tải được trạng thái đầu sách");
            })
            .finally(() => {
                if (active) setStatusOptionsLoading(false);
            });
        return () => {
            active = false;
        };
    }, []);

    useEffect(() => {
        const timer = window.setTimeout(() => {
            const normalized = search.trim();
            if (normalized !== urlSearch) {
                updateUrl({ search: normalized || null, page: 1 }, true);
            }
        }, 250);

        return () => window.clearTimeout(timer);
    }, [search, urlSearch, updateUrl]);

    function reloadBooks() {
        setReloadVersion((value) => value + 1);
    }

    function selectedFilterOption(id) {
        if (!id) return null;
        return { value: id, code: id, label: filterOptionLabels[id] || id };
    }

    function changeEntityFilter(param, option) {
        if (option) {
            setFilterOptionLabels((current) => ({ ...current, [option.value]: option.label }));
        }
        updateUrl({ [param]: option?.value || null, page: 1 });
    }

    function toggleColumn(columnKey) {
        const next = visibleColumnKeys.includes(columnKey)
            ? visibleColumnKeys.filter((key) => key !== columnKey)
            : [...visibleColumnKeys, columnKey];
        const ordered = CUSTOMIZABLE_COLUMNS.map((column) => column.key)
            .filter((key) => next.includes(key));
        updateUrl({
            columns: arraysEqual(ordered, DEFAULT_VISIBLE_COLUMNS)
                ? null
                : ordered.length ? ordered.join(",") : "none"
        }, true);
    }

    function clearFilters() {
        setSearchDraft({ urlValue: "", value: "" });
        updateUrl({
            search: null,
            statusIds: null,
            categoryIds: null,
            authorIds: null,
            publisherIds: null,
            yearFrom: null,
            yearTo: null,
            hasIsbn: null,
            page: 1
        });
    }

    function setSelectedIds(nextValue) {
        setSelection((current) => {
            const currentIds = current.queryKey === selectionQueryKey ? current.ids : [];
            const nextIds = typeof nextValue === "function" ? nextValue(currentIds) : nextValue;
            return { queryKey: selectionQueryKey, ids: nextIds };
        });
    }

    function currentExportFilters() {
        return {
            page,
            pageSize,
            search: urlSearch,
            sort: `${requestSort.field},${requestSort.direction}`,
            hasIsbn,
            statusIds: statusId ? [statusId] : [],
            categoryIds: categoryId ? [categoryId] : [],
            authorIds: authorId ? [authorId] : [],
            publisherIds: publisherId ? [publisherId] : [],
            yearFrom,
            yearTo
        };
    }

    async function exportBooks(scope) {
        if (exportLoading) return;
        if (scope === "SELECTED" && selectedIds.length === 0) {
            toast.error("Chưa chọn đầu sách để export");
            return;
        }
        setExportLoading(true);
        try {
            const result = await libraryApi.exportBooks({
                scope,
                ids: scope === "SELECTED" ? selectedIds : [],
                filters: currentExportFilters(),
                excludedIds: []
            });
            const ready = await resolveExportResult(result, () => toast.info("Tập dữ liệu lớn, đang tạo file export"));
            downloadCsvExport(ready);
            toast.success(`Đã export ${Number(ready.totalRows || 0).toLocaleString("vi-VN")} dòng`);
        } catch (err) {
            toast.error(err.message || "Export đầu sách thất bại");
        } finally {
            setExportLoading(false);
        }
    }

    function updateField(field, value) {
        setForm((prev) => ({ ...prev, [field]: value }));
        const errorField = { authors: "maTacGias", categories: "maTheLoais", publisher: "maNhaXuatBan" }[field] || field;
        setFieldErrors((current) => ({ ...current, [errorField]: undefined }));
        if (field === "isbn") setIsbnNotice(null);
    }

    function openCreateModal() {
        const emptyForm = buildDefaultBookForm();
        setEditingBook(null);
        setForm(emptyForm);
        setInitialFormSnapshot(snapshotForm(emptyForm));
        setFieldErrors({});
        setIsbnNotice(null);
        setShowModal(true);
    }

    function openEditModal(row) {
        setEditingBook(row);
        const editForm = {
            publisher: row.maNhaXuatBan ? pickerOption(row.maNhaXuatBan, row.tenNhaXuatBan) : null,
            tenDauSach: row.tenDauSach || "",
            isbn: row.isbn || "",
            namXuatBan: row.namXuatBan ?? "",
            ngonNgu: row.ngonNgu || "",
            soTrang: row.soTrang ?? "",
            triGia: row.triGia ?? "",
            authors: pickerOptions(row.maTacGias, row.tenTacGias),
            categories: pickerOptions(row.maTheLoais, row.tenTheLoais),
            moTa: row.moTa || "",
            anhBia: row.anhBia || ""
        };
        setForm(editForm);
        setInitialFormSnapshot(snapshotForm(editForm));
        setFieldErrors({});
        setIsbnNotice(null);
        setShowModal(true);
    }

    function closeModal() {
        setShowModal(false);
        setEditingBook(null);
        setForm(buildDefaultBookForm());
        setInitialFormSnapshot("");
        setFieldErrors({});
        setIsbnNotice(null);
    }

    async function requestCloseModal() {
        if (!isBookFormDirty(form, initialFormSnapshot)) {
            closeModal();
            return;
        }
        const confirmed = await actionDialog.confirm({
            title: "Bỏ thay đổi chưa lưu?",
            message: "Thông tin vừa nhập sẽ bị mất.",
            confirmLabel: "Bỏ thay đổi",
            danger: true
        });
        if (confirmed) closeModal();
    }

    function buildPayload() {
        return {
            maNhaXuatBan: form.publisher?.value || null,
            tenDauSach: form.tenDauSach.trim(),
            isbn: normalizeIsbn(form.isbn) || null,
            namXuatBan: Number(form.namXuatBan),
            ngonNgu: form.ngonNgu.trim() || null,
            soTrang: Number(form.soTrang),
            moTa: form.moTa.trim() || null,
            anhBia: form.anhBia.trim() || null,
            triGia: Number(form.triGia),
            maTacGias: form.authors.map((option) => option.value),
            maTheLoais: form.categories.map((option) => option.value)
        };
    }

    async function checkIsbn() {
        const isbn = normalizeIsbn(form.isbn);
        if (!isbn) {
            setIsbnNotice(null);
            return;
        }
        if (!isValidIsbn(isbn)) {
            setFieldErrors((current) => ({ ...current, isbn: "ISBN-10/ISBN-13 không đúng checksum" }));
            setIsbnNotice(null);
            return;
        }
        try {
            const result = await libraryApi.checkBookIsbn(isbn, editingBook?.maDauSach);
            setIsbnNotice(result);
            setFieldErrors((current) => ({ ...current, isbn: result.duplicate ? "ISBN đã thuộc một đầu sách khác" : undefined }));
        } catch (err) {
            setIsbnNotice({ valid: false, message: err.message || "Không kiểm tra được ISBN" });
        }
    }

    async function saveBook(event) {
        event.preventDefault();
        if (operationLoading) return;
        const clientErrors = validateBookForm(form);
        if (Object.keys(clientErrors).length > 0) {
            setFieldErrors(clientErrors);
            toast.error("Vui lòng kiểm tra các trường chưa hợp lệ");
            return;
        }
        setOperationLoading(true);

        try {
            const payload = buildPayload();

            if (editingBook) {
                await libraryApi.updateBook(editingBook.maDauSach, payload);
            } else {
                await libraryApi.createBook(payload);
            }

            toast.success(editingBook ? "Cập nhật sách thành công" : "Thêm sách thành công");
            closeModal();
            reloadBooks();
        } catch (err) {
            if (err.data?.fieldErrors) setFieldErrors(err.data.fieldErrors);
            if (err.data?.errorCode === "DUPLICATE_ISBN") {
                setIsbnNotice({
                    valid: true,
                    duplicate: true,
                    message: err.message,
                    existingBook: err.data?.details?.existingBook
                });
            }
            toast.error(err.message || "Lưu sách thất bại");
        } finally {
            setOperationLoading(false);
        }
    }

    function hideBook(book) {
        setLifecycleDialog({ action: "deactivate", book });
    }

    async function hardDeleteBook(book) {
        const maDauSach = typeof book === "string" ? book : book.maDauSach;
        if (!adminUser || operationLoading) return;
        setOperationLoading(true);
        let preflight;
        try {
            preflight = await libraryApi.bookDeletePreflight(maDauSach);
        } catch (err) {
            toast.error(err.message || "Không kiểm tra được liên kết đầu sách");
            setOperationLoading(false);
            return;
        }
        setOperationLoading(false);
        if (!preflight.canDelete) {
            toast.error(`Không thể xóa: ${formatDependencies(preflight.dependencies)}`);
            return;
        }
        const confirmed = await actionDialog.confirm({
            title: `Xóa đầu sách ${maDauSach}`,
            message: "Đầu sách không có liên kết. Thao tác này sẽ xóa vĩnh viễn và được ghi audit.",
            confirmLabel: "Xóa",
            danger: true
        });

        if (!confirmed) return;

        setOperationLoading(true);
        try {
            await libraryApi.deleteBook(maDauSach);
            toast.success("Đã xóa đầu sách");
            setSelectedIds((prev) => prev.filter((id) => id !== maDauSach));
            setDrawerBookId(null);
            reloadBooks();
        } catch (err) {
            toast.error(err.message || "Xóa đầu sách thất bại");
        } finally {
            setOperationLoading(false);
        }
    }

    function restoreBook(book) {
        setLifecycleDialog({ action: "reactivate", book });
    }

    async function submitLifecycle(reason) {
        const { action, book } = lifecycleDialog;
        if (action === "deactivate") await libraryApi.deactivateBook(book.maDauSach, reason);
        else await libraryApi.reactivateBook(book.maDauSach, reason);
        toast.success(action === "deactivate" ? "Đã ngừng hiển thị đầu sách" : "Đã khôi phục đầu sách");
        setLifecycleDialog(null);
        setDrawerBookId(null);
        reloadBooks();
    }

    return (
        <div>
            <PageHeader
                eyebrow="Catalog"
                title="Quản lý đầu sách"
                description="Danh sách đầu sách, ISBN, trị giá và trạng thái hiển thị."
            />

            <div className="panel books-filter-panel">
                <div className="books-filter-search">
                    <label htmlFor="books-search">Tìm kiếm</label>
                    <input
                        id="books-search"
                        value={search}
                        onChange={(event) => setSearchDraft({ urlValue: urlSearch, value: event.target.value })}
                        placeholder="Mã, tên sách, ISBN, tác giả, thể loại..."
                    />
                </div>
                <div className="books-filter-field">
                    <label htmlFor="books-status-filter">Trạng thái</label>
                    <select
                        id="books-status-filter"
                        value={statusId}
                        onChange={(event) => updateUrl({ statusIds: event.target.value || null, page: 1 })}
                        disabled={statusOptionsLoading || Boolean(statusOptionsError)}
                    >
                        <option value="">Tất cả trạng thái</option>
                        {bookStatuses.map((option) => (
                            <option key={option.value} value={option.value}>{option.label}</option>
                        ))}
                    </select>
                </div>
                <BookFilterPicker
                    label="Thể loại"
                    value={selectedFilterOption(categoryId)}
                    onChange={(option) => changeEntityFilter("categoryIds", option)}
                    loadOptions={libraryApi.searchBookCategories}
                    placeholder="Tìm thể loại"
                />
                <BookFilterPicker
                    label="Tác giả"
                    value={selectedFilterOption(authorId)}
                    onChange={(option) => changeEntityFilter("authorIds", option)}
                    loadOptions={libraryApi.searchBookAuthors}
                    placeholder="Tìm tác giả"
                />
                <BookFilterPicker
                    label="Nhà xuất bản"
                    value={selectedFilterOption(publisherId)}
                    onChange={(option) => changeEntityFilter("publisherIds", option)}
                    loadOptions={libraryApi.searchBookPublishers}
                    placeholder="Tìm NXB"
                />
                <div className="books-filter-field books-filter-year">
                    <label htmlFor="books-year-from">Năm xuất bản</label>
                    <div>
                        <input
                            id="books-year-from"
                            type="number"
                            min="0"
                            max="9999"
                            value={yearFrom ?? ""}
                            onChange={(event) => updateUrl({ yearFrom: event.target.value || null, page: 1 }, true)}
                            placeholder="Từ năm"
                        />
                        <input
                            aria-label="Đến năm xuất bản"
                            type="number"
                            min="0"
                            max="9999"
                            value={yearTo ?? ""}
                            onChange={(event) => updateUrl({ yearTo: event.target.value || null, page: 1 }, true)}
                            placeholder="Đến năm"
                        />
                    </div>
                </div>
                <div className="books-filter-field">
                    <label htmlFor="books-isbn-filter">ISBN</label>
                    <select
                        id="books-isbn-filter"
                        value={hasIsbn === null ? "" : String(hasIsbn)}
                        onChange={(event) => updateUrl({ hasIsbn: event.target.value || null, page: 1 })}
                    >
                        <option value="">Tất cả</option>
                        <option value="true">Có ISBN</option>
                        <option value="false">Chưa có ISBN</option>
                    </select>
                </div>
                <button className="ghost-button books-clear-filters" type="button" onClick={clearFilters}>
                    <X size={16} /> Xóa bộ lọc
                </button>
                {statusOptionsError && <div className="books-filter-error" role="alert">{statusOptionsError}</div>}
            </div>

            {showModal && (
                <ResultModal title={editingBook ? `Sửa đầu sách ${editingBook.maDauSach}` : "Thêm đầu sách"} onClose={requestCloseModal} className="form-modal-card book-form-modal">
                    <form className="form-panel modal-form book-form" onSubmit={saveBook} noValidate>
                        {!editingBook && <p className="form-help">Mã đầu sách sẽ được hệ thống cấp sau khi lưu.</p>}

                        <fieldset>
                            <legend>Nhận diện</legend>
                            <div className="form-grid-2">
                                <BookField label="Tên đầu sách" error={fieldErrors.tenDauSach} required>
                                    <input value={form.tenDauSach} maxLength={200} onChange={(e) => updateField("tenDauSach", e.target.value)} aria-invalid={Boolean(fieldErrors.tenDauSach)} />
                                </BookField>
                                <BookField label="ISBN-10 / ISBN-13" error={fieldErrors.isbn}>
                                    <input value={form.isbn} maxLength={30} onChange={(e) => updateField("isbn", e.target.value)} onBlur={checkIsbn} aria-invalid={Boolean(fieldErrors.isbn)} placeholder="Có thể dùng dấu gạch ngang" />
                                    {isbnNotice && (
                                        <div className={`book-form-notice ${isbnNotice.duplicate || !isbnNotice.valid ? "is-warning" : "is-success"}`} role="status">
                                            {isbnNotice.message}
                                            {isbnNotice.existingBook && <span>{isbnNotice.existingBook.maDauSach} · {isbnNotice.existingBook.tenDauSach}</span>}
                                        </div>
                                    )}
                                </BookField>
                            </div>
                        </fieldset>

                        <fieldset>
                            <legend>Tác giả và thể loại</legend>
                            <div className="form-grid-2">
                                <BookField label="Tác giả" error={fieldErrors.maTacGias} required>
                                    <AsyncEntityPicker multiple required value={form.authors} onChange={(value) => updateField("authors", value)} loadOptions={libraryApi.searchBookAuthors} placeholder="Tìm tác giả theo tên" ariaLabel="Chọn tác giả" />
                                </BookField>
                                <BookField label="Thể loại" error={fieldErrors.maTheLoais} required>
                                    <AsyncEntityPicker multiple required value={form.categories} onChange={(value) => updateField("categories", value)} loadOptions={libraryApi.searchBookCategories} placeholder="Tìm thể loại theo tên" ariaLabel="Chọn thể loại" />
                                </BookField>
                            </div>
                        </fieldset>

                        <fieldset>
                            <legend>Xuất bản và trị giá</legend>
                            <div className="form-grid-3">
                                <BookField label="Nhà xuất bản" error={fieldErrors.maNhaXuatBan}>
                                    <AsyncEntityPicker value={form.publisher} onChange={(value) => updateField("publisher", value)} loadOptions={libraryApi.searchBookPublishers} placeholder="Tìm nhà xuất bản" ariaLabel="Chọn nhà xuất bản" />
                                </BookField>
                                <BookField label="Năm xuất bản" error={fieldErrors.namXuatBan} required>
                                    <input type="number" min="1" max={new Date().getFullYear()} value={form.namXuatBan} onChange={(e) => updateField("namXuatBan", e.target.value)} aria-invalid={Boolean(fieldErrors.namXuatBan)} />
                                </BookField>
                                <BookField label="Trị giá (VNĐ)" error={fieldErrors.triGia} required>
                                    <input type="number" min="0" step="1000" value={form.triGia} onChange={(e) => updateField("triGia", e.target.value)} aria-invalid={Boolean(fieldErrors.triGia)} />
                                </BookField>
                                <BookField label="Ngôn ngữ" error={fieldErrors.ngonNgu}>
                                    <input value={form.ngonNgu} maxLength={50} onChange={(e) => updateField("ngonNgu", e.target.value)} aria-invalid={Boolean(fieldErrors.ngonNgu)} />
                                </BookField>
                                <BookField label="Số trang" error={fieldErrors.soTrang} required>
                                    <input type="number" min="1" value={form.soTrang} onChange={(e) => updateField("soTrang", e.target.value)} aria-invalid={Boolean(fieldErrors.soTrang)} />
                                </BookField>
                            </div>
                        </fieldset>

                        <fieldset>
                            <legend>Ảnh bìa và mô tả</legend>
                            <div className="book-cover-fields">
                                <BookField label="URL ảnh bìa" error={fieldErrors.anhBia}>
                                    <input type="url" value={form.anhBia} maxLength={500} onChange={(e) => updateField("anhBia", e.target.value)} aria-invalid={Boolean(fieldErrors.anhBia)} placeholder="https://..." />
                                </BookField>
                                {isHttpUrl(form.anhBia) && <img className="book-cover-preview" src={form.anhBia} alt="Xem trước ảnh bìa" />}
                            </div>
                            <BookField label="Mô tả" error={fieldErrors.moTa}>
                                <textarea value={form.moTa} maxLength={4000} rows={4} onChange={(e) => updateField("moTa", e.target.value)} aria-invalid={Boolean(fieldErrors.moTa)} />
                            </BookField>
                        </fieldset>

                        <div className="book-form-actions">
                            <button className="ghost-button" type="button" onClick={requestCloseModal} disabled={operationLoading}>Hủy</button>
                            <button className="primary-button" disabled={operationLoading}>
                                {operationLoading ? "Đang lưu..." : editingBook ? "Lưu thông tin" : "Thêm đầu sách"}
                            </button>
                        </div>
                    </form>
                </ResultModal>
            )}

            {drawerBookId && (
                <BookDetailDrawer
                    bookId={drawerBookId}
                    onClose={() => setDrawerBookId(null)}
                    onEdit={(book) => { setDrawerBookId(null); openEditModal(book); }}
                    onDeactivate={hideBook}
                    onReactivate={restoreBook}
                    onHardDelete={hardDeleteBook}
                    canHardDelete={adminUser}
                />
            )}

            {lifecycleDialog && (
                <BookLifecycleDialog
                    book={lifecycleDialog.book}
                    action={lifecycleDialog.action}
                    onClose={() => setLifecycleDialog(null)}
                    onSubmit={submitLifecycle}
                />
            )}

            <div className="list-toolbar">
                <button className="primary-button" type="button" onClick={openCreateModal}>
                    <Plus size={17} />
                    Thêm sách
                </button>
                <div className="selection-toolbar">
                    <button className="soft-button" type="button" onClick={() => exportBooks("SELECTED")} disabled={!selectedIds.length || exportLoading}>
                        <Download size={15} /> Export đã chọn
                    </button>
                    <button className="soft-button" type="button" onClick={() => exportBooks("PAGE")} disabled={!data.length || exportLoading}>
                        <Download size={15} /> Export trang này
                    </button>
                    <button className="soft-button" type="button" onClick={() => exportBooks("ALL_MATCHING")} disabled={pageInfo.totalItems === 0 || exportLoading}>
                        <Download size={15} /> Export tất cả kết quả
                    </button>
                </div>
                <details className="column-customizer">
                    <summary className="ghost-button">
                        <Settings2 size={17} /> Tùy chỉnh cột
                    </summary>
                    <div className="column-customizer-menu">
                        <strong>Cột hiển thị</strong>
                        {CUSTOMIZABLE_COLUMNS.map((column) => (
                            <label key={column.key}>
                                <input
                                    type="checkbox"
                                    checked={visibleColumnKeys.includes(column.key)}
                                    onChange={() => toggleColumn(column.key)}
                                />
                                <span>{column.label}</span>
                            </label>
                        ))}
                    </div>
                </details>
            </div>

            <DataTable
                mode="server"
                data={data}
                rowKey="maDauSach"
                onRowClick={(row) => setDrawerBookId(row.maDauSach)}
                loading={tableLoading}
                error={tableError}
                onRetry={reloadBooks}
                emptyText="Không có đầu sách phù hợp"
                pagination={{
                    page,
                    pageSize,
                    totalItems: pageInfo.totalItems,
                    totalPages: pageInfo.totalPages,
                    onPageChange: (nextPage) => updateUrl({ page: nextPage }),
                    onPageSizeChange: (nextSize) => updateUrl({ pageSize: nextSize, page: 1 })
                }}
                sort={sort}
                onSortChange={(nextSort) => updateUrl({
                    sort: nextSort ? `${nextSort.field},${nextSort.direction}` : null,
                    page: 1
                })}
                selectable
                selectionMode={selectionMode}
                onSelectionModeChange={setSelectionMode}
                selectedRowKeys={selectedIds}
                onSelectionChange={setSelectedIds}
                bulkActions={[]}
                columns={[
                    {
                        key: "title",
                        title: "Tên đầu sách",
                        width: "38%",
                        minWidth: "320px",
                        align: "left",
                        wrap: true,
                        maxLines: 3,
                        sticky: "left",
                        sortable: true,
                        sortKey: "title",
                        render: (row) => (
                            <span className="book-title-cell">
                                <strong>{row.tenDauSach}</strong>
                                <span>{[row.maDauSach, ...(row.tenTacGias || [])].filter(Boolean).join(" · ")}</span>
                            </span>
                        )
                    },
                    { key: "isbn", title: "ISBN", width: "165px", minWidth: "150px", sortable: true },
                    {
                        key: "publisher",
                        title: "NXB / năm",
                        width: "220px",
                        minWidth: "190px",
                        align: "left",
                        wrap: true,
                        sortable: true,
                        sortKey: "publisher",
                        render: (row) => (
                            <span className="book-secondary-cell">
                                <strong>{row.tenNhaXuatBan || row.maNhaXuatBan || "—"}</strong>
                                <span>{row.namXuatBan || "Chưa có năm"}</span>
                            </span>
                        )
                    },
                    {
                        key: "copies",
                        title: "Bản vật lý",
                        width: "150px",
                        minWidth: "140px",
                        align: "right",
                        sortable: true,
                        sortKey: "totalCopies",
                        render: (row) => (
                            <span className="book-copy-summary">
                                <strong>{Number(row.soBanSanCo || 0).toLocaleString("vi-VN")}</strong>
                                <span>sẵn có / {Number(row.tongSoBan || 0).toLocaleString("vi-VN")} tổng</span>
                            </span>
                        )
                    },
                    {
                        key: "categories",
                        title: "Thể loại",
                        width: "210px",
                        minWidth: "180px",
                        align: "left",
                        wrap: true,
                        render: (row) => (row.tenTheLoais || []).join(", ") || "—"
                    },
                    { key: "language", title: "Ngôn ngữ", width: "140px", minWidth: "130px", sortable: true, sortKey: "language", render: (row) => row.ngonNgu || "—" },
                    { key: "value", title: "Trị giá", width: "150px", minWidth: "140px", align: "right", sortable: true, sortKey: "value", render: (row) => formatMoney(row.triGia) },
                    { key: "status", title: "Trạng thái", width: "180px", minWidth: "170px", sortable: true, sortKey: "status", render: (row) => <StatusBadge value={row.trangThai} /> },
                    {
                        key: "actions",
                        title: "Thao tác",
                        width: "120px",
                        minWidth: "120px",
                        sticky: "right",
                        render: (row) => (
                            <InlineActionMenu
                                label={`Mở thao tác cho đầu sách ${row.maDauSach}`}
                                disabled={operationLoading}
                                actions={[
                                    { key: "edit", label: "Sửa thông tin", icon: Pencil, onClick: () => openEditModal(row) },
                                    { key: "hide", label: "Ngừng hiển thị", icon: EyeOff, onClick: () => hideBook(row), disabled: row.trangThai === "Ngừng hiển thị" },
                                    { key: "restore", label: "Khôi phục", icon: RotateCcw, onClick: () => restoreBook(row), disabled: row.trangThai === "Hoạt động" },
                                    ...(adminUser ? [{ key: "delete", label: "Xóa cứng", icon: Trash2, danger: true, onClick: () => hardDeleteBook(row) }] : [])
                                ]}
                            />
                        )
                    }
                ].filter((column) => column.key === "title" || column.key === "actions" || visibleColumnKeys.includes(column.key))}
            />
        </div>
    );
}

function buildDefaultBookForm() {
    return {
        publisher: null,
        tenDauSach: "",
        isbn: "",
        namXuatBan: "",
        ngonNgu: "",
        soTrang: "",
        triGia: "",
        authors: [],
        categories: [],
        moTa: "",
        anhBia: ""
    };
}

function pickerOption(value, label) {
    return { value, code: value, label: label || value };
}

function pickerOptions(values = [], labels = []) {
    return values.map((value, index) => pickerOption(value, labels?.[index]));
}

function snapshotForm(form) {
    return JSON.stringify({
        ...form,
        publisher: form.publisher?.value || null,
        authors: form.authors.map((option) => option.value),
        categories: form.categories.map((option) => option.value)
    });
}

function isBookFormDirty(form, initialSnapshot) {
    return Boolean(initialSnapshot) && snapshotForm(form) !== initialSnapshot;
}

function normalizeIsbn(value) {
    return value.trim().replace(/[\s-]/g, "").toUpperCase();
}

function isValidIsbn(value) {
    const isbn = normalizeIsbn(value);
    if (/^\d{13}$/.test(isbn)) {
        const sum = [...isbn].reduce((total, digit, index) => total + Number(digit) * (index % 2 === 0 ? 1 : 3), 0);
        return sum % 10 === 0;
    }
    if (/^\d{9}[\dX]$/.test(isbn)) {
        const sum = [...isbn].reduce((total, digit, index) => total + (digit === "X" ? 10 : Number(digit)) * (10 - index), 0);
        return sum % 11 === 0;
    }
    return false;
}

function isHttpUrl(value) {
    return /^https?:\/\/.+/i.test(value.trim());
}

function validateBookForm(form) {
    const errors = {};
    const year = Number(form.namXuatBan);
    const pages = Number(form.soTrang);
    const value = Number(form.triGia);
    if (!form.tenDauSach.trim()) errors.tenDauSach = "Tên đầu sách không được để trống";
    else if (form.tenDauSach.trim().length > 200) errors.tenDauSach = "Tên đầu sách tối đa 200 ký tự";
    if (form.isbn.trim() && !isValidIsbn(form.isbn)) errors.isbn = "ISBN-10/ISBN-13 không đúng checksum";
    if (!form.namXuatBan || !Number.isInteger(year) || year < 1 || year > new Date().getFullYear()) errors.namXuatBan = "Năm xuất bản không hợp lệ";
    if (!form.soTrang || !Number.isInteger(pages) || pages < 1) errors.soTrang = "Số trang phải lớn hơn 0";
    if (form.triGia === "" || !Number.isFinite(value) || value < 0) errors.triGia = "Trị giá không được âm";
    if (!form.authors.length) errors.maTacGias = "Vui lòng chọn ít nhất một tác giả";
    if (!form.categories.length) errors.maTheLoais = "Vui lòng chọn ít nhất một thể loại";
    if (form.anhBia.trim() && !isHttpUrl(form.anhBia)) errors.anhBia = "Ảnh bìa phải là URL http hoặc https";
    if (form.ngonNgu.length > 50) errors.ngonNgu = "Ngôn ngữ tối đa 50 ký tự";
    if (form.moTa.length > 4000) errors.moTa = "Mô tả tối đa 4000 ký tự";
    return errors;
}

function formatDependencies(dependencies = {}) {
    const labels = {
        copies: "bản vật lý",
        reservations: "đặt trước",
        ratings: "đánh giá",
        comments: "bình luận",
        favorites: "yêu thích",
        auditLogs: "nhật ký"
    };
    return Object.entries(dependencies)
        .map(([key, count]) => `${count} ${labels[key] || key}`)
        .join(", ") || "còn liên kết dữ liệu";
}

function BookField({ label, error, required = false, children }) {
    return (
        <div className={`form-row${error ? " has-error" : ""}`}>
            <label>{label}{required && <span aria-hidden="true"> *</span>}</label>
            {children}
            {error && <span className="field-error" role="alert">{error}</span>}
        </div>
    );
}

function readPositiveInteger(value, fallback) {
    const parsed = Number.parseInt(value, 10);
    return Number.isInteger(parsed) && parsed > 0 ? parsed : fallback;
}

function readPageSize(value) {
    const parsed = Number.parseInt(value, 10);
    return [20, 50, 100].includes(parsed) ? parsed : 20;
}

function readSort(value) {
    const allowedFields = new Set(["id", "title", "isbn", "publisher", "year", "language", "value", "status"]);
    if (!value) return null;
    const [field, direction] = value.split(",");
    if (!allowedFields.has(field) || !["asc", "desc"].includes(direction)) {
        return null;
    }
    return { field, direction };
}

function readBooleanFilter(value) {
    if (value === "true") return true;
    if (value === "false") return false;
    return null;
}

function readYearFilter(value) {
    if (!value) return null;
    const parsed = Number.parseInt(value, 10);
    return Number.isInteger(parsed) && parsed >= 0 && parsed <= 9999 ? parsed : null;
}

function readVisibleColumns(value) {
    if (!value) return DEFAULT_VISIBLE_COLUMNS;
    if (value === "none") return [];
    const allowed = new Set(CUSTOMIZABLE_COLUMNS.map((column) => column.key));
    return value.split(",").map((key) => key.trim()).filter((key) => allowed.has(key));
}

function arraysEqual(left, right) {
    return left.length === right.length && left.every((value, index) => value === right[index]);
}

function BookFilterPicker({ label, value, onChange, loadOptions, placeholder }) {
    return (
        <div className="books-filter-field">
            <span className="books-filter-label">{label}</span>
            <AsyncEntityPicker
                value={value}
                onChange={onChange}
                loadOptions={loadOptions}
                placeholder={placeholder}
                ariaLabel={`Lọc theo ${label.toLowerCase()}`}
            />
        </div>
    );
}
