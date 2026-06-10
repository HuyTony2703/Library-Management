package com.library.backend.service;

import com.library.backend.dto.KhoanNoResponse;
import com.library.backend.dto.PhieuThuRequest;
import com.library.backend.dto.PhieuThuResponse;
import com.library.backend.entity.ChiTietPhieuThuNo;
import com.library.backend.entity.KhoanNo;
import com.library.backend.entity.PhieuThu;
import com.library.backend.exception.BusinessException;
import com.library.backend.exception.ResourceNotFoundException;
import com.library.backend.repository.ChiTietPhieuThuNoRepository;
import com.library.backend.repository.DocGiaRepository;
import com.library.backend.repository.KhoanNoRepository;
import com.library.backend.repository.PhieuThuRepository;
import com.library.backend.repository.PhuongThucThanhToanRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ThanhToanService {

    private final KhoanNoRepository khoanNoRepository;
    private final PhieuThuRepository phieuThuRepository;
    private final ChiTietPhieuThuNoRepository chiTietPhieuThuNoRepository;
    private final PhuongThucThanhToanRepository phuongThucThanhToanRepository;
    private final DocGiaRepository docGiaRepository;
    private final JdbcTemplate jdbcTemplate;
    private final ActivityLogService activityLogService;

    public ThanhToanService(
            KhoanNoRepository khoanNoRepository,
            PhieuThuRepository phieuThuRepository,
            ChiTietPhieuThuNoRepository chiTietPhieuThuNoRepository,
            PhuongThucThanhToanRepository phuongThucThanhToanRepository,
            DocGiaRepository docGiaRepository,
            JdbcTemplate jdbcTemplate,
            ActivityLogService activityLogService
    ) {
        this.khoanNoRepository = khoanNoRepository;
        this.phieuThuRepository = phieuThuRepository;
        this.chiTietPhieuThuNoRepository = chiTietPhieuThuNoRepository;
        this.phuongThucThanhToanRepository = phuongThucThanhToanRepository;
        this.docGiaRepository = docGiaRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.activityLogService = activityLogService;
    }

    public List<KhoanNoResponse> getDebtsByReader(String maDocGia) {
        if (!docGiaRepository.existsById(maDocGia)) {
            throw new ResourceNotFoundException("Độc giả không tồn tại");
        }

        return khoanNoRepository.findByMaDocGiaOrderByNgayPhatSinhDesc(maDocGia)
                .stream()
                .map(this::toKhoanNoResponse)
                .toList();
    }

    @Transactional
    public PhieuThuResponse createPayment(PhieuThuRequest request) {
        if (request.getSoTienThu() == null || request.getSoTienThu().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Số tiền thu phải lớn hơn 0");
        }

        if (phieuThuRepository.existsById(request.getMaPhieuThu())) {
            throw new BusinessException("Mã phiếu thu đã tồn tại");
        }

        if (!docGiaRepository.existsById(request.getMaDocGia())) {
            throw new ResourceNotFoundException("Độc giả không tồn tại");
        }

        if (hasText(request.getMaNhanVienThu())
                && !existsById("NHANVIEN", "MaNhanVien", request.getMaNhanVienThu())) {
            throw new ResourceNotFoundException("Nhân viên thu không tồn tại");
        }

        if (!phuongThucThanhToanRepository.existsById(request.getMaPhuongThuc())) {
            throw new ResourceNotFoundException("Phương thức thanh toán không tồn tại");
        }

        List<PaymentApplyItem> applyItems;

        if (request.getChiTietNo() != null && !request.getChiTietNo().isEmpty()) {
            applyItems = buildManualApplyItems(request);
        } else {
            applyItems = buildAutoApplyItems(request.getMaDocGia(), request.getSoTienThu());
        }

        BigDecimal tongApDung = applyItems.stream()
                .map(PaymentApplyItem::soTienApDung)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (tongApDung.compareTo(request.getSoTienThu()) != 0) {
            throw new BusinessException("Tổng tiền áp dụng vào khoản nợ phải bằng số tiền thu");
        }

        PhieuThu phieuThu = new PhieuThu();
        phieuThu.setMaPhieuThu(request.getMaPhieuThu());
        phieuThu.setMaDocGia(request.getMaDocGia());
        phieuThu.setMaNhanVienThu(emptyToNull(request.getMaNhanVienThu()));
        phieuThu.setMaPhuongThuc(request.getMaPhuongThuc());
        phieuThu.setLoaiThu("Thu tiền phạt");
        phieuThu.setSoTienThu(request.getSoTienThu());
        phieuThu.setNgayThu(LocalDateTime.now());
        phieuThu.setMaGiaoDichNgoai(emptyToNull(request.getMaGiaoDichNgoai()));
        phieuThu.setTrangThai("Thành công");
        phieuThu.setGhiChu(request.getGhiChu());

        phieuThuRepository.save(phieuThu);

        for (int i = 0; i < applyItems.size(); i++) {
            PaymentApplyItem item = applyItems.get(i);
            KhoanNo khoanNo = item.khoanNo();

            BigDecimal daThanhToanMoi = khoanNo.getSoTienDaThanhToan().add(item.soTienApDung());

            if (daThanhToanMoi.compareTo(khoanNo.getSoTienPhatSinh()) > 0) {
                throw new BusinessException("Số tiền thanh toán vượt quá khoản nợ: " + khoanNo.getMaKhoanNo());
            }

            khoanNo.setSoTienDaThanhToan(daThanhToanMoi);

            if (daThanhToanMoi.compareTo(khoanNo.getSoTienPhatSinh()) == 0) {
                khoanNo.setTrangThai("Đã thanh toán");
            } else {
                khoanNo.setTrangThai("Thanh toán một phần");
            }

            khoanNoRepository.save(khoanNo);

            ChiTietPhieuThuNo chiTiet = new ChiTietPhieuThuNo();
            chiTiet.setMaChiTietPhieuThu(
                    buildMaChiTietPhieuThu(request.getMaPhieuThu(), i + 1)
            );
            chiTiet.setMaPhieuThu(request.getMaPhieuThu());
            chiTiet.setMaKhoanNo(khoanNo.getMaKhoanNo());
            chiTiet.setSoTienApDung(item.soTienApDung());

            chiTietPhieuThuNoRepository.save(chiTiet);
        }

        PhieuThuResponse response = getPaymentById(request.getMaPhieuThu());

        activityLogService.logSafe(
                "Thu tiền",
                "PHIEUTHU",
                request.getMaPhieuThu(),
                "Thu tiền độc giả " + request.getMaDocGia()
                        + ", số tiền: " + request.getSoTienThu()
                        + ", phương thức: " + request.getMaPhuongThuc()
        );

        return response;
    }

    public PhieuThuResponse getPaymentById(String maPhieuThu) {
        PhieuThu phieuThu = phieuThuRepository.findById(maPhieuThu)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu thu"));

        return toPhieuThuResponse(phieuThu);
    }

    public List<PhieuThuResponse> getPaymentsByReader(String maDocGia) {
        if (!docGiaRepository.existsById(maDocGia)) {
            throw new ResourceNotFoundException("Độc giả không tồn tại");
        }

        return phieuThuRepository.findByMaDocGiaOrderByNgayThuDesc(maDocGia)
                .stream()
                .map(this::toPhieuThuResponse)
                .toList();
    }

    private List<PaymentApplyItem> buildManualApplyItems(PhieuThuRequest request) {
        List<PaymentApplyItem> result = new ArrayList<>();
        Set<String> usedDebts = new HashSet<>();

        for (PhieuThuRequest.ChiTietThuNoRequest item : request.getChiTietNo()) {
            if (item.getSoTienApDung() == null || item.getSoTienApDung().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("Số tiền áp dụng phải lớn hơn 0");
            }

            if (!usedDebts.add(item.getMaKhoanNo())) {
                throw new BusinessException("Khoản nợ bị lặp trong phiếu thu: " + item.getMaKhoanNo());
            }

            KhoanNo khoanNo = khoanNoRepository.findById(item.getMaKhoanNo())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy khoản nợ: " + item.getMaKhoanNo()
                    ));

            if (!request.getMaDocGia().equals(khoanNo.getMaDocGia())) {
                throw new BusinessException("Khoản nợ không thuộc độc giả này: " + item.getMaKhoanNo());
            }

            BigDecimal conLai = getConLai(khoanNo);

            if (conLai.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("Khoản nợ đã thanh toán hết: " + item.getMaKhoanNo());
            }

            if (item.getSoTienApDung().compareTo(conLai) > 0) {
                throw new BusinessException("Số tiền thu vượt quá khoản nợ còn lại: " + item.getMaKhoanNo());
            }

            result.add(new PaymentApplyItem(khoanNo, item.getSoTienApDung()));
        }

        return result;
    }

    private List<PaymentApplyItem> buildAutoApplyItems(String maDocGia, BigDecimal soTienThu) {
        List<KhoanNo> debts = khoanNoRepository
                .findByMaDocGiaAndTrangThaiNotOrderByNgayPhatSinhAsc(maDocGia, "Đã thanh toán");

        BigDecimal tongNoConLai = debts.stream()
                .map(this::getConLai)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (tongNoConLai.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Độc giả không có khoản nợ cần thanh toán");
        }

        if (tongNoConLai.compareTo(soTienThu) < 0) {
            throw new BusinessException("Số tiền thu vượt quá tổng nợ còn lại");
        }

        List<PaymentApplyItem> result = new ArrayList<>();
        BigDecimal remaining = soTienThu;

        for (KhoanNo debt : debts) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal conLai = getConLai(debt);

            if (conLai.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            BigDecimal applyAmount = remaining.min(conLai);
            result.add(new PaymentApplyItem(debt, applyAmount));
            remaining = remaining.subtract(applyAmount);
        }

        return result;
    }

    private KhoanNoResponse toKhoanNoResponse(KhoanNo khoanNo) {
        return new KhoanNoResponse(
                khoanNo.getMaKhoanNo(),
                khoanNo.getMaDocGia(),
                khoanNo.getMaLoaiKhoanNo(),
                khoanNo.getMaChiTietTra(),
                khoanNo.getSoTienPhatSinh(),
                khoanNo.getSoTienDaThanhToan(),
                getConLai(khoanNo),
                khoanNo.getNgayPhatSinh(),
                khoanNo.getLyDo(),
                khoanNo.getTrangThai()
        );
    }

    private PhieuThuResponse toPhieuThuResponse(PhieuThu phieuThu) {
        List<PhieuThuResponse.ChiTietPhieuThuNoResponse> chiTiet = chiTietPhieuThuNoRepository
                .findByMaPhieuThu(phieuThu.getMaPhieuThu())
                .stream()
                .map(ct -> new PhieuThuResponse.ChiTietPhieuThuNoResponse(
                        ct.getMaChiTietPhieuThu(),
                        ct.getMaKhoanNo(),
                        ct.getSoTienApDung()
                ))
                .toList();

        return new PhieuThuResponse(
                phieuThu.getMaPhieuThu(),
                phieuThu.getMaDocGia(),
                phieuThu.getMaNhanVienThu(),
                phieuThu.getMaPhuongThuc(),
                phieuThu.getLoaiThu(),
                phieuThu.getSoTienThu(),
                phieuThu.getNgayThu(),
                phieuThu.getTrangThai(),
                chiTiet
        );
    }

    private BigDecimal getConLai(KhoanNo khoanNo) {
        return khoanNo.getSoTienPhatSinh().subtract(khoanNo.getSoTienDaThanhToan());
    }

    private String buildMaChiTietPhieuThu(String maPhieuThu, int index) {
        String maChiTietPhieuThu = "CTPT_" + maPhieuThu + "_" + String.format("%02d", index);

        if (maChiTietPhieuThu.length() > 30) {
            throw new BusinessException("Mã chi tiết phiếu thu vượt quá 30 ký tự");
        }

        return maChiTietPhieuThu;
    }

    private boolean existsById(String tableName, String idColumn, String value) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + tableName + " WHERE " + idColumn + " = ?",
                Integer.class,
                value
        );

        return count != null && count > 0;
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

    private record PaymentApplyItem(KhoanNo khoanNo, BigDecimal soTienApDung) {
    }
}
