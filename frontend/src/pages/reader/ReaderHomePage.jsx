import { Bell, BookOpen, BookmarkCheck, Calculator, CreditCard, Search } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { readerApi } from "../../api/readerApi";
import RandomBookSection from "../../components/reader/RandomBookSection";

function safeCount(value) {
    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : 0;
}

function getDueDays(hanTra) {
    if (!hanTra) return null;
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const due = new Date(hanTra);
    due.setHours(0, 0, 0, 0);
    return Math.ceil((due.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));
}

export default function ReaderHomePage() {
    const [profile, setProfile] = useState(null);
    const [loans, setLoans] = useState([]);
    const [reservations, setReservations] = useState([]);
    const [membership, setMembership] = useState(null);
    const [unreadCount, setUnreadCount] = useState(0);
    const [error, setError] = useState("");

    useEffect(() => {
        async function loadDashboard() {
            try {
                const [profileData, loanData, reservationData, membershipData, unreadData] = await Promise.allSettled([
                    readerApi.me(),
                    readerApi.currentLoans(),
                    readerApi.reservations(),
                    readerApi.getCurrentMembership(),
                    readerApi.getUnreadNotificationCount()
                ]);

                if (profileData.status === "fulfilled") setProfile(profileData.value);
                if (loanData.status === "fulfilled") setLoans(Array.isArray(loanData.value) ? loanData.value : []);
                if (reservationData.status === "fulfilled") setReservations(Array.isArray(reservationData.value) ? reservationData.value : []);
                if (membershipData.status === "fulfilled") setMembership(membershipData.value);
                if (unreadData.status === "fulfilled") {
                    const unreadValue = unreadData.value;
                    setUnreadCount(safeCount(unreadValue?.unreadCount ?? unreadValue?.count ?? unreadValue?.soLuong ?? unreadValue));
                }

                if (profileData.status === "rejected") {
                    throw profileData.reason;
                }
            } catch (err) {
                setError(err.message || "Không tải được thông tin độc giả.");
            }
        }

        loadDashboard();
    }, []);

    const dueSoonLoans = useMemo(() => {
        return loans
            .map((loan) => ({ ...loan, daysLeft: getDueDays(loan.hanTra) }))
            .filter((loan) => loan.daysLeft !== null && loan.daysLeft <= 3)
            .sort((a, b) => a.daysLeft - b.daysLeft);
    }, [loans]);

    const activeReservations = reservations.filter((item) =>
        ["Đang chờ", "Đã giữ chỗ"].includes(item.trangThai)
    );

    if (error) {
        return (
            <div>
                <div className="reader-home-header">
                    <small>CỔNG ĐỘC GIẢ</small>
                    <h1>Trang chủ độc giả</h1>
                    <p>Không tải được thông tin tài khoản độc giả.</p>
                </div>

                <div className="reader-error">{error}</div>
            </div>
        );
    }

    return (
        <div>
            <div className="reader-home-header reader-dashboard-header">
                <small>CỔNG ĐỘC GIẢ</small>
                <h1>Xin chào, {profile?.hoTen || "độc giả"}</h1>
                <p>
                    {dueSoonLoans[0]
                        ? `Bạn có sách "${dueSoonLoans[0].tenDauSach}" ${dueSoonLoans[0].daysLeft < 0 ? `quá hạn ${Math.abs(dueSoonLoans[0].daysLeft)} ngày` : `còn ${dueSoonLoans[0].daysLeft} ngày đến hạn`}.`
                        : "Theo dõi sách đang mượn, đặt trước, thông báo và gói độc giả của bạn."}
                </p>
                {dueSoonLoans[0] && (
                    <Link className="reader-primary-button" to="/reader/loans">
                        Gia hạn sách
                    </Link>
                )}
            </div>

            <section className="reader-overview-grid">
                <DashboardCard label="Đang mượn" value={`${loans.length} sách`} to="/reader/loans" />
                <DashboardCard label="Sắp đến hạn" value={`${dueSoonLoans.length} sách`} tone={dueSoonLoans.length ? "warn" : "good"} to="/reader/loans" />
                <DashboardCard label="Đặt trước" value={activeReservations.length} to="/reader/reservations" />
                <DashboardCard
                    label={membership?.tenGoi || "Gói độc giả"}
                    value={membership?.ngayKetThuc ? `Đến ${formatDate(membership.ngayKetThuc)}` : "Chưa có gói"}
                    to="/reader/membership"
                />
                <DashboardCard label="Thông báo chưa đọc" value={unreadCount} tone={unreadCount ? "warn" : "good"} to="/reader/notifications" />
            </section>

            {profile && (
                <section className="reader-card reader-compact-profile">
                    <h2>Thông tin tài khoản</h2>
                    <div className="reader-info-list">
                        <InfoRow label="Mã độc giả" value={profile.maDocGia} />
                        <InfoRow label="Email" value={profile.email} />
                        <InfoRow label="Hạn thẻ" value={formatDate(profile.ngayHetHanThe)} />
                    </div>
                </section>
            )}

            <section className="reader-section-block">
                <h2>Thao tác nhanh</h2>
                <div className="reader-quick-actions dashboard-actions">
                    <Link to="/reader/books"><Search size={17} />Tra cứu sách</Link>
                    <Link to="/reader/loans"><BookOpen size={17} />Gia hạn sách</Link>
                    <Link to="/reader/reservations"><BookmarkCheck size={17} />Xem đặt trước</Link>
                    <Link to="/reader/notifications"><Bell size={17} />Xem thông báo</Link>
                    <Link to="/reader/membership"><CreditCard size={17} />Gói độc giả</Link>
                    <Link to="/reader/rules#penalty-rules"><Calculator size={17} />Tính tiền phạt</Link>
                </div>
            </section>

            <RandomBookSection limit={12} />
        </div>
    );
}

function DashboardCard({ label, value, to, tone = "neutral" }) {
    return (
        <Link className={`reader-dashboard-card tone-${tone}`} to={to}>
            <span>{label}</span>
            <b>{value}</b>
        </Link>
    );
}

function InfoRow({ label, value }) {
    return (
        <div className="reader-info-row">
            <span>{label}</span>
            <b>{value || "Chưa cập nhật"}</b>
        </div>
    );
}

function formatDate(value) {
    if (!value) {
        return "Chưa cập nhật";
    }

    return new Intl.DateTimeFormat("vi-VN", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric"
    }).format(new Date(value));
}
