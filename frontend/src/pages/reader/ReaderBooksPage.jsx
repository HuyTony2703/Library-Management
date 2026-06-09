import { Search } from "lucide-react";
import { useEffect, useState } from "react";
import { readerApi } from "../../api/readerApi";
import ReaderBookCard from "../../components/reader/ReaderBookCard";

export default function ReaderBooksPage() {
    const [keyword, setKeyword] = useState("");
    const [books, setBooks] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    async function loadBooks(searchKeyword = keyword) {
        setLoading(true);
        setError("");

        try {
            const data = await readerApi.books({ keyword: searchKeyword });
            setBooks(Array.isArray(data) ? data : []);
        } catch (err) {
            setError(err.message || "Không tải được danh sách sách");
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        loadBooks("");
    }, []);

    function handleSearch(e) {
        e.preventDefault();
        loadBooks(keyword);
    }

    return (
        <div>
            <div className="reader-home-header">
                <small>Library Catalog</small>
                <h1>Tra cứu sách</h1>
                <p>Tìm kiếm đầu sách theo tên, mã sách, ISBN, tác giả hoặc thể loại.</p>
            </div>

            <form className="reader-search-panel" onSubmit={handleSearch}>
                <div className="reader-search-input">
                    <Search size={18} />
                    <input
                        value={keyword}
                        onChange={(e) => setKeyword(e.target.value)}
                        placeholder="Nhập tên sách, mã sách, tác giả, thể loại..."
                    />
                </div>

                <button type="submit">Tìm kiếm</button>
            </form>

            {error && <div className="reader-error">{error}</div>}

            {loading && <p>Đang tải danh sách sách...</p>}

            {!loading && !error && books.length === 0 && (
                <div className="reader-empty-box">
                    Không tìm thấy đầu sách phù hợp.
                </div>
            )}

            {!loading && !error && books.length > 0 && (
                <div className="reader-book-grid">
                    {books.map((book) => (
                        <ReaderBookCard key={book.maDauSach} book={book} />
                    ))}
                </div>
            )}
        </div>
    );
}

