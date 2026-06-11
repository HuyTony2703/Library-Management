import { RotateCcw, Search } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { readerApi } from "../../api/readerApi";
import ReaderBookCard from "../../components/reader/ReaderBookCard";

const ALL = "all";

function normalizeText(value) {
    return String(value || "")
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .replace(/\s+/g, "")
        .toLowerCase();
}

export default function ReaderBooksPage() {
    const [keyword, setKeyword] = useState("");
    const [books, setBooks] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [filters, setFilters] = useState({
        category: ALL,
        author: "",
        status: ALL,
        yearFrom: "",
        yearTo: "",
        sort: "newest",
        onlyAvailable: false
    });

    async function loadBooks(searchKeyword = keyword) {
        setLoading(true);
        setError("");

        try {
            const data = await readerApi.books({ keyword: searchKeyword });
            setBooks(Array.isArray(data) ? data : []);
        } catch (err) {
            setError(err.message || "Không tải được danh sách sách. Vui lòng thử lại.");
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

    function updateFilter(name, value) {
        setFilters((current) => ({ ...current, [name]: value }));
    }

    function resetFilters() {
        setFilters({
            category: ALL,
            author: "",
            status: ALL,
            yearFrom: "",
            yearTo: "",
            sort: "newest",
            onlyAvailable: false
        });
    }

    const categories = useMemo(() => {
        return [...new Set(books.map((book) => book.tenTheLoai || book.theLoai).filter(Boolean))].sort();
    }, [books]);

    const filteredBooks = useMemo(() => {
        const authorQuery = normalizeText(filters.author);

        return books
            .filter((book) => {
                const available = Number(book.soCuonSanCo || 0) > 0;
                const year = Number(book.namXuatBan || 0);
                const category = book.tenTheLoai || book.theLoai || "";
                const author = book.tenTacGia || book.tacGia || book.tenTacGiaChinh || "";

                if (filters.category !== ALL && category !== filters.category) {
                    return false;
                }

                if (filters.status === "available" && !available) {
                    return false;
                }

                if (filters.status === "unavailable" && available) {
                    return false;
                }

                if (filters.onlyAvailable && !available) {
                    return false;
                }

                if (filters.yearFrom && year < Number(filters.yearFrom)) {
                    return false;
                }

                if (filters.yearTo && year > Number(filters.yearTo)) {
                    return false;
                }

                if (authorQuery && !normalizeText(author).includes(authorQuery)) {
                    return false;
                }

                return true;
            })
            .sort((a, b) => {
                if (filters.sort === "name") {
                    return String(a.tenDauSach || "").localeCompare(String(b.tenDauSach || ""), "vi");
                }

                if (filters.sort === "available") {
                    return Number(b.soCuonSanCo || 0) - Number(a.soCuonSanCo || 0);
                }

                return Number(b.namXuatBan || 0) - Number(a.namXuatBan || 0);
            });
    }, [books, filters]);

    const availableCount = filteredBooks.filter((book) => Number(book.soCuonSanCo || 0) > 0).length;

    return (
        <div>
            <div className="reader-home-header">
                <small>DANH MỤC SÁCH</small>
                <h1>Tra cứu sách</h1>
                <p>Tìm kiếm theo tên sách, mã sách, ISBN, tác giả hoặc thể loại.</p>
            </div>

            <form className="reader-search-panel" onSubmit={handleSearch}>
                <div className="reader-search-input">
                    <Search size={18} />
                    <input
                        value={keyword}
                        onChange={(e) => setKeyword(e.target.value)}
                        placeholder="Nhập tên sách, mã sách, tác giả, thể loại..."
                        list="reader-book-suggestions"
                    />
                    <datalist id="reader-book-suggestions">
                        {books.slice(0, 12).map((book) => (
                            <option key={book.maDauSach} value={book.tenDauSach} />
                        ))}
                    </datalist>
                </div>

                <button type="submit" disabled={loading}>
                    {loading ? "Đang tìm..." : "Tìm kiếm"}
                </button>
            </form>

            <div className="reader-filter-panel">
                <select value={filters.category} onChange={(e) => updateFilter("category", e.target.value)}>
                    <option value={ALL}>Tất cả thể loại</option>
                    {categories.map((category) => (
                        <option key={category} value={category}>{category}</option>
                    ))}
                </select>

                <input
                    value={filters.author}
                    onChange={(e) => updateFilter("author", e.target.value)}
                    placeholder="Lọc theo tác giả"
                />

                <select value={filters.status} onChange={(e) => updateFilter("status", e.target.value)}>
                    <option value={ALL}>Tất cả trạng thái</option>
                    <option value="available">Còn sách</option>
                    <option value="unavailable">Hết sách</option>
                </select>

                <input
                    type="number"
                    value={filters.yearFrom}
                    onChange={(e) => updateFilter("yearFrom", e.target.value)}
                    placeholder="Từ năm"
                />

                <input
                    type="number"
                    value={filters.yearTo}
                    onChange={(e) => updateFilter("yearTo", e.target.value)}
                    placeholder="Đến năm"
                />

                <select value={filters.sort} onChange={(e) => updateFilter("sort", e.target.value)}>
                    <option value="newest">Mới nhất</option>
                    <option value="name">Tên A-Z</option>
                    <option value="available">Còn sách trước</option>
                </select>

                <label className="reader-check-filter">
                    <input
                        type="checkbox"
                        checked={filters.onlyAvailable}
                        onChange={(e) => updateFilter("onlyAvailable", e.target.checked)}
                    />
                    Chỉ sách còn có thể mượn
                </label>

                <button type="button" className="reader-secondary-button" onClick={resetFilters}>
                    <RotateCcw size={16} />
                    Đặt lại bộ lọc
                </button>
            </div>

            {!loading && !error && (
                <p className="reader-result-summary">
                    Tìm thấy {filteredBooks.length} đầu sách · {availableCount} đầu sách còn có thể mượn
                </p>
            )}

            {error && <div className="reader-error">{error}</div>}

            {loading && <p>Đang tải danh sách sách...</p>}

            {!loading && !error && filteredBooks.length === 0 && (
                <div className="reader-empty-box">
                    Không tìm thấy đầu sách phù hợp. Hãy thử bỏ bớt bộ lọc hoặc tìm bằng từ khóa khác.
                </div>
            )}

            {!loading && !error && filteredBooks.length > 0 && (
                <div className="reader-book-grid">
                    {filteredBooks.map((book) => (
                        <ReaderBookCard key={book.maDauSach} book={book} />
                    ))}
                </div>
            )}
        </div>
    );
}
