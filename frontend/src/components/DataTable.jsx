import {
    ArrowDown,
    ArrowUp,
    ArrowUpDown,
    ChevronsLeft,
    ChevronsRight,
    ChevronLeft,
    ChevronRight,
    RotateCcw
} from "lucide-react";
import { useEffect, useMemo, useRef, useState } from "react";

const SERVER_PAGE_SIZES = [20, 50, 100];

export default function DataTable({
    columns,
    data,
    mode = "local",
    emptyText = "Chưa có dữ liệu",
    loading = false,
    loadingText = "Đang tải dữ liệu...",
    error = null,
    onRetry,
    pageSize = 6,
    pageSizeOptions,
    pagination,
    sort,
    onSortChange,
    rowKey,
    rowClassName,
    onRowClick,
    selectable = false,
    selectionMode: controlledSelectionMode,
    onSelectionModeChange,
    selectedRowKeys = [],
    onSelectionChange,
    bulkActions = []
}) {
    const serverMode = mode === "server";
    const rows = useMemo(() => Array.isArray(data) ? data : [], [data]);
    const [localPage, setLocalPage] = useState(1);
    const [localPageSize, setLocalPageSize] = useState(pageSize);
    const [internalSelectionMode, setInternalSelectionMode] = useState(false);
    const [goToPage, setGoToPage] = useState("");
    const selectionMode = controlledSelectionMode ?? internalSelectionMode;

    const activePage = serverMode ? Math.max(1, pagination?.page || 1) : localPage;
    const activePageSize = serverMode
        ? pagination?.pageSize || SERVER_PAGE_SIZES[0]
        : localPageSize;
    const totalItems = serverMode ? Math.max(0, pagination?.totalItems || 0) : rows.length;
    const calculatedPages = Math.ceil(totalItems / activePageSize);
    const totalPages = serverMode
        ? Math.max(0, pagination?.totalPages ?? calculatedPages)
        : calculatedPages;
    const safePage = totalPages === 0 ? 1 : Math.min(activePage, totalPages);
    const visibleRows = useMemo(() => {
        if (serverMode) {
            return rows;
        }
        const start = (safePage - 1) * activePageSize;
        return rows.slice(start, start + activePageSize);
    }, [activePageSize, rows, safePage, serverMode]);

    const resolvedColumns = useMemo(
        () => buildColumns(columns, selectable && selectionMode),
        [columns, selectable, selectionMode]
    );
    const pageKeys = visibleRows.map((row, index) => getKey(row, index, rowKey));
    const selectedSet = useMemo(() => new Set(selectedRowKeys), [selectedRowKeys]);
    const selectedOnPage = pageKeys.filter((key) => selectedSet.has(key));
    const allPageSelected = pageKeys.length > 0 && selectedOnPage.length === pageKeys.length;
    const somePageSelected = selectedOnPage.length > 0 && !allPageSelected;

    function changePage(nextPage) {
        const upperBound = Math.max(1, totalPages);
        const normalized = Math.min(upperBound, Math.max(1, Number(nextPage) || 1));
        if (serverMode) {
            pagination?.onPageChange?.(normalized);
        } else {
            setLocalPage(normalized);
        }
    }

    function changePageSize(nextSize) {
        const normalized = Number(nextSize);
        if (serverMode) {
            pagination?.onPageSizeChange?.(normalized);
        } else {
            setLocalPageSize(normalized);
            setLocalPage(1);
        }
    }

    function submitGoToPage(event) {
        event.preventDefault();
        changePage(goToPage);
        setGoToPage("");
    }

    function changeSort(column) {
        if (!column.sortable || !onSortChange) return;
        const field = column.sortKey || column.key;
        let direction = "asc";
        if (sort?.field === field && sort.direction === "asc") direction = "desc";
        if (sort?.field === field && sort.direction === "desc") direction = null;
        onSortChange(direction ? { field, direction } : null);
    }

    function setSelectionMode(nextMode) {
        if (controlledSelectionMode === undefined) {
            setInternalSelectionMode(nextMode);
        }
        if (!nextMode) {
            onSelectionChange?.([]);
        }
        onSelectionModeChange?.(nextMode);
    }

    function toggleRow(key) {
        const next = selectedSet.has(key)
            ? selectedRowKeys.filter((value) => value !== key)
            : [...selectedRowKeys, key];
        onSelectionChange?.(next);
    }

    function toggleCurrentPage() {
        if (allPageSelected) {
            const pageKeySet = new Set(pageKeys);
            onSelectionChange?.(selectedRowKeys.filter((key) => !pageKeySet.has(key)));
            return;
        }
        onSelectionChange?.([...new Set([...selectedRowKeys, ...pageKeys])]);
    }

    const firstItem = totalItems === 0 ? 0 : (safePage - 1) * activePageSize + 1;
    const lastItem = totalItems === 0
        ? 0
        : Math.min(totalItems, firstItem + visibleRows.length - 1);
    const sizes = pageSizeOptions || (serverMode ? SERVER_PAGE_SIZES : null);

    return (
        <div className="data-table-shell">
            {selectable && (
                <div className="data-table-toolbar">
                    <button
                        type="button"
                        className={selectionMode ? "soft-button" : "ghost-button"}
                        onClick={() => setSelectionMode(!selectionMode)}
                        aria-pressed={selectionMode}
                    >
                        {selectionMode ? "Thoát chế độ chọn" : "Chọn nhiều"}
                    </button>
                    {selectionMode && (
                        <span>{selectedRowKeys.length} mục đã chọn</span>
                    )}
                </div>
            )}
            {error && rows.length > 0 && (
                <div className="table-inline-error" role="alert">
                    <span>{error}</span>
                    {onRetry && (
                        <button type="button" className="soft-button" onClick={onRetry}>
                            <RotateCcw size={16} /> Thử lại
                        </button>
                    )}
                </div>
            )}

            <div className="table-card">
                <div className="data-table-scroll">
                    <table className="data-table">
                        <colgroup>
                            {resolvedColumns.map((column) => (
                                <col key={column.key} style={column.colStyle} />
                            ))}
                        </colgroup>
                        <thead>
                        <tr>
                            {resolvedColumns.map((column) => (
                                <th
                                    key={column.key}
                                    className={column.className}
                                    data-column={column.key}
                                    style={column.cellStyle}
                                    aria-sort={getAriaSort(column, sort)}
                                >
                                    {column.isSelection ? (
                                        <IndeterminateCheckbox
                                            checked={allPageSelected}
                                            indeterminate={somePageSelected}
                                            onChange={toggleCurrentPage}
                                            label="Chọn toàn bộ trang hiện tại"
                                            disabled={pageKeys.length === 0}
                                        />
                                    ) : column.sortable ? (
                                        <button
                                            type="button"
                                            className="table-sort-button"
                                            onClick={() => changeSort(column)}
                                            disabled={!onSortChange}
                                        >
                                            <span>{column.title}</span>
                                            <SortIcon column={column} sort={sort} />
                                        </button>
                                    ) : column.title}
                                </th>
                            ))}
                        </tr>
                        </thead>

                        <tbody aria-busy={loading}>
                        {error && rows.length === 0 ? (
                            <StateRow columns={resolvedColumns.length} className="table-error-state">
                                <span>{error}</span>
                                {onRetry && (
                                    <button type="button" className="soft-button" onClick={onRetry}>
                                        <RotateCcw size={16} /> Thử lại
                                    </button>
                                )}
                            </StateRow>
                        ) : loading && rows.length === 0 ? (
                            <StateRow columns={resolvedColumns.length} className="table-loading-state">
                                <span className="table-spinner" aria-hidden="true" />
                                <span>{loadingText}</span>
                            </StateRow>
                        ) : visibleRows.length > 0 ? (
                            visibleRows.map((row, index) => {
                                const key = getKey(row, index, rowKey);
                                const selected = selectedSet.has(key);
                                return (
                                    <tr
                                        key={key}
                                        onClick={(event) => {
                                            if (!onRowClick || event.target.closest("button, input, select, textarea, a")) return;
                                            onRowClick(row);
                                        }}
                                        onKeyDown={(event) => {
                                            if (event.target.closest("button, input, select, textarea, a")) return;
                                            if (onRowClick && (event.key === "Enter" || event.key === " ")) {
                                                event.preventDefault();
                                                onRowClick(row);
                                            }
                                        }}
                                        tabIndex={onRowClick ? 0 : undefined}
                                        className={[
                                            onRowClick ? "is-clickable" : "",
                                            rowClassName?.(row) || "",
                                            selected ? "selected-row" : ""
                                        ].filter(Boolean).join(" ") || undefined}
                                    >
                                        {resolvedColumns.map((column) => (
                                            <td
                                                key={column.key}
                                                className={column.className}
                                                data-column={column.key}
                                                style={column.cellStyle}
                                                title={column.wrap ? undefined : displayTitle(row, column)}
                                            >
                                                {column.isSelection ? (
                                                    <input
                                                        type="checkbox"
                                                        checked={selected}
                                                        onChange={() => toggleRow(key)}
                                                        aria-label={`Chọn dòng ${key}`}
                                                    />
                                                ) : (
                                                    <span className="table-cell-content">
                                                        {column.render ? column.render(row) : row[column.key]}
                                                    </span>
                                                )}
                                            </td>
                                        ))}
                                    </tr>
                                );
                            })
                        ) : (
                            <StateRow columns={resolvedColumns.length} className="table-empty-state">
                                {emptyText}
                            </StateRow>
                        )}
                        </tbody>
                    </table>
                    {loading && rows.length > 0 && (
                        <div className="table-refresh-indicator" role="status">
                            <span className="table-spinner" aria-hidden="true" /> {loadingText}
                        </div>
                    )}
                </div>

                {(serverMode || totalItems > activePageSize) && (
                    <div className="table-pagination">
                        <div className="table-range">
                            {firstItem}–{lastItem} / {totalItems.toLocaleString("vi-VN")}
                        </div>
                        {sizes && (
                            <label className="table-page-size">
                                <span>Số dòng</span>
                                <select value={activePageSize} onChange={(event) => changePageSize(event.target.value)}>
                                    {sizes.map((size) => <option key={size} value={size}>{size}</option>)}
                                </select>
                            </label>
                        )}
                        <div className="table-page-controls" aria-label="Phân trang">
                            <PageButton label="Trang đầu" onClick={() => changePage(1)} disabled={safePage <= 1}>
                                <ChevronsLeft size={17} />
                            </PageButton>
                            <PageButton label="Trang trước" onClick={() => changePage(safePage - 1)} disabled={safePage <= 1}>
                                <ChevronLeft size={17} />
                            </PageButton>
                            {buildPageNumbers(safePage, totalPages).map((value, index) => value === "ellipsis" ? (
                                <span className="table-page-ellipsis" key={`ellipsis-${index}`}>…</span>
                            ) : (
                                <button
                                    type="button"
                                    className={`table-page-number${value === safePage ? " is-active" : ""}`}
                                    onClick={() => changePage(value)}
                                    aria-current={value === safePage ? "page" : undefined}
                                    key={value}
                                >
                                    {value}
                                </button>
                            ))}
                            <PageButton label="Trang sau" onClick={() => changePage(safePage + 1)} disabled={totalPages === 0 || safePage >= totalPages}>
                                <ChevronRight size={17} />
                            </PageButton>
                            <PageButton label="Trang cuối" onClick={() => changePage(totalPages)} disabled={totalPages === 0 || safePage >= totalPages}>
                                <ChevronsRight size={17} />
                            </PageButton>
                        </div>
                        <form className="table-go-to" onSubmit={submitGoToPage}>
                            <label htmlFor="table-go-to-page">Đi đến</label>
                            <input
                                id="table-go-to-page"
                                type="number"
                                min="1"
                                max={Math.max(1, totalPages)}
                                value={goToPage}
                                onChange={(event) => setGoToPage(event.target.value)}
                                placeholder={String(safePage)}
                                disabled={totalPages <= 1}
                            />
                        </form>
                    </div>
                )}
            </div>

            {selectionMode && selectedRowKeys.length > 0 && bulkActions.length > 0 && (
                <div className="table-bulk-bar" role="toolbar" aria-label="Thao tác với các mục đã chọn">
                    <strong>{selectedRowKeys.length} mục đã chọn</strong>
                    <div>
                        {bulkActions.map((action) => {
                            const Icon = action.icon;
                            return (
                                <button
                                    type="button"
                                    key={action.key}
                                    className={action.danger ? "soft-button danger-button" : "soft-button"}
                                    onClick={() => action.onClick?.(selectedRowKeys)}
                                    disabled={action.disabled}
                                >
                                    {Icon && <Icon size={16} />}
                                    {action.label}
                                </button>
                            );
                        })}
                    </div>
                </div>
            )}
        </div>
    );
}

function buildColumns(columns, includeSelection) {
    const source = includeSelection
        ? [{ key: "__selection", title: "", width: "56px", minWidth: "56px", align: "center", sticky: "left", isSelection: true }, ...columns]
        : columns;
    let leftOffset = 0;
    let rightOffset = 0;
    const rightOffsets = new Map();

    [...source].reverse().forEach((column) => {
        if (column.sticky === "right") {
            rightOffsets.set(column.key, rightOffset);
            rightOffset += pixelSize(column.width || column.minWidth);
        }
    });

    return source.map((column) => {
        const stickySide = column.sticky === true ? "left" : column.sticky;
        let stickyOffset;
        if (stickySide === "left") {
            stickyOffset = leftOffset;
            leftOffset += pixelSize(column.width || column.minWidth);
        } else if (stickySide === "right") {
            stickyOffset = rightOffsets.get(column.key) || 0;
        }
        const classes = [
            column.className,
            column.align ? `align-${column.align}` : "",
            column.wrap ? "is-wrap" : "",
            column.maxLines ? "has-max-lines" : "",
            stickySide ? `is-sticky sticky-${stickySide}` : "",
            isActionColumn(column) ? "action-cell" : "",
            column.isSelection ? "select-cell" : ""
        ].filter(Boolean).join(" ");
        return {
            ...column,
            className: classes,
            colStyle: {
                width: column.width,
                minWidth: column.minWidth || column.width
            },
            cellStyle: {
                width: column.width,
                minWidth: column.minWidth || column.width,
                ...(column.maxLines ? { "--table-max-lines": column.maxLines } : {}),
                ...(stickySide ? { [stickySide]: `${stickyOffset}px` } : {})
            }
        };
    });
}

function getKey(row, index, rowKey) {
    if (rowKey) return String(typeof rowKey === "function" ? rowKey(row) : row[rowKey]);
    return String(
        row.id || row.maChiTietMuon || row.maChiTietTra || row.maChiTietPhieuThu
        || row.maBinhLuan || row.maKhoanNo || row.maPhieuMuon || row.maPhieuTra
        || row.maPhieuThu || row.maCuonSach || row.maDauSach || row.maNhanVien
        || row.maDocGia || `row-${index}`
    );
}

function pixelSize(value) {
    const parsed = Number.parseFloat(value);
    return Number.isFinite(parsed) ? parsed : 140;
}

function isActionColumn(column) {
    return column.key === "actions" || column.key === "action";
}

function getAriaSort(column, sort) {
    const field = column.sortKey || column.key;
    if (!column.sortable || sort?.field !== field) return undefined;
    return sort.direction === "desc" ? "descending" : "ascending";
}

function SortIcon({ column, sort }) {
    const field = column.sortKey || column.key;
    if (sort?.field !== field) return <ArrowUpDown size={15} aria-hidden="true" />;
    return sort.direction === "desc"
        ? <ArrowDown size={15} aria-hidden="true" />
        : <ArrowUp size={15} aria-hidden="true" />;
}

function IndeterminateCheckbox({ checked, indeterminate, onChange, label, disabled }) {
    const ref = useRef(null);
    useEffect(() => {
        if (ref.current) ref.current.indeterminate = indeterminate;
    }, [indeterminate]);
    return (
        <input
            ref={ref}
            type="checkbox"
            checked={checked}
            onChange={onChange}
            aria-label={label}
            disabled={disabled}
        />
    );
}

function StateRow({ columns, className, children }) {
    return (
        <tr>
            <td className={`table-state-cell ${className}`} colSpan={columns}>
                <div>{children}</div>
            </td>
        </tr>
    );
}

function PageButton({ label, onClick, disabled, children }) {
    return (
        <button type="button" className="icon-button" onClick={onClick} disabled={disabled} title={label} aria-label={label}>
            {children}
        </button>
    );
}

function buildPageNumbers(page, totalPages) {
    if (totalPages <= 7) return Array.from({ length: totalPages }, (_, index) => index + 1);
    const values = new Set([1, totalPages, page - 1, page, page + 1]);
    const sorted = [...values].filter((value) => value >= 1 && value <= totalPages).sort((a, b) => a - b);
    const result = [];
    sorted.forEach((value, index) => {
        if (index > 0 && value - sorted[index - 1] > 1) result.push("ellipsis");
        result.push(value);
    });
    return result;
}

function displayTitle(row, column) {
    if (column.render) return undefined;
    const value = row[column.key];
    return value === null || value === undefined ? undefined : String(value);
}
