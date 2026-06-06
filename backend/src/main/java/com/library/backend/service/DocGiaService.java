package com.library.backend.service;

import com.library.backend.dto.DocGiaRequest;
import com.library.backend.dto.DocGiaResponse;
import com.library.backend.entity.DocGia;
import com.library.backend.entity.LichSuGoiThanhVien;
import com.library.backend.entity.TaiKhoan;
import com.library.backend.repository.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

@Service
public class DocGiaService {

    private static final String VAI_TRO_DOC_GIA = "VT_DOC_GIA";
    private static final String GOI_MAC_DINH = "GOI_THUONG";

    private final DocGiaRepository docGiaRepository;
    private final TaiKhoanRepository taiKhoanRepository;
    private final NhomDocGiaRepository nhomDocGiaRepository;
    private final GoiThanhVienRepository goiThanhVienRepository;
    private final LichSuGoiThanhVienRepository lichSuGoiThanhVienRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    public DocGiaService(
            DocGiaRepository docGiaRepository,
            TaiKhoanRepository taiKhoanRepository,
            NhomDocGiaRepository nhomDocGiaRepository,
            GoiThanhVienRepository goiThanhVienRepository,
            LichSuGoiThanhVienRepository lichSuGoiThanhVienRepository,
            PasswordEncoder passwordEncoder,
            JdbcTemplate jdbcTemplate
    ) {
        this.docGiaRepository = docGiaRepository;
        this.taiKhoanRepository = taiKhoanRepository;
        this.nhomDocGiaRepository = nhomDocGiaRepository;
        this.goiThanhVienRepository = goiThanhVienRepository;
        this.lichSuGoiThanhVienRepository = lichSuGoiThanhVienRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<DocGiaResponse> getAll() {
        return docGiaRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public DocGiaResponse getById(String maDocGia) {
        DocGia docGia = docGiaRepository.findById(maDocGia)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy độc giả"));

        return toResponse(docGia);
    }

    @Transactional
    public DocGiaResponse create(DocGiaRequest request) {
        if (docGiaRepository.existsById(request.getMaDocGia())) {
            throw new RuntimeException("Mã độc giả đã tồn tại");
        }

        if (taiKhoanRepository.existsById(request.getMaTaiKhoan())) {
            throw new RuntimeException("Mã tài khoản đã tồn tại");
        }

        if (taiKhoanRepository.existsByTenDangNhap(request.getTenDangNhap())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }

        if (taiKhoanRepository.existsByEmailDangNhap(request.getEmail())) {
            throw new RuntimeException("Email đăng nhập đã tồn tại");
        }

        if (docGiaRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email độc giả đã tồn tại");
        }

        if (!nhomDocGiaRepository.existsById(request.getMaNhomDocGia())) {
            throw new RuntimeException("Nhóm độc giả không tồn tại");
        }

        String maGoi = request.getMaGoiThanhVien() == null || request.getMaGoiThanhVien().isBlank()
                ? GOI_MAC_DINH
                : request.getMaGoiThanhVien();

        if (!goiThanhVienRepository.existsById(maGoi)) {
            throw new RuntimeException("Gói thành viên không tồn tại");
        }

        ThamSoDocGia thamSo = getThamSoDocGia();

        LocalDate ngayLapThe = request.getNgayLapThe() == null
                ? LocalDate.now()
                : request.getNgayLapThe();

        int tuoi = Period.between(request.getNgaySinh(), ngayLapThe).getYears();

        if (tuoi < thamSo.tuoiToiThieu || tuoi > thamSo.tuoiToiDa) {
            throw new RuntimeException(
                    "Tuổi độc giả không hợp lệ. Tuổi phải từ "
                            + thamSo.tuoiToiThieu + " đến " + thamSo.tuoiToiDa
            );
        }

        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setMaTaiKhoan(request.getMaTaiKhoan());
        taiKhoan.setTenDangNhap(request.getTenDangNhap());
        taiKhoan.setMatKhauHash(passwordEncoder.encode(request.getMatKhau()));
        taiKhoan.setEmailDangNhap(request.getEmail());
        taiKhoan.setMaVaiTro(VAI_TRO_DOC_GIA);
        taiKhoan.setTrangThai("Hoạt động");
        taiKhoan.setNgayTao(LocalDateTime.now());
        taiKhoanRepository.save(taiKhoan);

        DocGia docGia = new DocGia();
        docGia.setMaDocGia(request.getMaDocGia());
        docGia.setMaTaiKhoan(request.getMaTaiKhoan());
        docGia.setMaNhomDocGia(request.getMaNhomDocGia());
        docGia.setHoTen(request.getHoTen());
        docGia.setNgaySinh(request.getNgaySinh());
        docGia.setDiaChi(request.getDiaChi());
        docGia.setEmail(request.getEmail());
        docGia.setSoDienThoai(request.getSoDienThoai());
        docGia.setNgayLapThe(ngayLapThe);
        docGia.setNgayHetHanThe(ngayLapThe.plusMonths(thamSo.thoiHanTheTheoThang));
        docGia.setTrangThai("Hoạt động");

        DocGia savedDocGia = docGiaRepository.save(docGia);

        int thoiHanGoiTheoNgay = getThoiHanGoiTheoNgay(maGoi, request.getMaNhomDocGia());

        LichSuGoiThanhVien lichSuGoi = new LichSuGoiThanhVien();
        lichSuGoi.setMaLichSuGoi("LSG_" + request.getMaDocGia());
        lichSuGoi.setMaDocGia(request.getMaDocGia());
        lichSuGoi.setMaGoiThanhVien(maGoi);
        lichSuGoi.setMaPhieuThu(null);
        lichSuGoi.setNgayBatDau(ngayLapThe);
        lichSuGoi.setNgayKetThuc(ngayLapThe.plusDays(thoiHanGoiTheoNgay));
        lichSuGoi.setTrangThai("Đang sử dụng");
        lichSuGoi.setGhiChu("Gói tạo mặc định khi lập độc giả");

        lichSuGoiThanhVienRepository.save(lichSuGoi);

        return toResponse(savedDocGia);
    }

    @Transactional
    public DocGiaResponse update(String maDocGia, DocGiaRequest request) {
        DocGia docGia = docGiaRepository.findById(maDocGia)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy độc giả"));

        if (!nhomDocGiaRepository.existsById(request.getMaNhomDocGia())) {
            throw new RuntimeException("Nhóm độc giả không tồn tại");
        }

        if (docGiaRepository.existsByEmailAndMaDocGiaNot(request.getEmail(), maDocGia)) {
            throw new RuntimeException("Email độc giả đã tồn tại");
        }

        docGia.setMaNhomDocGia(request.getMaNhomDocGia());
        docGia.setHoTen(request.getHoTen());
        docGia.setNgaySinh(request.getNgaySinh());
        docGia.setDiaChi(request.getDiaChi());
        docGia.setEmail(request.getEmail());
        docGia.setSoDienThoai(request.getSoDienThoai());

        return toResponse(docGiaRepository.save(docGia));
    }

    @Transactional
    public void disable(String maDocGia) {
        DocGia docGia = docGiaRepository.findById(maDocGia)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy độc giả"));

        docGia.setTrangThai("Ngừng hoạt động");
        docGiaRepository.save(docGia);
    }

    private DocGiaResponse toResponse(DocGia docGia) {
        String tenDangNhap = taiKhoanRepository.findById(docGia.getMaTaiKhoan())
                .map(TaiKhoan::getTenDangNhap)
                .orElse(null);

        LichSuGoiThanhVien goiHienTai = lichSuGoiThanhVienRepository
                .findFirstByMaDocGiaAndTrangThaiOrderByNgayBatDauDesc(
                        docGia.getMaDocGia(),
                        "Đang sử dụng"
                )
                .orElse(null);

        String maGoi = goiHienTai == null ? null : goiHienTai.getMaGoiThanhVien();
        LocalDate ngayHetHanGoi = goiHienTai == null ? null : goiHienTai.getNgayKetThuc();

        return new DocGiaResponse(
                docGia.getMaDocGia(),
                docGia.getMaTaiKhoan(),
                tenDangNhap,
                docGia.getMaNhomDocGia(),
                docGia.getHoTen(),
                docGia.getNgaySinh(),
                docGia.getDiaChi(),
                docGia.getEmail(),
                docGia.getSoDienThoai(),
                docGia.getNgayLapThe(),
                docGia.getNgayHetHanThe(),
                docGia.getTrangThai(),
                maGoi,
                ngayHetHanGoi
        );
    }

    private ThamSoDocGia getThamSoDocGia() {
        String sql = """
                SELECT TOP 1
                    ts.TuoiToiThieu,
                    ts.TuoiToiDa,
                    ts.ThoiHanTheTheoThang
                FROM THAMSOQUYDINH ts
                INNER JOIN PHIENBANQUYDINH pb
                    ON ts.MaPhienBan = pb.MaPhienBan
                WHERE pb.TrangThai = N'Đang áp dụng'
                ORDER BY pb.NgayApDung DESC
                """;

        return jdbcTemplate.queryForObject(
                sql,
                (rs, rowNum) -> new ThamSoDocGia(
                        rs.getInt("TuoiToiThieu"),
                        rs.getInt("TuoiToiDa"),
                        rs.getInt("ThoiHanTheTheoThang")
                )
        );
    }

    private int getThoiHanGoiTheoNgay(String maGoiThanhVien, String maNhomDocGia) {
        String sql = """
                SELECT TOP 1 g.ThoiHanGoiTheoNgay
                FROM GIAGOI_THEONHOM g
                INNER JOIN PHIENBANQUYDINH pb
                    ON g.MaPhienBan = pb.MaPhienBan
                WHERE pb.TrangThai = N'Đang áp dụng'
                  AND g.MaGoiThanhVien = ?
                  AND g.MaNhomDocGia = ?
                ORDER BY pb.NgayApDung DESC
                """;

        List<Integer> result = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getInt("ThoiHanGoiTheoNgay"),
                maGoiThanhVien,
                maNhomDocGia
        );

        if (result.isEmpty()) {
            return 180;
        }

        return result.get(0);
    }

    private static class ThamSoDocGia {
        private final int tuoiToiThieu;
        private final int tuoiToiDa;
        private final int thoiHanTheTheoThang;

        public ThamSoDocGia(int tuoiToiThieu, int tuoiToiDa, int thoiHanTheTheoThang) {
            this.tuoiToiThieu = tuoiToiThieu;
            this.tuoiToiDa = tuoiToiDa;
            this.thoiHanTheTheoThang = thoiHanTheTheoThang;
        }
    }
}