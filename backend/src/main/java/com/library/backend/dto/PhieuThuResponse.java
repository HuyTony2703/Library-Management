package com.library.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PhieuThuResponse {

    private String maPhieuThu;
    private String maDocGia;
    private String maNhanVienThu;
    private String maPhuongThuc;
    private String loaiThu;
    private BigDecimal soTienThu;
    private BigDecimal tienKhachDua;
    private BigDecimal tienThua;
    private String maGiaoDichNgoai;
    private String ghiChu;
    private LocalDateTime ngayThu;
    private String trangThai;
    private List<ChiTietPhieuThuNoResponse> chiTietNo;
    private PrintReceiptData printData;

    public PhieuThuResponse(
            String maPhieuThu,
            String maDocGia,
            String maNhanVienThu,
            String maPhuongThuc,
            String loaiThu,
            BigDecimal soTienThu,
            LocalDateTime ngayThu,
            String trangThai,
            List<ChiTietPhieuThuNoResponse> chiTietNo
    ) {
        this(maPhieuThu, maDocGia, maNhanVienThu, maPhuongThuc, loaiThu, soTienThu,
                null, null, null, null, ngayThu, trangThai, chiTietNo, null);
    }

    public PhieuThuResponse(
            String maPhieuThu,
            String maDocGia,
            String maNhanVienThu,
            String maPhuongThuc,
            String loaiThu,
            BigDecimal soTienThu,
            BigDecimal tienKhachDua,
            BigDecimal tienThua,
            String maGiaoDichNgoai,
            String ghiChu,
            LocalDateTime ngayThu,
            String trangThai,
            List<ChiTietPhieuThuNoResponse> chiTietNo,
            PrintReceiptData printData
    ) {
        this.maPhieuThu = maPhieuThu;
        this.maDocGia = maDocGia;
        this.maNhanVienThu = maNhanVienThu;
        this.maPhuongThuc = maPhuongThuc;
        this.loaiThu = loaiThu;
        this.soTienThu = soTienThu;
        this.tienKhachDua = tienKhachDua;
        this.tienThua = tienThua;
        this.maGiaoDichNgoai = maGiaoDichNgoai;
        this.ghiChu = ghiChu;
        this.ngayThu = ngayThu;
        this.trangThai = trangThai;
        this.chiTietNo = chiTietNo;
        this.printData = printData;
    }

    public String getMaPhieuThu() { return maPhieuThu; }
    public String getMaDocGia() { return maDocGia; }
    public String getMaNhanVienThu() { return maNhanVienThu; }
    public String getMaPhuongThuc() { return maPhuongThuc; }
    public String getLoaiThu() { return loaiThu; }
    public BigDecimal getSoTienThu() { return soTienThu; }
    public BigDecimal getTienKhachDua() { return tienKhachDua; }
    public BigDecimal getTienThua() { return tienThua; }
    public String getMaGiaoDichNgoai() { return maGiaoDichNgoai; }
    public String getGhiChu() { return ghiChu; }
    public LocalDateTime getNgayThu() { return ngayThu; }
    public String getTrangThai() { return trangThai; }
    public List<ChiTietPhieuThuNoResponse> getChiTietNo() { return chiTietNo; }
    public PrintReceiptData getPrintData() { return printData; }

    public static class ChiTietPhieuThuNoResponse {
        private String maChiTietPhieuThu;
        private String maKhoanNo;
        private BigDecimal soTienApDung;

        public ChiTietPhieuThuNoResponse(
                String maChiTietPhieuThu,
                String maKhoanNo,
                BigDecimal soTienApDung
        ) {
            this.maChiTietPhieuThu = maChiTietPhieuThu;
            this.maKhoanNo = maKhoanNo;
            this.soTienApDung = soTienApDung;
        }

        public String getMaChiTietPhieuThu() { return maChiTietPhieuThu; }
        public String getMaKhoanNo() { return maKhoanNo; }
        public BigDecimal getSoTienApDung() { return soTienApDung; }
    }

    public record PrintReceiptData(
            String receiptId,
            String readerId,
            String staffId,
            String paymentMethodId,
            BigDecimal amount,
            BigDecimal cashReceived,
            BigDecimal changeAmount,
            LocalDateTime paidAt,
            List<PrintReceiptLine> lines
    ) {
    }

    public record PrintReceiptLine(
            String debtId,
            BigDecimal appliedAmount
    ) {
    }
}
