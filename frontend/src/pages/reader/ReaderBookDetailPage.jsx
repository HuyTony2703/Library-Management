import { ArrowLeft } from "lucide-react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { useEffect, useState } from "react";
import { readerApi } from "../../api/readerApi";
import FavoriteButton from "../../components/reader/FavoriteButton";
import ReaderBookComments from "../../components/reader/ReaderBookComments";
import ReaderBookCopyList from "../../components/reader/ReaderBookCopyList";
import ReaderBookRating from "../../components/reader/ReaderBookRating";
import ReservationButton from "../../components/reader/ReservationButton";

function formatMoney(value) {
    return `${Number(value || 0).toLocaleString("vi-VN")}đ`;
}

export default function ReaderBookDetailPage() {
    const { maDauSach } = useParams();
    const navigate = useNavigate();

    const [book, setBook] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    async function loadBook() {
        setLoading(true);
        setError("");

        try {
            const data = await readerApi.bookDetail(maDauSach);
            setBook(data);
        } catch (err) {
            setError(err.message || "Không tải được chi tiết sách");
        } finally {
            setLoading(false);
        }
    }

    async function handleReserved() {
        await loadBook();
        navigate("/reader/reservations");
    }

    useEffect(() => {
        loadBook();
    }, [maDauSach]);

    if (loading) {
        return <p>Đang tải chi tiết sách...</p>;
    }

    if (error) {
        return (
            <div>
                <button className="reader-back-button" onClick={() => navigate(-1)}>
                    <ArrowLeft size={18} />
                    Quay lại
                </button>

                <div className="reader-error">{error}</div>
            </div>
        );
    }

    if (!book) {
        return null;
    }

    const defaultReservationBranch =
        book.cuonSach?.find((copy) => copy.maTrangThai === "TT_SANCO")?.maChiNhanh ||
        book.cuonSach?.[0]?.maChiNhanh ||
        "CN_TD";

    return (
        <div>
            <button className="reader-back-button" onClick={() => navigate(-1)}>
                <ArrowLeft size={18} />
                Quay lại
            </button>

            <div className="reader-book-detail">
                <div className="reader-book-detail-cover">
                    {book.anhBia ? (
                        <img src={book.anhBia} alt={book.tenDauSach} />
                    ) : (
                        <div className="reader-book-detail-placeholder">
                            {book.tenDauSach?.slice(0, 1) || "S"}
                        </div>
                    )}
                </div>

                <div className="reader-book-detail-info">
                    <div className="reader-book-code">{book.maDauSach}</div>
                    <h1>{book.tenDauSach}</h1>

                    <p className="reader-book-description">
                        {book.moTa || "Chưa có mô tả cho đầu sách này."}
                    </p>

                    <div className="reader-chip-row">
                        {(book.theLoai || []).map((item) => (
                            <span className="reader-chip" key={item}>
                                {item}
                            </span>
                        ))}
                    </div>

                    <div className="reader-detail-grid">
                        <div>
                            <span>ISBN</span>
                            <b>{book.isbn || "Chưa cập nhật"}</b>
                        </div>

                        <div>
                            <span>Năm xuất bản</span>
                            <b>{book.namXuatBan}</b>
                        </div>

                        <div>
                            <span>Nhà xuất bản</span>
                            <b>{book.nhaXuatBan || "Chưa cập nhật"}</b>
                        </div>

                        <div>
                            <span>Ngôn ngữ</span>
                            <b>{book.ngonNgu || "Chưa cập nhật"}</b>
                        </div>

                        <div>
                            <span>Số trang</span>
                            <b>{book.soTrang || "Chưa cập nhật"}</b>
                        </div>

                        <div>
                            <span>Trị giá</span>
                            <b>{formatMoney(book.triGia)}</b>
                        </div>
                    </div>

                    <div className="reader-author-box">
                        <span>Tác giả</span>
                        <b>
                            {(book.tacGia || []).length > 0
                                ? book.tacGia.join(", ")
                                : "Chưa cập nhật"}
                        </b>
                    </div>

                    <div className="reader-action-row">
                        <ReservationButton
                            maDauSach={book.maDauSach}
                            maChiNhanh={defaultReservationBranch}
                            label="Đặt trước đầu sách"
                            onSuccess={handleReserved}
                        />

                        <FavoriteButton maDauSach={book.maDauSach} />
                    </div>
                </div>
            </div>

            <section className="reader-card">
                <h2>Danh sách cuốn sách</h2>
                <ReaderBookCopyList
                    maDauSach={book.maDauSach}
                    copies={book.cuonSach || []}
                    onReserved={handleReserved}
                />
            </section>

            <ReaderBookRating maDauSach={book.maDauSach} />

            <ReaderBookComments maDauSach={book.maDauSach} />

            <div className="reader-bottom-link">
                <Link to="/reader/books">Quay lại danh sách sách</Link>
            </div>
        </div>
    );
}
