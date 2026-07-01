package com.library.backend.service;

import org.springframework.dao.EmptyResultDataAccessException;
import com.library.backend.dto.DauSachCreateRequest;
import com.library.backend.dto.DauSachListQuery;
import com.library.backend.dto.DauSachResponse;
import com.library.backend.dto.DauSachUpdateRequest;
import com.library.backend.dto.DauSachUpsertRequest;
import com.library.backend.dto.EntityPickerOptionResponse;
import com.library.backend.dto.IsbnCheckResponse;
import com.library.backend.dto.PageResponse;
import com.library.backend.entity.DauSach;
import com.library.backend.exception.BusinessException;
import com.library.backend.exception.CatalogValidationException;
import com.library.backend.repository.DauSachRepository;
import com.library.backend.repository.DauSachPageRepository;
import com.library.backend.repository.NhaXuatBanRepository;
import com.library.backend.repository.TacGiaRepository;
import com.library.backend.repository.TheLoaiRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;

import java.time.Year;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class DauSachService {

    private final DauSachRepository dauSachRepository;
    private final NhaXuatBanRepository nhaXuatBanRepository;
    private final TacGiaRepository tacGiaRepository;
    private final TheLoaiRepository theLoaiRepository;
    private final JdbcTemplate jdbcTemplate;
    private final DauSachPageRepository dauSachPageRepository;

    public DauSachService(
            DauSachRepository dauSachRepository,
            NhaXuatBanRepository nhaXuatBanRepository,
            TacGiaRepository tacGiaRepository,
            TheLoaiRepository theLoaiRepository,
            JdbcTemplate jdbcTemplate,
            DauSachPageRepository dauSachPageRepository
    ) {
        this.dauSachRepository = dauSachRepository;
        this.nhaXuatBanRepository = nhaXuatBanRepository;
        this.tacGiaRepository = tacGiaRepository;
        this.theLoaiRepository = theLoaiRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.dauSachPageRepository = dauSachPageRepository;
    }

    public List<DauSachResponse> getAll() {
        throw new BusinessException("Danh sach dau sach khong phan trang da bi vo hieu hoa; hay dung endpoint page");
    }

    public PageResponse<DauSachResponse> getPage(DauSachListQuery query) {
        if (query.page() < 1) {
            throw new IllegalArgumentException("Page phải bắt đầu từ 1");
        }
        if (query.pageSize() < 1 || query.pageSize() > 100) {
            throw new IllegalArgumentException("Page size phải nằm trong khoảng 1 đến 100");
        }
        if (query.yearFrom() != null && query.yearTo() != null && query.yearFrom() > query.yearTo()) {
            throw new IllegalArgumentException("yearFrom không được lớn hơn yearTo");
        }
        return dauSachPageRepository.findPage(query);
    }

    public List<String> getMatchingIds(DauSachListQuery query, List<String> excludedIds) {
        return getMatchingIds(query, excludedIds, null);
    }

    public List<String> getMatchingIds(DauSachListQuery query, List<String> excludedIds, Integer maxRows) {
        if (query.yearFrom() != null && query.yearTo() != null && query.yearFrom() > query.yearTo()) {
            throw new IllegalArgumentException("yearFrom khĂ´ng Ä‘Æ°á»£c lá»›n hÆ¡n yearTo");
        }
        return dauSachPageRepository.findMatchingIds(query, excludedIds, maxRows);
    }

    public List<DauSachResponse> getByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return ids.stream()
                .map(this::getById)
                .toList();
    }

    public List<EntityPickerOptionResponse> searchForPicker(String query, int limit, boolean activeOnly) {
        if (query == null || query.trim().length() < 2) {
            throw new IllegalArgumentException("Từ khóa tìm kiếm phải có ít nhất 2 ký tự");
        }
        if (limit < 1 || limit > 50) {
            throw new IllegalArgumentException("Giới hạn kết quả phải nằm trong khoảng 1 đến 50");
        }
        return dauSachPageRepository.searchForPicker(query, limit, activeOnly);
    }

    public List<EntityPickerOptionResponse> searchAuthors(String query, int limit) {
        validatePickerSearch(query, limit);
        return dauSachPageRepository.searchAuthors(query, limit);
    }

    public List<EntityPickerOptionResponse> searchCategories(String query, int limit) {
        validatePickerSearch(query, limit);
        return dauSachPageRepository.searchCategories(query, limit);
    }

    public List<EntityPickerOptionResponse> searchPublishers(String query, int limit) {
        validatePickerSearch(query, limit);
        return dauSachPageRepository.searchPublishers(query, limit);
    }

    private void validatePickerSearch(String query, int limit) {
        if (query == null || query.trim().length() < 2) {
            throw new IllegalArgumentException("Từ khóa tìm kiếm phải có ít nhất 2 ký tự");
        }
        if (limit < 1 || limit > 50) {
            throw new IllegalArgumentException("Giới hạn kết quả phải nằm trong khoảng 1 đến 50");
        }
    }

    public DauSachResponse getById(String maDauSach) {
        DauSach dauSach = dauSachRepository.findById(maDauSach)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đầu sách"));

        return toResponse(dauSach);
    }

    @Transactional
    public DauSachResponse create(DauSachCreateRequest request) {
        validateRequest(request, null);

        DauSach dauSach = new DauSach();
        dauSach.setMaDauSach(generateBookId());
        dauSach.setMaNhaXuatBan(emptyToNull(request.getMaNhaXuatBan()));
        dauSach.setTenDauSach(request.getTenDauSach().trim());
        dauSach.setIsbn(IsbnUtils.normalize(request.getIsbn()));
        dauSach.setNamXuatBan(request.getNamXuatBan());
        dauSach.setNgonNgu(trimToNull(request.getNgonNgu()));
        dauSach.setSoTrang(request.getSoTrang());
        dauSach.setMoTa(trimToNull(request.getMoTa()));
        dauSach.setAnhBia(trimToNull(request.getAnhBia()));
        dauSach.setTriGia(request.getTriGia());
        dauSach.setTrangThai("Hoạt động");

        DauSach saved = dauSachRepository.saveAndFlush(dauSach);

        saveTacGias(saved.getMaDauSach(), request.getMaTacGias());
        saveTheLoais(saved.getMaDauSach(), request.getMaTheLoais());

        return toResponse(saved);
    }

    @Transactional
    public DauSachResponse update(String maDauSach, DauSachUpdateRequest request) {
        DauSach dauSach = dauSachRepository.findById(maDauSach)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đầu sách"));

        validateRequest(request, maDauSach);

        dauSach.setMaNhaXuatBan(emptyToNull(request.getMaNhaXuatBan()));
        dauSach.setTenDauSach(request.getTenDauSach().trim());
        dauSach.setIsbn(IsbnUtils.normalize(request.getIsbn()));
        dauSach.setNamXuatBan(request.getNamXuatBan());
        dauSach.setNgonNgu(trimToNull(request.getNgonNgu()));
        dauSach.setSoTrang(request.getSoTrang());
        dauSach.setMoTa(trimToNull(request.getMoTa()));
        dauSach.setAnhBia(trimToNull(request.getAnhBia()));
        dauSach.setTriGia(request.getTriGia());

        DauSach updated = dauSachRepository.saveAndFlush(dauSach);

        deleteTacGias(maDauSach);
        deleteTheLoais(maDauSach);

        saveTacGias(maDauSach, request.getMaTacGias());
        saveTheLoais(maDauSach, request.getMaTheLoais());

        return toResponse(updated);
    }

    public IsbnCheckResponse checkIsbn(String isbn, String excludeId) {
        String normalized = IsbnUtils.normalize(isbn);
        if (normalized == null || !IsbnUtils.isValid(normalized)) {
            return new IsbnCheckResponse(false, normalized, "ISBN phải là ISBN-10 hoặc ISBN-13 có checksum hợp lệ", false, null);
        }
        Optional<DauSach> duplicate = findDuplicateIsbn(normalized, excludeId);
        return duplicate
                .map(book -> new IsbnCheckResponse(true, normalized, "ISBN đã thuộc một đầu sách khác", true, toResponse(book)))
                .orElseGet(() -> new IsbnCheckResponse(true, normalized, "ISBN hợp lệ và chưa được sử dụng", false, null));
    }

    private void validateRequest(DauSachUpsertRequest request, String maDauSachDangUpdate) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        if (hasText(request.getMaNhaXuatBan())
                && !nhaXuatBanRepository.existsById(request.getMaNhaXuatBan())) {
            fieldErrors.put("maNhaXuatBan", "Nhà xuất bản không tồn tại");
        }

        if (hasText(request.getIsbn())) {
            String normalized = IsbnUtils.normalize(request.getIsbn());
            if (!IsbnUtils.isValid(normalized)) {
                fieldErrors.put("isbn", "ISBN phải là ISBN-10 hoặc ISBN-13 có checksum hợp lệ");
            } else {
                Optional<DauSach> duplicate = findDuplicateIsbn(normalized, maDauSachDangUpdate);
                if (duplicate.isPresent()) {
                    DauSach existing = duplicate.get();
                    throw new CatalogValidationException(
                            HttpStatus.CONFLICT,
                            "DUPLICATE_ISBN",
                            "ISBN đã thuộc đầu sách " + existing.getMaDauSach(),
                            Map.of("isbn", "ISBN đã tồn tại"),
                            Map.of("existingBook", toResponse(existing))
                    );
                }
            }
        }

        int namHienTai = Year.now().getValue();

        if (request.getNamXuatBan() > namHienTai) {
            fieldErrors.put("namXuatBan", "Năm xuất bản không được lớn hơn năm hiện tại");
        }

        int khoangCachNamXuatBan = getKhoangCachNamXuatBan();

        if (namHienTai - request.getNamXuatBan() > khoangCachNamXuatBan) {
            fieldErrors.put("namXuatBan",
                    "Chỉ tiếp nhận sách xuất bản trong vòng " + khoangCachNamXuatBan + " năm"
            );
        }

        for (String maTacGia : request.getMaTacGias().stream().distinct().toList()) {
            if (!tacGiaRepository.existsById(maTacGia)) {
                fieldErrors.putIfAbsent("maTacGias", "Tác giả không tồn tại: " + maTacGia);
            }
        }

        for (String maTheLoai : request.getMaTheLoais().stream().distinct().toList()) {
            if (!theLoaiRepository.existsById(maTheLoai)) {
                fieldErrors.putIfAbsent("maTheLoais", "Thể loại không tồn tại: " + maTheLoai);
            }
        }

        if (hasText(request.getAnhBia()) && !request.getAnhBia().trim().matches("(?i)^https?://.+")) {
            fieldErrors.put("anhBia", "Ảnh bìa phải là URL http hoặc https hợp lệ");
        }

        if (!fieldErrors.isEmpty()) {
            throw new CatalogValidationException(
                    HttpStatus.BAD_REQUEST,
                    "VALIDATION_ERROR",
                    "Dữ liệu đầu sách không hợp lệ",
                    fieldErrors,
                    null
            );
        }
    }

    private Optional<DauSach> findDuplicateIsbn(String normalizedIsbn, String excludeId) {
        return dauSachRepository.findByNormalizedIsbn(normalizedIsbn)
                .filter(book -> excludeId == null || !book.getMaDauSach().equals(excludeId));
    }

    private String generateBookId() {
        String id;
        do {
            id = "DS-" + UUID.randomUUID().toString().replace("-", "").substring(0, 24).toUpperCase();
        } while (dauSachRepository.existsById(id));
        return id;
    }

    private void saveTacGias(String maDauSach, List<String> maTacGias) {
        for (String maTacGia : maTacGias.stream().distinct().toList()) {
            jdbcTemplate.update(
                    "INSERT INTO DAUSACH_TACGIA (MaDauSach, MaTacGia, VaiTro) VALUES (?, ?, ?)",
                    maDauSach,
                    maTacGia,
                    "Tác giả"
            );
        }
    }

    private void saveTheLoais(String maDauSach, List<String> maTheLoais) {
        for (String maTheLoai : maTheLoais.stream().distinct().toList()) {
            jdbcTemplate.update(
                    "INSERT INTO DAUSACH_THELOAI (MaDauSach, MaTheLoai) VALUES (?, ?)",
                    maDauSach,
                    maTheLoai
            );
        }
    }

    private void deleteTacGias(String maDauSach) {
        jdbcTemplate.update(
                "DELETE FROM DAUSACH_TACGIA WHERE MaDauSach = ?",
                maDauSach
        );
    }

    private void deleteTheLoais(String maDauSach) {
        jdbcTemplate.update(
                "DELETE FROM DAUSACH_THELOAI WHERE MaDauSach = ?",
                maDauSach
        );
    }

    private DauSachResponse toResponse(DauSach dauSach) {
        List<IdLabel> authors = jdbcTemplate.query(
                """
                SELECT tg.MaTacGia, tg.TenTacGia
                FROM DAUSACH_TACGIA dstg
                INNER JOIN TACGIA tg ON tg.MaTacGia = dstg.MaTacGia
                WHERE dstg.MaDauSach = ?
                ORDER BY tg.TenTacGia, tg.MaTacGia
                """,
                (rs, rowNum) -> new IdLabel(rs.getString(1), rs.getString(2)),
                dauSach.getMaDauSach()
        );
        List<IdLabel> categories = jdbcTemplate.query(
                """
                SELECT tl.MaTheLoai, tl.TenTheLoai
                FROM DAUSACH_THELOAI dstl
                INNER JOIN THELOAI tl ON tl.MaTheLoai = dstl.MaTheLoai
                WHERE dstl.MaDauSach = ?
                ORDER BY tl.TenTheLoai, tl.MaTheLoai
                """,
                (rs, rowNum) -> new IdLabel(rs.getString(1), rs.getString(2)),
                dauSach.getMaDauSach()
        );
        String publisherName = dauSach.getMaNhaXuatBan() == null ? null : jdbcTemplate.query(
                "SELECT TenNhaXuatBan FROM NHAXUATBAN WHERE MaNhaXuatBan = ?",
                (rs, rowNum) -> rs.getString(1),
                dauSach.getMaNhaXuatBan()
        ).stream().findFirst().orElse(null);
        Map<String, Long> copySummary = jdbcTemplate.queryForMap("""
                SELECT COUNT(*) AS total,
                       SUM(CASE WHEN tt.TenTrangThai = N'Sẵn có' THEN 1 ELSE 0 END) AS available
                FROM CUONSACH cs
                INNER JOIN TRANGTHAICUONSACH tt ON tt.MaTrangThai = cs.MaTrangThai
                WHERE cs.MaDauSach = ?
                """, dauSach.getMaDauSach()).entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        entry -> entry.getKey().toLowerCase(),
                        entry -> entry.getValue() == null ? 0L : ((Number) entry.getValue()).longValue()
                ));
        return new DauSachResponse(
                dauSach.getMaDauSach(),
                dauSach.getMaNhaXuatBan(),
                dauSach.getTenDauSach(),
                dauSach.getIsbn(),
                dauSach.getNamXuatBan(),
                dauSach.getNgonNgu(),
                dauSach.getSoTrang(),
                dauSach.getMoTa(),
                dauSach.getAnhBia(),
                dauSach.getTriGia(),
                dauSach.getTrangThai(),
                authors.stream().map(IdLabel::id).toList(),
                categories.stream().map(IdLabel::id).toList(),
                publisherName,
                authors.stream().map(IdLabel::label).toList(),
                categories.stream().map(IdLabel::label).toList(),
                copySummary.getOrDefault("total", 0L),
                copySummary.getOrDefault("available", 0L)
        );
    }

    private record IdLabel(String id, String label) {}

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String emptyToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
    private int getKhoangCachNamXuatBan() {
        String sql = """
            SELECT TOP 1 ts.KhoangCachNamXuatBan
            FROM THAMSOQUYDINH ts
            INNER JOIN PHIENBANQUYDINH pb
                ON ts.MaPhienBan = pb.MaPhienBan
            WHERE pb.TrangThai = N'Đang áp dụng'
            ORDER BY pb.NgayApDung DESC
            """;

        try {
            Integer result = jdbcTemplate.queryForObject(sql, Integer.class);
            return result == null ? 8 : result;
        } catch (EmptyResultDataAccessException ex) {
            return 8;
        }
    }
}
