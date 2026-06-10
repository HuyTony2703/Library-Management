package com.library.backend.service.reader;

import com.library.backend.dto.reader.ReaderBookCopyResponse;
import com.library.backend.dto.reader.ReaderBookDetailResponse;
import com.library.backend.dto.reader.ReaderBookListResponse;
import com.library.backend.exception.ResourceNotFoundException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ReaderBookService {

    private final JdbcTemplate jdbcTemplate;

    public ReaderBookService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ReaderBookListResponse> getBooks(String keyword) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        String likeKeyword = "%" + normalizedKeyword + "%";

        String sql = """
                SELECT
                    ds.MaDauSach,
                    ds.TenDauSach,
                    ds.AnhBia,
                    ds.NamXuatBan,
                    ds.TriGia,
                    ds.TrangThai,
                    COALESCE(SUM(CASE WHEN cs.MaTrangThai = 'TT_SANCO' THEN 1 ELSE 0 END), 0) AS SoCuonSanCo
                FROM DAUSACH ds
                LEFT JOIN CUONSACH cs
                    ON ds.MaDauSach = cs.MaDauSach
                WHERE ds.TrangThai = N'Hoạt động'
                  AND (
                        ? = ''
                        OR ds.MaDauSach LIKE ?
                        OR ds.TenDauSach LIKE ?
                        OR ds.ISBN LIKE ?
                        OR EXISTS (
                            SELECT 1
                            FROM DAUSACH_TACGIA dstg
                            INNER JOIN TACGIA tg
                                ON dstg.MaTacGia = tg.MaTacGia
                            WHERE dstg.MaDauSach = ds.MaDauSach
                              AND tg.TenTacGia LIKE ?
                        )
                        OR EXISTS (
                            SELECT 1
                            FROM DAUSACH_THELOAI dstl
                            INNER JOIN THELOAI tl
                                ON dstl.MaTheLoai = tl.MaTheLoai
                            WHERE dstl.MaDauSach = ds.MaDauSach
                              AND tl.TenTheLoai LIKE ?
                        )
                  )
                GROUP BY
                    ds.MaDauSach,
                    ds.TenDauSach,
                    ds.AnhBia,
                    ds.NamXuatBan,
                    ds.TriGia,
                    ds.TrangThai
                ORDER BY ds.TenDauSach ASC
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new ReaderBookListResponse(
                        rs.getString("MaDauSach"),
                        rs.getString("TenDauSach"),
                        rs.getString("AnhBia"),
                        rs.getInt("NamXuatBan"),
                        rs.getBigDecimal("TriGia"),
                        rs.getString("TrangThai"),
                        rs.getInt("SoCuonSanCo")
                ),
                normalizedKeyword,
                likeKeyword,
                likeKeyword,
                likeKeyword,
                likeKeyword,
                likeKeyword
        );
    }

    public ReaderBookDetailResponse getBookDetail(String maDauSach) {
        String sql = """
                SELECT
                    ds.MaDauSach,
                    ds.TenDauSach,
                    ds.ISBN,
                    ds.NamXuatBan,
                    ds.NgonNgu,
                    ds.SoTrang,
                    ds.MoTa,
                    ds.AnhBia,
                    ds.TriGia,
                    ds.TrangThai,
                    nxb.TenNhaXuatBan
                FROM DAUSACH ds
                LEFT JOIN NHAXUATBAN nxb
                    ON ds.MaNhaXuatBan = nxb.MaNhaXuatBan
                WHERE ds.MaDauSach = ?
                  AND ds.TrangThai = N'Hoạt động'
                """;

        List<ReaderBookDetailBase> bases = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new ReaderBookDetailBase(
                        rs.getString("MaDauSach"),
                        rs.getString("TenDauSach"),
                        rs.getString("ISBN"),
                        rs.getInt("NamXuatBan"),
                        rs.getString("NgonNgu"),
                        (Integer) rs.getObject("SoTrang"),
                        rs.getString("MoTa"),
                        rs.getString("AnhBia"),
                        rs.getBigDecimal("TriGia"),
                        rs.getString("TrangThai"),
                        rs.getString("TenNhaXuatBan")
                ),
                maDauSach
        );

        if (bases.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy đầu sách: " + maDauSach);
        }

        ReaderBookDetailBase base = bases.get(0);

        return new ReaderBookDetailResponse(
                base.maDauSach(),
                base.tenDauSach(),
                base.isbn(),
                base.namXuatBan(),
                base.ngonNgu(),
                base.soTrang(),
                base.moTa(),
                base.anhBia(),
                base.triGia(),
                base.trangThai(),
                base.nhaXuatBan(),
                getAuthors(maDauSach),
                getCategories(maDauSach),
                getBookCopies(maDauSach)
        );
    }

    public List<ReaderBookCopyResponse> getBookCopies(String maDauSach) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM DAUSACH
                WHERE MaDauSach = ?
                  AND TrangThai = N'Hoạt động'
                """,
                Integer.class,
                maDauSach
        );

        if (count == null || count == 0) {
            throw new ResourceNotFoundException("Không tìm thấy đầu sách: " + maDauSach);
        }

        String sql = """
                SELECT
                    cs.MaCuonSach,
                    cs.MaChiNhanh,
                    cn.TenChiNhanh,
                    cs.MaViTri,
                    vt.MaViTriHienThi,
                    cs.MaTrangThai,
                    tt.TenTrangThai
                FROM CUONSACH cs
                INNER JOIN CHINHANH cn
                    ON cs.MaChiNhanh = cn.MaChiNhanh
                INNER JOIN VITRISACH vt
                    ON cs.MaViTri = vt.MaViTri
                INNER JOIN TRANGTHAICUONSACH tt
                    ON cs.MaTrangThai = tt.MaTrangThai
                WHERE cs.MaDauSach = ?
                ORDER BY cs.MaCuonSach ASC
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new ReaderBookCopyResponse(
                        rs.getString("MaCuonSach"),
                        rs.getString("MaChiNhanh"),
                        rs.getString("TenChiNhanh"),
                        rs.getString("MaViTri"),
                        rs.getString("MaViTriHienThi"),
                        rs.getString("MaTrangThai"),
                        rs.getString("TenTrangThai")
                ),
                maDauSach
        );
    }

    private List<String> getAuthors(String maDauSach) {
        String sql = """
                SELECT tg.TenTacGia
                FROM DAUSACH_TACGIA dstg
                INNER JOIN TACGIA tg
                    ON dstg.MaTacGia = tg.MaTacGia
                WHERE dstg.MaDauSach = ?
                ORDER BY tg.TenTacGia ASC
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getString("TenTacGia"),
                maDauSach
        );
    }

    private List<String> getCategories(String maDauSach) {
        String sql = """
                SELECT tl.TenTheLoai
                FROM DAUSACH_THELOAI dstl
                INNER JOIN THELOAI tl
                    ON dstl.MaTheLoai = tl.MaTheLoai
                WHERE dstl.MaDauSach = ?
                ORDER BY tl.TenTheLoai ASC
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getString("TenTheLoai"),
                maDauSach
        );
    }

    private record ReaderBookDetailBase(
            String maDauSach,
            String tenDauSach,
            String isbn,
            Integer namXuatBan,
            String ngonNgu,
            Integer soTrang,
            String moTa,
            String anhBia,
            BigDecimal triGia,
            String trangThai,
            String nhaXuatBan
    ) {
    }
}

