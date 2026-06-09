import { BookOpen, CheckCircle2 } from "lucide-react";
import { Link } from "react-router-dom";

function formatMoney(value) {
    return `${Number(value || 0).toLocaleString("vi-VN")}đ`;
}

export default function ReaderBookCard({ book }) {
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
                    <div className="reader-book-code">{book.maDauSach}</div>
                    <h3>{book.tenDauSach}</h3>

                    <div className="reader-book-meta">
                        <span>Năm xuất bản: {book.namXuatBan || "Chưa cập nhật"}</span>
                        <span>Trị giá: {formatMoney(book.triGia)}</span>
                    </div>
                </div>

                <div>
                    <div className="reader-book-available">
                        <CheckCircle2 size={15} />
                        {book.soCuonSanCo || 0} cuốn sẵn có
                    </div>

                    <div className="reader-book-footer">
                        <span className="reader-book-status">{book.trangThai}</span>
                        <Link to={`/reader/books/${book.maDauSach}`}>Xem chi tiết</Link>
                    </div>
                </div>
            </div>
        </article>
    );
}

