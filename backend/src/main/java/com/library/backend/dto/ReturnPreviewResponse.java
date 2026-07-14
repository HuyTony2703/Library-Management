package com.library.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ReturnPreviewResponse {

    private final boolean eligible;
    private final String maDocGia;
    private final String maChiNhanh;
    private final LocalDateTime previewedAt;
    private final BigDecimal tongPhatTre;
    private final BigDecimal tongPhatHongMat;
    private final BigDecimal tongPhat;
    private final List<String> warnings;
    private final List<ReturnPreviewItemResponse> chiTiet;

    public ReturnPreviewResponse(
            boolean eligible,
            String maDocGia,
            String maChiNhanh,
            LocalDateTime previewedAt,
            BigDecimal tongPhatTre,
            BigDecimal tongPhatHongMat,
            BigDecimal tongPhat,
            List<String> warnings,
            List<ReturnPreviewItemResponse> chiTiet
    ) {
        this.eligible = eligible;
        this.maDocGia = maDocGia;
        this.maChiNhanh = maChiNhanh;
        this.previewedAt = previewedAt;
        this.tongPhatTre = tongPhatTre;
        this.tongPhatHongMat = tongPhatHongMat;
        this.tongPhat = tongPhat;
        this.warnings = warnings;
        this.chiTiet = chiTiet;
    }

    public boolean isEligible() { return eligible; }
    public String getMaDocGia() { return maDocGia; }
    public String getMaChiNhanh() { return maChiNhanh; }
    public LocalDateTime getPreviewedAt() { return previewedAt; }
    public BigDecimal getTongPhatTre() { return tongPhatTre; }
    public BigDecimal getTongPhatHongMat() { return tongPhatHongMat; }
    public BigDecimal getTongPhat() { return tongPhat; }
    public List<String> getWarnings() { return warnings; }
    public List<ReturnPreviewItemResponse> getChiTiet() { return chiTiet; }

    public static class ReturnPreviewItemResponse {
        private final String maChiTietMuon;
        private final String maCuonSach;
        private final String maDauSach;
        private final String tenDauSach;
        private final String tinhTrangKhiTra;
        private final List<String> loaiHuHong;
        private final String mucDoHuHong;
        private final String moTaHuHong;
        private final Integer soNgayTre;
        private final BigDecimal tienPhatTre;
        private final BigDecimal tienPhatHongMatDeXuat;
        private final BigDecimal tienPhatHongMat;
        private final BigDecimal tongPhat;
        private final boolean dieuChinhTienPhat;
        private final String lyDoDieuChinhTienPhat;

        public ReturnPreviewItemResponse(
                String maChiTietMuon,
                String maCuonSach,
                String maDauSach,
                String tenDauSach,
                String tinhTrangKhiTra,
                List<String> loaiHuHong,
                String mucDoHuHong,
                String moTaHuHong,
                Integer soNgayTre,
                BigDecimal tienPhatTre,
                BigDecimal tienPhatHongMatDeXuat,
                BigDecimal tienPhatHongMat,
                BigDecimal tongPhat,
                boolean dieuChinhTienPhat,
                String lyDoDieuChinhTienPhat
        ) {
            this.maChiTietMuon = maChiTietMuon;
            this.maCuonSach = maCuonSach;
            this.maDauSach = maDauSach;
            this.tenDauSach = tenDauSach;
            this.tinhTrangKhiTra = tinhTrangKhiTra;
            this.loaiHuHong = loaiHuHong;
            this.mucDoHuHong = mucDoHuHong;
            this.moTaHuHong = moTaHuHong;
            this.soNgayTre = soNgayTre;
            this.tienPhatTre = tienPhatTre;
            this.tienPhatHongMatDeXuat = tienPhatHongMatDeXuat;
            this.tienPhatHongMat = tienPhatHongMat;
            this.tongPhat = tongPhat;
            this.dieuChinhTienPhat = dieuChinhTienPhat;
            this.lyDoDieuChinhTienPhat = lyDoDieuChinhTienPhat;
        }

        public String getMaChiTietMuon() { return maChiTietMuon; }
        public String getMaCuonSach() { return maCuonSach; }
        public String getMaDauSach() { return maDauSach; }
        public String getTenDauSach() { return tenDauSach; }
        public String getTinhTrangKhiTra() { return tinhTrangKhiTra; }
        public List<String> getLoaiHuHong() { return loaiHuHong; }
        public String getMucDoHuHong() { return mucDoHuHong; }
        public String getMoTaHuHong() { return moTaHuHong; }
        public Integer getSoNgayTre() { return soNgayTre; }
        public BigDecimal getTienPhatTre() { return tienPhatTre; }
        public BigDecimal getTienPhatHongMatDeXuat() { return tienPhatHongMatDeXuat; }
        public BigDecimal getTienPhatHongMat() { return tienPhatHongMat; }
        public BigDecimal getTongPhat() { return tongPhat; }
        public boolean isDieuChinhTienPhat() { return dieuChinhTienPhat; }
        public String getLyDoDieuChinhTienPhat() { return lyDoDieuChinhTienPhat; }
    }
}
