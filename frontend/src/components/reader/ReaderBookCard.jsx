import { BookOpen, CheckCircle2, XCircle } from "lucide-react";
import { Link } from "react-router-dom";
import FavoriteButton from "./FavoriteButton";

function formatMoney(value) {
    return `${Number(value || 0).toLocaleString("vi-VN")}đ`;
}

export default function ReaderBookCard({ book, initialFavorite = null, onFavoriteChanged }) {
    const availableCount = Number(book.soCuonSanCo || 0);
    const isAvailable = availableCount > 0;
    const author = book.tenTacGia || book.tacGia || book.tenTacGiaChinh || "Chưa cập nhật tác giả";
    const category = book.tenTheLoai || book.theLoai || "Chưa cập nhật thể loại";

    return (
        <article className="reader-book-card">
            <div className="reader-book-cover">
                {book.anhBia ? (
                    <img src={book.anhBia} alt={book.tenDauSach} />
                ) : (
                    <div className="reader-book-cover-placeholder">
                        <BookOpen size={34} />
                    </div>
                )}
            </div>

            <div className="reader-book-body">
                <div>
                    <h3>{book.tenDauSach}</h3>
                    <p className="reader-book-author">{author}</p>

                    <div className="reader-book-meta">
                        <span>{category} · {book.namXuatBan || "Chưa cập nhật năm"}</span>
                        <span>Mã sách: {book.maDauSach}</span>
                        <span>Trị giá: {formatMoney(book.triGia)}</span>
                    </div>
                </div>

                <div>
                    <div className={`reader-book-available ${isAvailable ? "is-available" : "is-unavailable"}`}>
                        {isAvailable ? <CheckCircle2 size={15} /> : <XCircle size={15} />}
                        {isAvailable ? `${availableCount} bản có thể mượn` : "Hết sách"}
                    </div>

                    <div className="reader-book-footer">
                        <div className="reader-book-actions">
                            <Link to={`/reader/books/${book.maDauSach}`}>
                                {isAvailable ? "Xem chi tiết" : "Đặt trước"}
                            </Link>
                            <FavoriteButton
                                maDauSach={book.maDauSach}
                                initialFavorite={initialFavorite}
                                compact
                                onChanged={onFavoriteChanged}
                            />
                        </div>
                    </div>
                </div>
            </div>
        </article>
    );
}
