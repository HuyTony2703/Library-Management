package com.library.backend.service;

import com.library.backend.dto.NhatKyHoatDongResponse;
import com.library.backend.entity.NhatKyHoatDong;
import com.library.backend.repository.NhatKyHoatDongRepository;
import com.library.backend.security.AuthUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ActivityLogService {

    private final NhatKyHoatDongRepository nhatKyHoatDongRepository;

    public ActivityLogService(NhatKyHoatDongRepository nhatKyHoatDongRepository) {
        this.nhatKyHoatDongRepository = nhatKyHoatDongRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSafe(
            String hanhDong,
            String doiTuongTacDong,
            String maDoiTuongTacDong,
            String moTaChiTiet
    ) {
        try {
            saveLog(
                    getCurrentMaTaiKhoan(),
                    hanhDong,
                    doiTuongTacDong,
                    maDoiTuongTacDong,
                    moTaChiTiet
            );
        } catch (Exception ex) {
            System.err.println("Không ghi được nhật ký hoạt động: " + ex.getMessage());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAsAccountSafe(
            String maTaiKhoan,
            String hanhDong,
            String doiTuongTacDong,
            String maDoiTuongTacDong,
            String moTaChiTiet
    ) {
        try {
            saveLog(
                    maTaiKhoan,
                    hanhDong,
                    doiTuongTacDong,
                    maDoiTuongTacDong,
                    moTaChiTiet
            );
        } catch (Exception ex) {
            System.err.println("Không ghi được nhật ký hoạt động: " + ex.getMessage());
        }
    }

    @Transactional
    public void logAsAccount(
            String maTaiKhoan,
            String hanhDong,
            String doiTuongTacDong,
            String maDoiTuongTacDong,
            String moTaChiTiet
    ) {
        saveLog(
                maTaiKhoan,
                hanhDong,
                doiTuongTacDong,
                maDoiTuongTacDong,
                moTaChiTiet
        );
    }

    public List<NhatKyHoatDongResponse> getLatestLogs(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 200));

        return nhatKyHoatDongRepository
                .findAllByOrderByThoiGianDesc(PageRequest.of(0, safeLimit))
                .stream()
                .map(NhatKyHoatDongResponse::from)
                .toList();
    }

    private void saveLog(
            String maTaiKhoan,
            String hanhDong,
            String doiTuongTacDong,
            String maDoiTuongTacDong,
            String moTaChiTiet
    ) {
        NhatKyHoatDong log = new NhatKyHoatDong();
        log.setMaTaiKhoan(maTaiKhoan);
        log.setHanhDong(hanhDong);
        log.setDoiTuongTacDong(doiTuongTacDong);
        log.setMaDoiTuongTacDong(maDoiTuongTacDong);
        log.setThoiGian(LocalDateTime.now());
        log.setDiaChiIP(getClientIp());
        log.setMoTaChiTiet(moTaChiTiet);

        nhatKyHoatDongRepository.save(log);
    }

    private String getCurrentMaTaiKhoan() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof AuthUser user) {
            return user.getMaTaiKhoan();
        }

        return null;
    }

    private String getClientIp() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            return null;
        }

        HttpServletRequest request = attributes.getRequest();

        String forwardedFor = request.getHeader("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}
