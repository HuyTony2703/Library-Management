package com.library.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public class TraSachRequest {

    @NotBlank(message = "Mã phiếu trả không được để trống")
    @Size(max = 20, message = "Mã phiếu trả tối đa 20 ký tự")
    private String maPhieuTra;

    @NotBlank(message = "Mã độc giả không được để trống")
    private String maDocGia;

    @Size(max = 30, message = "Mã nhân viên nhận tối đa 30 ký tự")
    private String maNhanVienNhan;

    @NotBlank(message = "Mã chi nhánh không được để trống")
    private String maChiNhanh;

    @NotEmpty(message = "Phải có ít nhất một cuốn sách được trả")
    @Valid
    private List<ChiTietTraRequest> chiTiet;

    private String ghiChu;

    private String idempotencyKey;

    private String requestFingerprint;

    public String getMaPhieuTra() { return maPhieuTra; }
    public void setMaPhieuTra(String maPhieuTra) { this.maPhieuTra = maPhieuTra; }

    public String getMaDocGia() { return maDocGia; }
    public void setMaDocGia(String maDocGia) { this.maDocGia = maDocGia; }

    public String getMaNhanVienNhan() { return maNhanVienNhan; }
    public void setMaNhanVienNhan(String maNhanVienNhan) { this.maNhanVienNhan = maNhanVienNhan; }

    public String getMaChiNhanh() { return maChiNhanh; }
    public void setMaChiNhanh(String maChiNhanh) { this.maChiNhanh = maChiNhanh; }

    public List<ChiTietTraRequest> getChiTiet() { return chiTiet; }
    public void setChiTiet(List<ChiTietTraRequest> chiTiet) { this.chiTiet = chiTiet; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public String getRequestFingerprint() { return requestFingerprint; }
    public void setRequestFingerprint(String requestFingerprint) { this.requestFingerprint = requestFingerprint; }

    public static class ChiTietTraRequest {

        @NotBlank(message = "Mã chi tiết mượn không được để trống")
        private String maChiTietMuon;

        @NotBlank(message = "Tình trạng khi trả không được để trống")
        private String tinhTrangKhiTra;

        @DecimalMin(value = "0.0", message = "Tiền phạt hỏng/mất không được âm")
        private BigDecimal tienPhatHongMat;

        private List<String> loaiHuHong;

        private String mucDoHuHong;

        private String moTaHuHong;

        @DecimalMin(value = "0.0", message = "Tiá»n pháº¡t Ä‘iá»u chá»‰nh khĂ´ng Ä‘Æ°á»£c Ă¢m")
        private BigDecimal tienPhatHongMatDieuChinh;

        private String lyDoDieuChinhTienPhat;

        private String ghiChu;

        public String getMaChiTietMuon() { return maChiTietMuon; }
        public void setMaChiTietMuon(String maChiTietMuon) { this.maChiTietMuon = maChiTietMuon; }

        public String getTinhTrangKhiTra() { return tinhTrangKhiTra; }
        public void setTinhTrangKhiTra(String tinhTrangKhiTra) { this.tinhTrangKhiTra = tinhTrangKhiTra; }

        public BigDecimal getTienPhatHongMat() { return tienPhatHongMat; }
        public void setTienPhatHongMat(BigDecimal tienPhatHongMat) { this.tienPhatHongMat = tienPhatHongMat; }

        public List<String> getLoaiHuHong() { return loaiHuHong; }
        public void setLoaiHuHong(List<String> loaiHuHong) { this.loaiHuHong = loaiHuHong; }

        public String getMucDoHuHong() { return mucDoHuHong; }
        public void setMucDoHuHong(String mucDoHuHong) { this.mucDoHuHong = mucDoHuHong; }

        public String getMoTaHuHong() { return moTaHuHong; }
        public void setMoTaHuHong(String moTaHuHong) { this.moTaHuHong = moTaHuHong; }

        public BigDecimal getTienPhatHongMatDieuChinh() { return tienPhatHongMatDieuChinh; }
        public void setTienPhatHongMatDieuChinh(BigDecimal tienPhatHongMatDieuChinh) {
            this.tienPhatHongMatDieuChinh = tienPhatHongMatDieuChinh;
        }

        public String getLyDoDieuChinhTienPhat() { return lyDoDieuChinhTienPhat; }
        public void setLyDoDieuChinhTienPhat(String lyDoDieuChinhTienPhat) {
            this.lyDoDieuChinhTienPhat = lyDoDieuChinhTienPhat;
        }

        public String getGhiChu() { return ghiChu; }
        public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    }
}
