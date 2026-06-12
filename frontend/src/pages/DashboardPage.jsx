import { AlertTriangle, BookOpen, CreditCard, UsersRound } from "lucide-react";
import { useEffect, useState } from "react";
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import DataTable from "../components/DataTable";
import PageHeader from "../components/PageHeader";
import StatCard from "../components/StatCard";
import { libraryApi } from "../api/libraryApi";
import { useToast } from "../components/ToastProvider";
import { useAuth } from "../context/AuthContext";

export default function DashboardPage() {
    const toast = useToast();
    const { user } = useAuth();
    const isAdmin = user?.tenVaiTro === "QUAN_TRI_VIEN" || user?.maVaiTro === "VT_ADMIN";

    const [books, setBooks] = useState([]);
    const [copies, setCopies] = useState([]);
    const [readers, setReaders] = useState([]);
    const [debts, setDebts] = useState([]);
    const [currentLoans, setCurrentLoans] = useState([]);

    useEffect(() => {
        async function load() {
            try {
                const [booksData, copiesData, readersData] = await Promise.all([
                    libraryApi.books(),
                    libraryApi.bookCopies(),
                    libraryApi.readers()
                ]);

                setBooks(Array.isArray(booksData) ? booksData : []);
                setCopies(Array.isArray(copiesData) ? copiesData : []);
                setReaders(Array.isArray(readersData) ? readersData : []);

                if (isAdmin) {
                    const [debtData, loansData] = await Promise.all([
                        libraryApi.debtReport(),
                        libraryApi.currentLoansReport()
                    ]);

                    setDebts(Array.isArray(debtData) ? debtData : []);
                    setCurrentLoans(Array.isArray(loansData) ? loansData : []);
                } else {
                    setDebts([]);
                    setCurrentLoans([]);
                }
            } catch (err) {
                toast.error(err.message);
            }
        }

        load();
    }, [isAdmin]);

    const chartData = currentLoans.slice(0, 8).map((item) => ({
        name: item.hoTen || item.maDocGia,
        value: Number(item.soSachDangMuon || 0)
    }));

    const totalDebt = debts.reduce((sum, item) => {
        return sum + Number(item.tongNoConLai || item.tongNo || 0);
    }, 0);

    return (
        <div>
            <PageHeader
                eyebrow="Dashboard"
                title="Tổng quan hệ thống"
                description="Theo dõi nhanh tình trạng sách, độc giả, mượn trả và công nợ."
            />

            <div className="stat-grid">
                <StatCard icon={BookOpen} label="Đầu sách" value={books.length} hint="Danh mục hiện có" />
                <StatCard icon={BookOpen} label="Cuốn sách" value={copies.length} hint="Bản vật lý trong kho" />
                <StatCard icon={UsersRound} label="Độc giả" value={readers.length} hint="Tài khoản thư viện" />
                <StatCard
                    icon={CreditCard}
                    label={isAdmin ? "Tổng nợ" : "Thu tiền"}
                    value={isAdmin ? `${totalDebt.toLocaleString()}đ` : "Staff"}
                    hint={isAdmin ? "Còn phải thu" : "Dùng trang Thu tiền để xem nợ độc giả"}
                />
            </div>

            {isAdmin ? (
                <div className="grid-2">
                    <div className="panel">
                        <div className="panel-title">
                            <h2>Sách đang mượn theo độc giả</h2>
                            <span>Top 8</span>
                        </div>

                        <div className="chart-box">
                            <ResponsiveContainer width="100%" height={260}>
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
                            <h2>Cảnh báo công nợ</h2>
                            <AlertTriangle size={20} />
                        </div>

                        <DataTable
                            data={debts.slice(0, 6)}
                            columns={[
                                { key: "maDocGia", title: "Mã ĐG" },
                                { key: "hoTen", title: "Họ tên" },
                                {
                                    key: "tongNoConLai",
                                    title: "Nợ còn lại",
                                    render: (row) => `${Number(row.tongNoConLai || row.tongNo || 0).toLocaleString()}đ`
                                }
                            ]}
                        />
                    </div>
                </div>
            ) : (
                <div className="panel">
                    <div className="panel-title">
                        <h2>Tổng quan thủ thư</h2>
                    </div>

                    <p>
                        Tài khoản thủ thư có thể quản lý sách, độc giả, mượn trả và thu tiền.
                        Các báo cáo tổng hợp và công nợ toàn hệ thống chỉ dành cho admin.
                    </p>
                </div>
            )}
        </div>
    );
}
