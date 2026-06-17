import { useEffect, useMemo, useState } from "react";
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { adminApi } from "../../api/adminApi";
import { libraryApi } from "../../api/libraryApi";
import PageHeader from "../../components/PageHeader";
import DataTable from "../../components/DataTable";
import StatusBadge from "../../components/StatusBadge";
import { useToast } from "../../components/ToastProvider";
import { chartAxisTick, chartTheme, chartTooltipItemStyle, chartTooltipLabelStyle, chartTooltipStyle } from "../../utils/chartTheme";
import { formatDateTime, formatMoney } from "../../utils/displayUtils";

export default function AdminReportsPage() {
    const toast = useToast();
    const now = new Date();

    const [reportMonth, setReportMonth] = useState(() => `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`);
    const selectedReportPeriod = useMemo(() => parseReportMonth(reportMonth), [reportMonth]);

    const [overview, setOverview] = useState(null);
    const [debts, setDebts] = useState([]);
    const [currentLoans, setCurrentLoans] = useState([]);
    const [borrowByCategory, setBorrowByCategory] = useState([]);
    const [lateReturns, setLateReturns] = useState([]);
    const [payments, setPayments] = useState([]);
    const [selectedDebtReaders, setSelectedDebtReaders] = useState([]);
    const [loading, setLoading] = useState(false);

    const totalBorrow = useMemo(() => {
        return borrowByCategory.reduce((sum, item) => sum + Number(item.soLuotMuon || 0), 0);
    }, [borrowByCategory]);

    async function loadReports(period = selectedReportPeriod) {
        if (!period) {
            toast.error("Tháng báo cáo không hợp lệ.");
            return;
        }

        setLoading(true);

        try {
            const { month, year } = period;

            const results = await Promise.allSettled([
                loadOverviewWithFallback(month, year),
                loadWithFallback(() => adminApi.getDebtReport(), () => libraryApi.debtReport()),
                loadWithFallback(() => adminApi.getCurrentLoansReport(), () => libraryApi.currentLoansReport()),
                loadWithFallback(() => adminApi.getBorrowByCategoryReport(month, year), () => libraryApi.borrowByCategoryReport(month, year)),
                loadWithFallback(() => adminApi.getLateReturnsReport(month, year), () => libraryApi.lateReturnsReport(month, year)),
                loadWithFallback(() => adminApi.getPaymentsReport(month, year), () => [])
            ]);

            applyReportResult(results[0], setOverview);
            applyArrayReportResult(results[1], setDebts);
            applyArrayReportResult(results[2], (items) => setCurrentLoans(normalizeCurrentLoans(items)));
            applyArrayReportResult(results[3], setBorrowByCategory);
            applyArrayReportResult(results[4], setLateReturns);
            applyArrayReportResult(results[5], setPayments);

            const failedCount = results.filter((result) => result.status === "rejected").length;
            if (failedCount > 0) {
                toast.error(`Có ${failedCount} phần báo cáo tải lỗi. Các phần còn lại vẫn được hiển thị.`);
            }
        } catch (err) {
            toast.error(err.message || "Không tải được báo cáo");
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        if (selectedReportPeriod) {
            loadReports(selectedReportPeriod);
        }
    }, [selectedReportPeriod]);

    useEffect(() => {
        setSelectedDebtReaders((prev) =>
            prev.filter((readerId) => debts.some((row) => getDebtReaderId(row) === readerId))
        );
    }, [debts]);

    function toggleDebtReader(row) {
        const readerId = getDebtReaderId(row);

        if (!readerId) {
            return;
        }

        setSelectedDebtReaders((prev) =>
            prev.includes(readerId)
                ? prev.filter((item) => item !== readerId)
                : [...prev, readerId]
        );
    }

    return (
        <div>
            <PageHeader
                eyebrow="Admin"
                title="Báo cáo hệ thống"
                description="Thống kê mượn sách, trả trễ, khoản nợ và phiếu thu theo tháng."
            />

            <div className="panel compact-form report-filter-panel">
                <label>Tháng báo cáo</label>
                <input
                    type="month"
                    min="2000-01"
                    max="2100-12"
                    value={reportMonth}
                    onChange={(event) => setReportMonth(event.target.value)}
                    disabled={loading}
                />
                <span className="form-note">Báo cáo tự cập nhật khi đổi tháng.</span>
            </div>

            <div className="report-card-grid">
                <ReportCard label="Tổng cuốn sách" value={overview?.totalBookCopies ?? 0} />
                <ReportCard label="Sách sẵn có" value={overview?.availableBookCopies ?? 0} />
                <ReportCard label="Sách đang mượn" value={overview?.borrowedBookCopies ?? 0} />
                <ReportCard label="Độc giả hoạt động" value={overview?.activeReaders ?? 0} />
                <ReportCard label="Lượt mượn trong tháng" value={overview?.loansThisMonth ?? 0} />
                <ReportCard label="Trả trễ trong tháng" value={overview?.lateReturnsThisMonth ?? 0} />
                <ReportCard label="Tổng nợ còn lại" value={formatMoney(overview?.totalDebt)} />
                <ReportCard label="Phiếu thu trong tháng" value={formatMoney(overview?.paymentsThisMonth)} />
            </div>

            <div className="panel">
                <div className="panel-title">
                    <h2>Mượn sách theo thể loại</h2>
                    <span>Tổng {totalBorrow} lượt</span>
                </div>

                <div className="chart-box">
                    <ResponsiveContainer width="100%" height={280}>
                        <BarChart data={borrowByCategory}>
                            <CartesianGrid stroke={chartTheme.grid} strokeDasharray="3 3" vertical={false} />
                            <XAxis dataKey="tenTheLoai" tick={chartAxisTick} axisLine={{ stroke: chartTheme.grid }} tickLine={{ stroke: chartTheme.grid }} />
                            <YAxis allowDecimals={false} tick={chartAxisTick} axisLine={{ stroke: chartTheme.grid }} tickLine={{ stroke: chartTheme.grid }} />
                            <Tooltip
                                contentStyle={chartTooltipStyle}
                                labelStyle={chartTooltipLabelStyle}
                                itemStyle={chartTooltipItemStyle}
                                cursor={{ fill: chartTheme.hover }}
                            />
                            <Bar dataKey="soLuotMuon" name="Số lượt mượn" fill={chartTheme.bar} radius={[8, 8, 0, 0]} />
                        </BarChart>
                    </ResponsiveContainer>
                </div>

                <DataTable
                    data={borrowByCategory}
                    columns={[
                        { key: "maTheLoai", title: "Mã thể loại" },
                        { key: "tenTheLoai", title: "Tên thể loại" },
                        { key: "soLuotMuon", title: "Số lượt mượn" },
                        { key: "tiLePhanTram", title: "Tỉ lệ", render: (row) => `${Number(row.tiLePhanTram || 0)}%` }
                    ]}
                />
            </div>

            <div className="panel">
                <div className="panel-title">
                    <h2>Sách trả trễ trong tháng</h2>
                    <span>{lateReturns.length} dòng</span>
                </div>

                <DataTable
                    data={lateReturns}
                    columns={[
                        { key: "maCuonSach", title: "Mã cuốn" },
                        { key: "tenDauSach", title: "Tên sách" },
                        { key: "maDocGia", title: "Mã độc giả" },
                        { key: "hoTenDocGia", title: "Độc giả" },
                        { key: "hanTra", title: "Hạn trả", render: (row) => formatDateTime(row.hanTra) },
                        { key: "ngayTraThucTe", title: "Ngày trả", render: (row) => formatDateTime(row.ngayTraThucTe) },
                        { key: "soNgayTre", title: "Số ngày trễ" },
                        { key: "tienPhatTre", title: "Tiền phạt", render: (row) => formatMoney(row.tienPhatTre) }
                    ]}
                />
            </div>

            <div className="panel">
                <div className="panel-title">
                    <h2>Độc giả còn nợ</h2>
                    <span>{debts.length} độc giả</span>
                </div>

                <DataTable
                    data={debts}
                    rowClassName={(row) => selectedDebtReaders.includes(getDebtReaderId(row)) ? "selected-row" : ""}
                    columns={[
                        {
                            key: "chon",
                            title: "Chọn",
                            width: "76px",
                            render: (row) => {
                                const readerId = getDebtReaderId(row);

                                return (
                                    <input
                                        className="table-checkbox"
                                        type="checkbox"
                                        checked={selectedDebtReaders.includes(readerId)}
                                        onChange={() => toggleDebtReader(row)}
                                    />
                                );
                            }
                        },
                        { key: "maDocGia", title: "Mã độc giả" },
                        { key: "hoTen", title: "Họ tên" },
                        { key: "tongNoConLai", title: "Tổng nợ còn lại", render: (row) => formatMoney(row.tongNoConLai) }
                    ]}
                />
            </div>

            <div className="panel">
                <div className="panel-title">
                    <h2>Sách đang mượn</h2>
                    <span>{currentLoans.length} cuốn</span>
                </div>

                <DataTable
                    data={currentLoans}
                    columns={[
                        { key: "maChiTietMuon", title: "Mã CT mượn" },
                        { key: "maPhieuMuon", title: "Phiếu mượn" },
                        { key: "maDocGia", title: "Độc giả" },
                        { key: "hoTenDocGia", title: "Họ tên" },
                        { key: "maCuonSach", title: "Mã cuốn" },
                        { key: "tenDauSach", title: "Tên sách" },
                        { key: "hanTra", title: "Hạn trả", render: (row) => formatDateTime(row.hanTra) },
                        {
                            key: "soNgayConLai",
                            title: "Còn lại",
                            render: (row) => {
                                const value = Number(row.soNgayConLai || 0);
                                return value < 0 ? <span className="status-badge status-bad">Quá hạn {Math.abs(value)} ngày</span> : `${value} ngày`;
                            }
                        }
                    ]}
                />
            </div>

            <div className="panel">
                <div className="panel-title">
                    <h2>Phiếu thu trong tháng</h2>
                    <span>{payments.length} phiếu</span>
                </div>

                <DataTable
                    data={payments}
                    columns={[
                        { key: "maPhieuThu", title: "Mã phiếu thu" },
                        { key: "maDocGia", title: "Mã độc giả" },
                        { key: "hoTenDocGia", title: "Họ tên" },
                        { key: "tenPhuongThuc", title: "Phương thức" },
                        { key: "loaiThu", title: "Loại thu" },
                        { key: "soTienThu", title: "Số tiền", render: (row) => formatMoney(row.soTienThu) },
                        { key: "ngayThu", title: "Ngày thu", render: (row) => formatDateTime(row.ngayThu) },
                        { key: "trangThai", title: "Trạng thái", render: (row) => <StatusBadge value={row.trangThai} /> }
                    ]}
                />
            </div>
        </div>
    );
}

function parseReportMonth(value) {
    const match = /^(\d{4})-(\d{2})$/.exec(value || "");
    if (!match) {
        return null;
    }

    const year = Number(match[1]);
    const month = Number(match[2]);

    if (year < 2000 || year > 2100 || month < 1 || month > 12) {
        return null;
    }

    return { month, year };
}

function applyReportResult(result, setter) {
    if (result.status === "fulfilled") {
        setter(result.value);
    }
}

function applyArrayReportResult(result, setter) {
    if (result.status === "fulfilled") {
        setter(Array.isArray(result.value) ? result.value : []);
    }
}

async function loadWithFallback(primary, fallback) {
    try {
        return await primary();
    } catch {
        return fallback();
    }
}

async function loadOverviewWithFallback(month, year) {
    try {
        return await adminApi.getReportOverview(month, year);
    } catch {
        const [bookCopiesResult, readersResult, debtsResult, borrowResult, lateResult] = await Promise.allSettled([
            libraryApi.bookCopies(),
            libraryApi.readers(),
            libraryApi.debtReport(),
            libraryApi.borrowByCategoryReport(month, year),
            libraryApi.lateReturnsReport(month, year)
        ]);

        const bookCopies = getSettledArray(bookCopiesResult);
        const readers = getSettledArray(readersResult);
        const debts = getSettledArray(debtsResult);
        const borrowByCategory = getSettledArray(borrowResult);
        const lateReturns = getSettledArray(lateResult);

        return {
            totalBookCopies: bookCopies.length,
            availableBookCopies: bookCopies.filter((item) => item.maTrangThai === "TT_SANCO").length,
            borrowedBookCopies: bookCopies.filter((item) => item.maTrangThai === "TT_DANGMUON").length,
            activeReaders: readers.filter((item) => item.trangThai === "Hoạt động").length,
            loansThisMonth: borrowByCategory.reduce((sum, item) => sum + Number(item.soLuotMuon || 0), 0),
            lateReturnsThisMonth: lateReturns.length,
            totalDebt: debts.reduce((sum, item) => sum + Number(item.tongNoConLai || 0), 0),
            paymentsThisMonth: 0
        };
    }
}

function getSettledArray(result) {
    return result.status === "fulfilled" && Array.isArray(result.value) ? result.value : [];
}

function normalizeCurrentLoans(items) {
    return items.map((item) => {
        if (item.maChiTietMuon) {
            return item;
        }

        return {
            maChiTietMuon: "",
            maPhieuMuon: "",
            maDocGia: item.maDocGia,
            hoTenDocGia: item.hoTen,
            maCuonSach: "",
            tenDauSach: `${Number(item.soSachDangMuon || 0)} sách đang mượn`,
            hanTra: null,
            soNgayConLai: null
        };
    });
}

function getDebtReaderId(row) {
    return row?.maDocGia ?? row?.MaDocGia ?? row?.id ?? "";
}

function ReportCard({ label, value }) {
    return (
        <div className="report-card">
            <span>{label}</span>
            <strong>{value}</strong>
        </div>
    );
}
