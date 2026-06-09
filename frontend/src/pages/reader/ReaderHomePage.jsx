import { useEffect, useState } from "react";
import { readerApi } from "../../api/readerApi";

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
        <h1>Trang chủ độc giả</h1>
        <p style={{ color: "red" }}>{error}</p>
      </div>
    );
  }

  return (
    <div>
      <h1>Trang chủ độc giả</h1>

      {!profile ? (
        <p>Đang tải thông tin độc giả...</p>
      ) : (
        <div>
          <p>
            Xin chào, <b>{profile.hoTen}</b>
          </p>
          <p>Mã độc giả: {profile.maDocGia}</p>
          <p>Email: {profile.email}</p>
          <p>Trạng thái thẻ: {profile.trangThai}</p>
          <p>Ngày hết hạn thẻ: {profile.ngayHetHanThe}</p>
        </div>
      )}
    </div>
  );
}