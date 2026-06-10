package com.library.backend.service;

import com.library.backend.dto.CuonSachRequest;
import com.library.backend.dto.CuonSachResponse;
import com.library.backend.entity.CuonSach;
import com.library.backend.entity.DauSach;
import com.library.backend.entity.TrangThaiCuonSach;
import com.library.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class CuonSachService {

    private static final String TT_SANCO = "TT_SANCO";
    private static final String TT_NGUNGLUUTHONG = "TT_NGUNGLUUTHONG";

    private final CuonSachRepository cuonSachRepository;
    private final DauSachRepository dauSachRepository;
    private final ChiNhanhRepository chiNhanhRepository;
    private final ViTriSachRepository viTriSachRepository;
    private final TrangThaiCuonSachRepository trangThaiCuonSachRepository;

    public CuonSachService(
            CuonSachRepository cuonSachRepository,
            DauSachRepository dauSachRepository,
            ChiNhanhRepository chiNhanhRepository,
            ViTriSachRepository viTriSachRepository,
            TrangThaiCuonSachRepository trangThaiCuonSachRepository
    ) {
        this.cuonSachRepository = cuonSachRepository;
        this.dauSachRepository = dauSachRepository;
        this.chiNhanhRepository = chiNhanhRepository;
        this.viTriSachRepository = viTriSachRepository;
        this.trangThaiCuonSachRepository = trangThaiCuonSachRepository;
    }

    public List<CuonSachResponse> getAll() {
        return cuonSachRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public CuonSachResponse getById(String maCuonSach) {
        CuonSach cuonSach = cuonSachRepository.findById(maCuonSach)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cuốn sách"));

        return toResponse(cuonSach);
    }

    @Transactional
    public CuonSachResponse create(CuonSachRequest request) {
        if (cuonSachRepository.existsById(request.getMaCuonSach())) {
            throw new RuntimeException("Mã cuốn sách đã tồn tại");
        }

        validateForeignKeys(request);
        validateUniqueCode(request, null);

        String maTrangThai = hasText(request.getMaTrangThai())
                ? request.getMaTrangThai()
                : TT_SANCO;

        if (!trangThaiCuonSachRepository.existsById(maTrangThai)) {
            throw new RuntimeException("Trạng thái cuốn sách không tồn tại");
        }

        CuonSach cuonSach = new CuonSach();
        cuonSach.setMaCuonSach(request.getMaCuonSach());
        cuonSach.setMaDauSach(request.getMaDauSach());
        cuonSach.setMaChiNhanh(request.getMaChiNhanh());
        cuonSach.setMaViTri(request.getMaViTri());
        cuonSach.setMaTrangThai(maTrangThai);
        cuonSach.setMaVach(emptyToNull(request.getMaVach()));
        cuonSach.setMaQrCode(emptyToNull(request.getMaQrCode()));
        cuonSach.setNgayNhapSach(
                request.getNgayNhapSach() == null ? LocalDate.now() : request.getNgayNhapSach()
        );
        cuonSach.setGhiChu(request.getGhiChu());

        CuonSach saved = cuonSachRepository.save(cuonSach);

        return toResponse(saved);
    }

    @Transactional
    public CuonSachResponse update(String maCuonSach, CuonSachRequest request) {
        CuonSach cuonSach = cuonSachRepository.findById(maCuonSach)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cuốn sách"));

        validateForeignKeys(request);
        validateUniqueCode(request, maCuonSach);

        String maTrangThai = hasText(request.getMaTrangThai())
                ? request.getMaTrangThai()
                : cuonSach.getMaTrangThai();

        if (!trangThaiCuonSachRepository.existsById(maTrangThai)) {
            throw new RuntimeException("Trạng thái cuốn sách không tồn tại");
        }

        cuonSach.setMaDauSach(request.getMaDauSach());
        cuonSach.setMaChiNhanh(request.getMaChiNhanh());
        cuonSach.setMaViTri(request.getMaViTri());
        cuonSach.setMaTrangThai(maTrangThai);
        cuonSach.setMaVach(emptyToNull(request.getMaVach()));
        cuonSach.setMaQrCode(emptyToNull(request.getMaQrCode()));

        if (request.getNgayNhapSach() != null) {
            cuonSach.setNgayNhapSach(request.getNgayNhapSach());
        }

        cuonSach.setGhiChu(request.getGhiChu());

        CuonSach updated = cuonSachRepository.save(cuonSach);

        return toResponse(updated);
    }

    @Transactional
    public void disable(String maCuonSach) {
        CuonSach cuonSach = cuonSachRepository.findById(maCuonSach)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cuốn sách"));

        if (!trangThaiCuonSachRepository.existsById(TT_NGUNGLUUTHONG)) {
            throw new RuntimeException("Thiếu trạng thái TT_NGUNGLUUTHONG trong database");
        }

        cuonSach.setMaTrangThai(TT_NGUNGLUUTHONG);
        cuonSachRepository.save(cuonSach);
    }

    @Transactional
    public void hardDelete(String maCuonSach) {
        CuonSach cuonSach = cuonSachRepository.findById(maCuonSach)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cuốn sách"));

        cuonSachRepository.delete(cuonSach);
        cuonSachRepository.flush();
    }

    private void validateForeignKeys(CuonSachRequest request) {
        if (!dauSachRepository.existsById(request.getMaDauSach())) {
            throw new RuntimeException("Đầu sách không tồn tại");
        }

        if (!chiNhanhRepository.existsById(request.getMaChiNhanh())) {
            throw new RuntimeException("Chi nhánh không tồn tại");
        }

        if (!viTriSachRepository.existsById(request.getMaViTri())) {
            throw new RuntimeException("Vị trí sách không tồn tại");
        }
    }

    private void validateUniqueCode(CuonSachRequest request, String maCuonSachDangUpdate) {
        if (hasText(request.getMaVach())) {
            boolean trungMaVach = maCuonSachDangUpdate == null
                    ? cuonSachRepository.existsByMaVach(request.getMaVach())
                    : cuonSachRepository.existsByMaVachAndMaCuonSachNot(request.getMaVach(), maCuonSachDangUpdate);

            if (trungMaVach) {
                throw new RuntimeException("Mã vạch đã tồn tại");
            }
        }

        if (hasText(request.getMaQrCode())) {
            boolean trungQr = maCuonSachDangUpdate == null
                    ? cuonSachRepository.existsByMaQrCode(request.getMaQrCode())
                    : cuonSachRepository.existsByMaQrCodeAndMaCuonSachNot(request.getMaQrCode(), maCuonSachDangUpdate);

            if (trungQr) {
                throw new RuntimeException("Mã QR đã tồn tại");
            }
        }
    }

    private CuonSachResponse toResponse(CuonSach cuonSach) {
        String tenDauSach = dauSachRepository.findById(cuonSach.getMaDauSach())
                .map(DauSach::getTenDauSach)
                .orElse(null);

        String tenTrangThai = trangThaiCuonSachRepository.findById(cuonSach.getMaTrangThai())
                .map(TrangThaiCuonSach::getTenTrangThai)
                .orElse(null);

        return new CuonSachResponse(
                cuonSach.getMaCuonSach(),
                cuonSach.getMaDauSach(),
                tenDauSach,
                cuonSach.getMaChiNhanh(),
                cuonSach.getMaViTri(),
                cuonSach.getMaTrangThai(),
                tenTrangThai,
                cuonSach.getMaVach(),
                cuonSach.getMaQrCode(),
                cuonSach.getNgayNhapSach(),
                cuonSach.getGhiChu()
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
}
