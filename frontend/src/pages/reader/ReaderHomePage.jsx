import { BookOpen, BookmarkCheck, CreditCard, Heart, Search } from "lucide-react";
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

  if (error) {
    return (
      <div>
        <div className="reader-home-header">
          <small>Reader Portal</small>
          <h1>Trang chủ độc giả</h1>
          <p>Không tải được thông tin tài khoản độc giả.</p>
        </div>

        <div className="reader-error">{error}</div>
      </div>
    );
  }

  return (
    <div>
      <div className="reader-home-header">
        <small>Reader Portal</small>
        <h1>Trang chủ độc giả</h1>
        <p>Theo dõi hồ sơ, tra cứu sách và quản lý các hoạt động thư viện của bạn.</p>
      </div>

      {!profile ? (
        <div className="reader-card">
          <p className="reader-muted">Đang tải thông tin độc giả...</p>
        </div>
      ) : (
        <div className="reader-profile-grid">
          <section className="reader-card">
            <h2>Thông tin cá nhân</h2>

            <div className="reader-info-list">
              <InfoRow label="Họ tên" value={profile.hoTen} />
              <InfoRow label="Mã độc giả" value={profile.maDocGia} />
              <InfoRow label="Email" value={profile.email} />
              <InfoRow label="Số điện thoại" value={profile.soDienThoai || "Chưa cập nhật"} />
              <InfoRow label="Ngày lập thẻ" value={formatDate(profile.ngayLapThe)} />
              <InfoRow label="Ngày hết hạn" value={formatDate(profile.ngayHetHanThe)} />
            </div>
          </section>

          <section className="reader-card">
            <h2>Trạng thái thẻ</h2>
            <span className={getStatusClass(profile.trangThai)}>
              {profile.trangThai || "Chưa cập nhật"}
            </span>

            <div className="reader-quick-actions">
              <Link to="/reader/books">
                <Search size={17} />
                Tra cứu sách
              </Link>
              <Link to="/reader/loans">
                <BookOpen size={17} />
                Sách đang mượn
              </Link>
              <Link to="/reader/reservations">
                <BookmarkCheck size={17} />
                Phiếu đặt trước
              </Link>
              <Link to="/reader/favorites">
                <Heart size={17} />
                Sách yêu thích
              </Link>
              <Link to="/reader/membership">
                <CreditCard size={17} />
                Gói thành viên
              </Link>
            </div>
          </section>
        </div>
      )}

      <RandomBookSection limit={12} />
    </div>
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

  return new Date(value).toLocaleDateString("vi-VN");
}

function getStatusClass(status) {
  if (status === "Hoạt động") {
    return "reader-status-good";
  }

  if (status === "Hết hạn") {
    return "reader-status-warn";
  }

  return "reader-status-bad";
}
