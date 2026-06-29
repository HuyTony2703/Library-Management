package com.library.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class TraSachResponse {

    private String maPhieuTra;
    private String maDocGia;
    private String maNhanVienNhan;
    private String maChiNhanh;
    private LocalDateTime ngayTra;
    private String idempotencyKey;
    private List<KhoanNoTraResponse> khoanNo;
    private PrintPayload printPayload;
    private PaymentSuggestion paymentSuggestion;
    private List<ChiTietTraResponse> chiTiet;

    public TraSachResponse(
            String maPhieuTra,
            String maDocGia,
            String maNhanVienNhan,
            String maChiNhanh,
            LocalDateTime ngayTra,
            String idempotencyKey,
            List<KhoanNoTraResponse> khoanNo,
            PrintPayload printPayload,
            PaymentSuggestion paymentSuggestion,
            List<ChiTietTraResponse> chiTiet
    ) {
        this.maPhieuTra = maPhieuTra;
        this.maDocGia = maDocGia;
        this.maNhanVienNhan = maNhanVienNhan;
        this.maChiNhanh = maChiNhanh;
        this.ngayTra = ngayTra;
        this.idempotencyKey = idempotencyKey;
        this.khoanNo = khoanNo;
        this.printPayload = printPayload;
        this.paymentSuggestion = paymentSuggestion;
        this.chiTiet = chiTiet;
    }

    public String getMaPhieuTra() { return maPhieuTra; }
    public String getMaDocGia() { return maDocGia; }
    public String getMaNhanVienNhan() { return maNhanVienNhan; }
    public String getMaChiNhanh() { return maChiNhanh; }
    public LocalDateTime getNgayTra() { return ngayTra; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public List<KhoanNoTraResponse> getKhoanNo() { return khoanNo; }
    public PrintPayload getPrintPayload() { return printPayload; }
    public PaymentSuggestion getPaymentSuggestion() { return paymentSuggestion; }
    public List<ChiTietTraResponse> getChiTiet() { return chiTiet; }

    public static class KhoanNoTraResponse {
        private final String maKhoanNo;
        private final String maChiTietTra;
        private final String maLoaiKhoanNo;
        private final BigDecimal soTienPhatSinh;
        private final String lyDo;

        public KhoanNoTraResponse(
                String maKhoanNo,
                String maChiTietTra,
                String maLoaiKhoanNo,
                BigDecimal soTienPhatSinh,
                String lyDo
        ) {
            this.maKhoanNo = maKhoanNo;
            this.maChiTietTra = maChiTietTra;
            this.maLoaiKhoanNo = maLoaiKhoanNo;
            this.soTienPhatSinh = soTienPhatSinh;
            this.lyDo = lyDo;
        }

        public String getMaKhoanNo() { return maKhoanNo; }
        public String getMaChiTietTra() { return maChiTietTra; }
        public String getMaLoaiKhoanNo() { return maLoaiKhoanNo; }
        public BigDecimal getSoTienPhatSinh() { return soTienPhatSinh; }
        public String getLyDo() { return lyDo; }
    }

    public static class PaymentSuggestion {
        private final boolean hasDebt;
        private final String readerId;
        private final BigDecimal totalDebt;
        private final List<String> debtIds;

        public PaymentSuggestion(boolean hasDebt, String readerId, BigDecimal totalDebt, List<String> debtIds) {
            this.hasDebt = hasDebt;
            this.readerId = readerId;
            this.totalDebt = totalDebt;
            this.debtIds = debtIds;
        }

        public boolean isHasDebt() { return hasDebt; }
        public String getReaderId() { return readerId; }
        public BigDecimal getTotalDebt() { return totalDebt; }
        public List<String> getDebtIds() { return debtIds; }
    }

    public static class PrintPayload {
        private final String title;
        private final String returnId;
        private final String readerId;
        private final String staffId;
        private final String branchId;
        private final LocalDateTime returnedAt;
        private final BigDecimal totalFine;

        public PrintPayload(
                String title,
                String returnId,
                String readerId,
                String staffId,
                String branchId,
                LocalDateTime returnedAt,
                BigDecimal totalFine
        ) {
            this.title = title;
            this.returnId = returnId;
            this.readerId = readerId;
            this.staffId = staffId;
            this.branchId = branchId;
            this.returnedAt = returnedAt;
            this.totalFine = totalFine;
        }

        public String getTitle() { return title; }
        public String getReturnId() { return returnId; }
        public String getReaderId() { return readerId; }
        public String getStaffId() { return staffId; }
        public String getBranchId() { return branchId; }
        public LocalDateTime getReturnedAt() { return returnedAt; }
        public BigDecimal getTotalFine() { return totalFine; }
    }

    public static class ChiTietTraResponse {
        private String maChiTietTra;
        private String maChiTietMuon;
        private String tinhTrangKhiTra;
        private Integer soNgayTre;
        private BigDecimal tienPhatTre;
        private BigDecimal tienPhatHongMat;

        public ChiTietTraResponse(
                String maChiTietTra,
                String maChiTietMuon,
                String tinhTrangKhiTra,
                Integer soNgayTre,
                BigDecimal tienPhatTre,
                BigDecimal tienPhatHongMat
        ) {
            this.maChiTietTra = maChiTietTra;
            this.maChiTietMuon = maChiTietMuon;
            this.tinhTrangKhiTra = tinhTrangKhiTra;
            this.soNgayTre = soNgayTre;
            this.tienPhatTre = tienPhatTre;
            this.tienPhatHongMat = tienPhatHongMat;
        }

        public String getMaChiTietTra() { return maChiTietTra; }
        public String getMaChiTietMuon() { return maChiTietMuon; }
        public String getTinhTrangKhiTra() { return tinhTrangKhiTra; }
        public Integer getSoNgayTre() { return soNgayTre; }
        public BigDecimal getTienPhatTre() { return tienPhatTre; }
        public BigDecimal getTienPhatHongMat() { return tienPhatHongMat; }
    }
}
