import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { readerApi } from "../../api/readerApi";
import RandomBookSection from "../../components/reader/RandomBookSection";

export default function ReaderHomePage() {
    const [profile, setProfile] = useState(null);
    const [error, setError] = useState("");

    useEffect(() => {
        async function loadProfile() {
            try {
                const data = await readerApi.me();
                setProfile(data);
            } catch (err) {
                setError(err.message || "Không tải được thông tin độc giả");
            }
        }

        loadProfile();
    }, []);

    return (
        <div>
            <div className="reader-home-header">
                <small>Reader Portal</small>
                <h1>Trang chủ độc giả</h1>
                <p>Quản lý sách đang mượn, đặt trước, thông báo và gói thành viên của bạn.</p>
            </div>

            {error && <div className="reader-error">{error}</div>}

            {!error && !profile && <p>Đang tải thông tin độc giả...</p>}

            {profile && (
                <>
                <div className="reader-profile-grid">
                    <div className="reader-card">
                        <h2>Thông tin thẻ độc giả</h2>

                        <div className="reader-info-list">
                            <div className="reader-info-row">
                                <span>Mã độc giả</span>
                                <b>{profile.maDocGia}</b>
                            </div>

                            <div className="reader-info-row">
                                <span>Họ tên</span>
                                <b>{profile.hoTen}</b>
                            </div>

                            <div className="reader-info-row">
                                <span>Email</span>
                                <b>{profile.email}</b>
                            </div>

                            <div className="reader-info-row">
                                <span>Số điện thoại</span>
                                <b>{profile.soDienThoai || "Chưa cập nhật"}</b>
                            </div>

                            <div className="reader-info-row">
                                <span>Nhóm độc giả</span>
                                <b>{profile.maNhomDocGia}</b>
                            </div>

                            <div className="reader-info-row">
                                <span>Ngày lập thẻ</span>
                                <b>{profile.ngayLapThe}</b>
                            </div>

                            <div className="reader-info-row">
                                <span>Ngày hết hạn</span>
                                <b>{profile.ngayHetHanThe}</b>
                            </div>

                            <div className="reader-info-row">
                                <span>Trạng thái</span>
                                <b className="reader-status-good">{profile.trangThai}</b>
                            </div>
                        </div>
                    </div>

                    <div className="reader-card">
                        <h2>Thao tác nhanh</h2>

                        <div className="reader-quick-actions">
                            <Link to="/reader/books">Tra cứu sách</Link>
                            <Link to="/reader/loans">Xem sách đang mượn</Link>
                            <Link to="/reader/reservations">Đặt trước sách</Link>
                            <Link to="/reader/notifications">Xem thông báo</Link>
                        </div>
                    </div>
                </div>

                <RandomBookSection limit={6} />
                </>
            )}
        </div>
    );
}
