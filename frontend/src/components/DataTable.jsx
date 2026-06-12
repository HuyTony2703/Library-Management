import { ChevronLeft, ChevronRight } from "lucide-react";
import { useEffect, useMemo, useState } from "react";

export default function DataTable({
    columns,
    data,
    emptyText = "Chưa có dữ liệu",
    pageSize = 6,
    rowKey,
    rowClassName
}) {
    const [page, setPage] = useState(0);
    const rows = Array.isArray(data) ? data : [];
    const totalPages = Math.max(1, Math.ceil(rows.length / pageSize));
    const safePage = Math.min(page, totalPages - 1);

    useEffect(() => {
        setPage(0);
    }, [rows.length, pageSize]);

    const visibleRows = useMemo(() => {
        const start = safePage * pageSize;
        return rows.slice(start, start + pageSize);
    }, [rows, safePage, pageSize]);

    function getKey(row, index) {
        const absoluteIndex = safePage * pageSize + index;
        const id = rowKey
            ? rowKey(row)
            : row.id ||
                row.maChiTietMuon ||
                row.maChiTietTra ||
                row.maChiTietPhieuThu ||
                row.maBinhLuan ||
                row.maKhoanNo ||
                row.maPhieuMuon ||
                row.maPhieuTra ||
                row.maPhieuThu ||
                row.maCuonSach ||
                row.maDauSach ||
                row.maNhanVien ||
                row.maDocGia ||
                "row";

        return `${id}-${absoluteIndex}`;
    }

    function isSelectColumn(column) {
        return column.key === "select" || column.key === "chon";
    }

    function isActionColumn(column) {
        return column.key === "actions" || column.key === "action";
    }

    function getColumnClassName(column) {
        return [
            column.className,
            column.align ? `align-${column.align}` : "",
            isSelectColumn(column) ? "select-cell" : "",
            isActionColumn(column) ? "action-cell" : ""
        ].filter(Boolean).join(" ");
    }

    return (
        <div className="table-card">
            <table className="data-table">
                <colgroup>
                    {columns.map((column) => (
                        <col
                            key={column.key}
                            className={[
                                isSelectColumn(column) ? "select-col" : "",
                                isActionColumn(column) ? "action-col" : ""
                            ].filter(Boolean).join(" ") || undefined}
                            style={column.width ? { width: column.width } : undefined}
                        />
                    ))}
                </colgroup>
                <thead>
                <tr>
                    {columns.map((column) => (
                        <th
                            key={column.key}
                            className={getColumnClassName(column)}
                            data-column={column.key}
                        >
                            {column.title}
                        </th>
                    ))}
                </tr>
                </thead>

                <tbody>
                {rows.length > 0 ? (
                    visibleRows.map((row, index) => (
                        <tr key={getKey(row, index)} className={rowClassName?.(row) || undefined}>
                            {columns.map((column) => (
                                <td
                                    key={column.key}
                                    className={getColumnClassName(column)}
                                    data-column={column.key}
                                >
                                    {column.render ? column.render(row) : row[column.key]}
                                </td>
                            ))}
                        </tr>
                    ))
                ) : (
                    <tr>
                        <td className="empty-cell" colSpan={columns.length}>
                            {emptyText}
                        </td>
                    </tr>
                )}
                </tbody>
            </table>

            {rows.length > pageSize && (
                <div className="table-pagination">
                    <button
                        type="button"
                        className="icon-button"
                        onClick={() => setPage((value) => Math.max(0, value - 1))}
                        disabled={safePage === 0}
                        title="Trang trước"
                    >
                        <ChevronLeft size={18} />
                    </button>

                    <span>
                        {safePage + 1}/{totalPages} · {rows.length} mục
                    </span>

                    <button
                        type="button"
                        className="icon-button"
                        onClick={() => setPage((value) => Math.min(totalPages - 1, value + 1))}
                        disabled={safePage >= totalPages - 1}
                        title="Trang sau"
                    >
                        <ChevronRight size={18} />
                    </button>
                </div>
            )}
        </div>
    );
}
