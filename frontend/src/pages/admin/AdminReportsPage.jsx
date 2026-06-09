import { useEffect, useMemo, useState } from "react";
import { RefreshCcw } from "lucide-react";
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { adminApi } from "../../api/adminApi";
import PageHeader from "../../components/PageHeader";
import DataTable from "../../components/DataTable";
import StatusBadge from "../../components/StatusBadge";
import { useToast } from "../../components/ToastProvider";
import { formatDateTime, formatMoney } from "../../utils/displayUtils";

export default function AdminReportsPage() {
    const toast = useToast();
    const now = new Date();

    const [filter, setFilter] = useState({
        month: now.getMonth() + 1,
        year: now.getFullYear()
    });

    const [overview, setOverview] = useState(null);
    const [debts, setDebts] = useState([]);
    const [currentLoans, setCurrentLoans] = useState([]);
    const [borrowByCategory, setBorrowByCategory] = useState([]);
    const [lateReturns, setLateReturns] = useState([]);
    const [payments, setPayments] = useState([]);
    const [loading, setLoading] = useState(false);

    const totalBorrow = useMemo(() => {
        return borrowByCategory.reduce((sum, item) => sum + Number(item.soLuotMuon || 0), 0);
    }, [borrowByCategory]);

    function updateFilter(field, value) {
        setFilter((prev) => ({ ...prev, [field]: Number(value) }));
    }

    async function loadReports() {
        if (filter.month < 1 || filter.month > 12) {
            toast.error("Tháng báo cáo phải nằm trong khoảng 1 đến 12");
            return;
        }

        if (filter.year < 2000 || filter.year > 2100) {
            toast.error("Năm báo cáo phải nằm trong khoảng 2000 đến 2100");
            return;
        }

        setLoading(true);

        try {
            const month = filter.month;
            const year = filter.year;

            const results = await Promise.allSettled([
                adminApi.getReportOverview(month, year),
                adminApi.getDebtReport(),
                adminApi.getCurrentLoansReport(),
                adminApi.getBorrowByCategoryReport(month, year),
                adminApi.getLateReturnsReport(month, year),
                adminApi.getPaymentsReport(month, year)
            ]);

            applyReportResult(results[0], setOverview);
            applyArrayReportResult(results[1], setDebts);
            applyArrayReportResult(results[2], setCurrentLoans);
            applyArrayReportResult(results[3], setBorrowByCategory);
            applyArrayReportResult(results[4], setLateReturns);
            applyArrayReportResult(results[5], setPayments);

            const failedCount = results.filter((result) => result.status === "rejected").length;
            if (failedCount > 0) {
                toast.error(`Có ${failedCount} phần báo cáo tải lỗi. Các phần còn lại vẫn được hiển thị.`);
            } else {
                toast.success("Đã tải báo cáo");
            }
        } catch (err) {
            toast.error(err.message || "Không tải được báo cáo");
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        loadReports();
    }, []);

    return (
        <div>
            <PageHeader
                eyebrow="Admin"
                title="Báo cáo hệ thống"
                description="Thống kê mượn sách, trả trễ, khoản nợ và phiếu thu theo tháng."
                right={
                    <button className="soft-button" onClick={loadReports} disabled={loading}>
                        <RefreshCcw size={17} />
                        Tải báo cáo
                    </button>
                }
            />

            <div className="panel compact-form">
                <label>Tháng</label>
                <input type="number" min="1" max="12" value={filter.month} onChange={(event) => updateFilter("month", event.target.value)} />

                <label>Năm</label>
                <input type="number" min="2000" max="2100" value={filter.year} onChange={(event) => updateFilter("year", event.target.value)} />

                <button className="primary-button" onClick={loadReports} disabled={loading}>
                    Xem báo cáo
                </button>
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
                            <CartesianGrid strokeDasharray="3 3" />
                            <XAxis dataKey="tenTheLoai" />
                            <YAxis allowDecimals={false} />
                            <Tooltip />
                            <Bar dataKey="soLuotMuon" name="Số lượt mượn" fill="#2563eb" />
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
                    columns={[
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

function ReportCard({ label, value }) {
    return (
        <div className="report-card">
            <span>{label}</span>
            <strong>{value}</strong>
        </div>
    );
}
