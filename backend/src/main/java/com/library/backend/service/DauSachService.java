package com.library.backend.service;

import org.springframework.dao.EmptyResultDataAccessException;
import com.library.backend.dto.DauSachRequest;
import com.library.backend.dto.DauSachResponse;
import com.library.backend.entity.DauSach;
import com.library.backend.repository.DauSachRepository;
import com.library.backend.repository.NhaXuatBanRepository;
import com.library.backend.repository.TacGiaRepository;
import com.library.backend.repository.TheLoaiRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.List;

@Service
public class DauSachService {

    private final DauSachRepository dauSachRepository;
    private final NhaXuatBanRepository nhaXuatBanRepository;
    private final TacGiaRepository tacGiaRepository;
    private final TheLoaiRepository theLoaiRepository;
    private final JdbcTemplate jdbcTemplate;

    public DauSachService(
            DauSachRepository dauSachRepository,
            NhaXuatBanRepository nhaXuatBanRepository,
            TacGiaRepository tacGiaRepository,
            TheLoaiRepository theLoaiRepository,
            JdbcTemplate jdbcTemplate
    ) {
        this.dauSachRepository = dauSachRepository;
        this.nhaXuatBanRepository = nhaXuatBanRepository;
        this.tacGiaRepository = tacGiaRepository;
        this.theLoaiRepository = theLoaiRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<DauSachResponse> getAll() {
        return dauSachRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public DauSachResponse getById(String maDauSach) {
        DauSach dauSach = dauSachRepository.findById(maDauSach)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đầu sách"));

        return toResponse(dauSach);
    }

    @Transactional
    public DauSachResponse create(DauSachRequest request) {
        if (dauSachRepository.existsById(request.getMaDauSach())) {
            throw new RuntimeException("Mã đầu sách đã tồn tại");
        }

        validateRequest(request, null);

        DauSach dauSach = new DauSach();
        dauSach.setMaDauSach(request.getMaDauSach());
        dauSach.setMaNhaXuatBan(emptyToNull(request.getMaNhaXuatBan()));
        dauSach.setTenDauSach(request.getTenDauSach());
        dauSach.setIsbn(emptyToNull(request.getIsbn()));
        dauSach.setNamXuatBan(request.getNamXuatBan());
        dauSach.setNgonNgu(request.getNgonNgu());
        dauSach.setSoTrang(request.getSoTrang());
        dauSach.setMoTa(request.getMoTa());
        dauSach.setAnhBia(request.getAnhBia());
        dauSach.setTriGia(request.getTriGia());
        dauSach.setTrangThai("Hoạt động");

        DauSach saved = dauSachRepository.saveAndFlush(dauSach);

        saveTacGias(saved.getMaDauSach(), request.getMaTacGias());
        saveTheLoais(saved.getMaDauSach(), request.getMaTheLoais());

        return toResponse(saved);
    }

    @Transactional
    public DauSachResponse update(String maDauSach, DauSachRequest request) {
        DauSach dauSach = dauSachRepository.findById(maDauSach)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đầu sách"));

        validateRequest(request, maDauSach);

        dauSach.setMaNhaXuatBan(emptyToNull(request.getMaNhaXuatBan()));
        dauSach.setTenDauSach(request.getTenDauSach());
        dauSach.setIsbn(emptyToNull(request.getIsbn()));
        dauSach.setNamXuatBan(request.getNamXuatBan());
        dauSach.setNgonNgu(request.getNgonNgu());
        dauSach.setSoTrang(request.getSoTrang());
        dauSach.setMoTa(request.getMoTa());
        dauSach.setAnhBia(request.getAnhBia());
        dauSach.setTriGia(request.getTriGia());

        DauSach updated = dauSachRepository.saveAndFlush(dauSach);

        deleteTacGias(maDauSach);
        deleteTheLoais(maDauSach);

        saveTacGias(maDauSach, request.getMaTacGias());
        saveTheLoais(maDauSach, request.getMaTheLoais());

        return toResponse(updated);
    }

    @Transactional
    public void disable(String maDauSach) {
        DauSach dauSach = dauSachRepository.findById(maDauSach)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đầu sách"));

        dauSach.setTrangThai("Ngừng hiển thị");
        dauSachRepository.save(dauSach);
    }

    @Transactional
    public void restore(String maDauSach) {
        DauSach dauSach = dauSachRepository.findById(maDauSach)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đầu sách"));

        dauSach.setTrangThai("Hoạt động");
        dauSachRepository.save(dauSach);
    }

    @Transactional
    public void hardDelete(String maDauSach) {
        DauSach dauSach = dauSachRepository.findById(maDauSach)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đầu sách"));

        deleteTacGias(maDauSach);
        deleteTheLoais(maDauSach);
        dauSachRepository.delete(dauSach);
        dauSachRepository.flush();
    }

    private void validateRequest(DauSachRequest request, String maDauSachDangUpdate) {
        if (hasText(request.getMaNhaXuatBan())
                && !nhaXuatBanRepository.existsById(request.getMaNhaXuatBan())) {
            throw new RuntimeException("Nhà xuất bản không tồn tại");
        }

        if (hasText(request.getIsbn())) {
            boolean isbnTrung;

            if (maDauSachDangUpdate == null) {
                isbnTrung = dauSachRepository.existsByIsbn(request.getIsbn());
            } else {
                isbnTrung = dauSachRepository.existsByIsbnAndMaDauSachNot(
                        request.getIsbn(),
                        maDauSachDangUpdate
                );
            }

            if (isbnTrung) {
                throw new RuntimeException("ISBN đã tồn tại");
            }
        }

        int namHienTai = Year.now().getValue();

        if (request.getNamXuatBan() > namHienTai) {
            throw new RuntimeException("Năm xuất bản không được lớn hơn năm hiện tại");
        }

        int khoangCachNamXuatBan = getKhoangCachNamXuatBan();

        if (namHienTai - request.getNamXuatBan() > khoangCachNamXuatBan) {
            throw new RuntimeException(
                    "Chỉ tiếp nhận sách xuất bản trong vòng " + khoangCachNamXuatBan + " năm"
            );
        }

        for (String maTacGia : request.getMaTacGias()) {
            if (!tacGiaRepository.existsById(maTacGia)) {
                throw new RuntimeException("Tác giả không tồn tại: " + maTacGia);
            }
        }

        for (String maTheLoai : request.getMaTheLoais()) {
            if (!theLoaiRepository.existsById(maTheLoai)) {
                throw new RuntimeException("Thể loại không tồn tại: " + maTheLoai);
            }
        }
    }

    private void saveTacGias(String maDauSach, List<String> maTacGias) {
        for (String maTacGia : maTacGias) {
            jdbcTemplate.update(
                    "INSERT INTO DAUSACH_TACGIA (MaDauSach, MaTacGia, VaiTro) VALUES (?, ?, ?)",
                    maDauSach,
                    maTacGia,
                    "Tác giả"
            );
        }
    }

    private void saveTheLoais(String maDauSach, List<String> maTheLoais) {
        for (String maTheLoai : maTheLoais) {
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

    private List<String> getTacGiaIds(String maDauSach) {
        return jdbcTemplate.queryForList(
                "SELECT MaTacGia FROM DAUSACH_TACGIA WHERE MaDauSach = ?",
                String.class,
                maDauSach
        );
    }

    private List<String> getTheLoaiIds(String maDauSach) {
        return jdbcTemplate.queryForList(
                "SELECT MaTheLoai FROM DAUSACH_THELOAI WHERE MaDauSach = ?",
                String.class,
                maDauSach
        );
    }

    private DauSachResponse toResponse(DauSach dauSach) {
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
                getTacGiaIds(dauSach.getMaDauSach()),
                getTheLoaiIds(dauSach.getMaDauSach())
        );
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String emptyToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value;
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
