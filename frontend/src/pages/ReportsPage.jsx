import { useState } from "react";
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { libraryApi } from "../api/libraryApi";
import DataTable from "../components/DataTable";
import PageHeader from "../components/PageHeader";
import { useToast } from "../components/ToastProvider";
import { useAuth } from "../context/AuthContext";

export default function ReportsPage() {
    const toast = useToast();
    const { user } = useAuth();
    const now = new Date();
    const isAdmin = user?.tenVaiTro === "QUAN_TRI_VIEN" || user?.maVaiTro === "VT_ADMIN";

    const [month, setMonth] = useState(now.getMonth() + 1);
    const [year, setYear] = useState(now.getFullYear());
    const [borrow, setBorrow] = useState([]);
    const [late, setLate] = useState([]);

    async function load() {
        if (!isAdmin) {
            toast.error("Chỉ admin được xem báo cáo tổng hợp");
            return;
        }

        try {
            const [borrowData, lateData] = await Promise.all([
                libraryApi.borrowByCategoryReport(month, year),
                libraryApi.lateReturnsReport(month, year)
            ]);

            setBorrow(Array.isArray(borrowData) ? borrowData : []);
            setLate(Array.isArray(lateData) ? lateData : []);
            toast.success("Đã tải báo cáo");
        } catch (err) {
            toast.error(err.message);
        }
    }

    const chartData = borrow.map((item) => ({
        name: item.tenTheLoai || item.maTheLoai,
        value: Number(item.soLuotMuon || 0)
    }));

    if (!isAdmin) {
        return (
            <div>
                <PageHeader
                    eyebrow="Reports"
                    title="Báo cáo thư viện"
                    description="Báo cáo tổng hợp chỉ dành cho admin."
                />

                <div className="panel">
                    <div className="panel-title">
                        <h2>Không có quyền truy cập</h2>
                    </div>
                    <p>Tài khoản thủ thư không được gọi API báo cáo admin-only.</p>
                </div>
            </div>
        );
    }

    return (
        <div>
            <PageHeader
                eyebrow="Reports"
                title="Báo cáo thư viện"
                description="Thống kê mượn sách theo thể loại và danh sách trả trễ trong tháng."
            />

            <div className="panel compact-form">
                <label>Tháng</label>
                <input type="number" min="1" max="12" value={month} onChange={(event) => setMonth(event.target.value)} />

                <label>Năm</label>
                <input type="number" value={year} onChange={(event) => setYear(event.target.value)} />

                <button className="primary-button" onClick={load}>Xem báo cáo</button>
            </div>

            <div className="grid-2">
                <div className="panel">
                    <div className="panel-title">
                        <h2>Mượn theo thể loại</h2>
                    </div>

                    <div className="chart-box">
                        <ResponsiveContainer width="100%" height={280}>
                            <BarChart data={chartData}>
                                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                                <XAxis dataKey="name" />
                                <YAxis allowDecimals={false} />
                                <Tooltip />
                                <Bar dataKey="value" radius={[8, 8, 0, 0]} />
                            </BarChart>
                        </ResponsiveContainer>
                    </div>
                </div>

                <div className="panel">
                    <div className="panel-title">
                        <h2>Sách trả trễ</h2>
                    </div>

                    <DataTable
                        data={late}
                        columns={[
                            { key: "maCuonSach", title: "Mã cuốn" },
                            { key: "tenDauSach", title: "Tên sách" },
                            { key: "hoTenDocGia", title: "Độc giả" },
                            { key: "soNgayTre", title: "Ngày trễ" },
                            {
                                key: "tienPhatTre",
                                title: "Tiền phạt",
                                render: (row) => `${Number(row.tienPhatTre || 0).toLocaleString()}đ`
                            }
                        ]}
                    />
                </div>
            </div>
        </div>
    );
}
