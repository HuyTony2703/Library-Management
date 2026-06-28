package com.library.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public class PhieuThuRequest {

    @Size(max = 20, message = "Ma phieu thu toi da 20 ky tu")
    private String maPhieuThu;

    @NotBlank(message = "Ma doc gia khong duoc de trong")
    private String maDocGia;

    @Size(max = 30, message = "Ma nhan vien thu toi da 30 ky tu")
    private String maNhanVienThu;

    @NotBlank(message = "Ma phuong thuc thanh toan khong duoc de trong")
    private String maPhuongThuc;

    @NotNull(message = "So tien thu khong duoc de trong")
    @DecimalMin(value = "0.01", message = "So tien thu phai lon hon 0")
    private BigDecimal soTienThu;

    private String maGiaoDichNgoai;

    private String externalTransactionId;

    private BigDecimal cashReceived;

    private String ghiChu;

    @Valid
    private List<ChiTietThuNoRequest> chiTietNo;

    public String getMaPhieuThu() { return maPhieuThu; }
    public void setMaPhieuThu(String maPhieuThu) { this.maPhieuThu = maPhieuThu; }

    public String getMaDocGia() { return maDocGia; }
    public void setMaDocGia(String maDocGia) { this.maDocGia = maDocGia; }

    public String getMaNhanVienThu() { return maNhanVienThu; }
    public void setMaNhanVienThu(String maNhanVienThu) { this.maNhanVienThu = maNhanVienThu; }

    public String getMaPhuongThuc() { return maPhuongThuc; }
    public void setMaPhuongThuc(String maPhuongThuc) { this.maPhuongThuc = maPhuongThuc; }

    public BigDecimal getSoTienThu() { return soTienThu; }
    public void setSoTienThu(BigDecimal soTienThu) { this.soTienThu = soTienThu; }

    public String getMaGiaoDichNgoai() { return maGiaoDichNgoai; }
    public void setMaGiaoDichNgoai(String maGiaoDichNgoai) { this.maGiaoDichNgoai = maGiaoDichNgoai; }

    public String getExternalTransactionId() { return externalTransactionId; }
    public void setExternalTransactionId(String externalTransactionId) { this.externalTransactionId = externalTransactionId; }

    public BigDecimal getCashReceived() { return cashReceived; }
    public void setCashReceived(BigDecimal cashReceived) { this.cashReceived = cashReceived; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public List<ChiTietThuNoRequest> getChiTietNo() { return chiTietNo; }
    public void setChiTietNo(List<ChiTietThuNoRequest> chiTietNo) { this.chiTietNo = chiTietNo; }

    public static class ChiTietThuNoRequest {

        @NotBlank(message = "Ma khoan no khong duoc de trong")
        private String maKhoanNo;

        @NotNull(message = "So tien ap dung khong duoc de trong")
        @DecimalMin(value = "0.01", message = "So tien ap dung phai lon hon 0")
        private BigDecimal soTienApDung;

        public String getMaKhoanNo() { return maKhoanNo; }
        public void setMaKhoanNo(String maKhoanNo) { this.maKhoanNo = maKhoanNo; }

        public BigDecimal getSoTienApDung() { return soTienApDung; }
        public void setSoTienApDung(BigDecimal soTienApDung) { this.soTienApDung = soTienApDung; }
    }
}
