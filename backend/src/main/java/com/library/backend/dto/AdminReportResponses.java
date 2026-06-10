package com.library.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AdminReportResponses {

    public static class OverviewResponse {
        private final Long totalBookCopies;
        private final Long availableBookCopies;
        private final Long borrowedBookCopies;
        private final Long activeReaders;
        private final Long loansThisMonth;
        private final Long lateReturnsThisMonth;
        private final BigDecimal totalDebt;
        private final BigDecimal paymentsThisMonth;

        public OverviewResponse(
                Long totalBookCopies,
                Long availableBookCopies,
                Long borrowedBookCopies,
                Long activeReaders,
                Long loansThisMonth,
                Long lateReturnsThisMonth,
                BigDecimal totalDebt,
                BigDecimal paymentsThisMonth
        ) {
            this.totalBookCopies = totalBookCopies;
            this.availableBookCopies = availableBookCopies;
            this.borrowedBookCopies = borrowedBookCopies;
            this.activeReaders = activeReaders;
            this.loansThisMonth = loansThisMonth;
            this.lateReturnsThisMonth = lateReturnsThisMonth;
            this.totalDebt = totalDebt;
            this.paymentsThisMonth = paymentsThisMonth;
        }

        public Long getTotalBookCopies() { return totalBookCopies; }
        public Long getAvailableBookCopies() { return availableBookCopies; }
        public Long getBorrowedBookCopies() { return borrowedBookCopies; }
        public Long getActiveReaders() { return activeReaders; }
        public Long getLoansThisMonth() { return loansThisMonth; }
        public Long getLateReturnsThisMonth() { return lateReturnsThisMonth; }
        public BigDecimal getTotalDebt() { return totalDebt; }
        public BigDecimal getPaymentsThisMonth() { return paymentsThisMonth; }
    }

    public static class DebtReportResponse {
        private final String maDocGia;
        private final String hoTen;
        private final BigDecimal tongNoConLai;

        public DebtReportResponse(String maDocGia, String hoTen, BigDecimal tongNoConLai) {
            this.maDocGia = maDocGia;
            this.hoTen = hoTen;
            this.tongNoConLai = tongNoConLai;
        }

        public String getMaDocGia() { return maDocGia; }
        public String getHoTen() { return hoTen; }
        public BigDecimal getTongNoConLai() { return tongNoConLai; }
    }

    public static class CurrentLoanReportResponse {
        private final String maChiTietMuon;
        private final String maPhieuMuon;
        private final String maDocGia;
        private final String hoTenDocGia;
        private final String maCuonSach;
        private final String tenDauSach;
        private final LocalDateTime ngayMuon;
        private final LocalDateTime hanTra;
        private final Integer soNgayConLai;

        public CurrentLoanReportResponse(
                String maChiTietMuon,
                String maPhieuMuon,
                String maDocGia,
                String hoTenDocGia,
                String maCuonSach,
                String tenDauSach,
                LocalDateTime ngayMuon,
                LocalDateTime hanTra,
                Integer soNgayConLai
        ) {
            this.maChiTietMuon = maChiTietMuon;
            this.maPhieuMuon = maPhieuMuon;
            this.maDocGia = maDocGia;
            this.hoTenDocGia = hoTenDocGia;
            this.maCuonSach = maCuonSach;
            this.tenDauSach = tenDauSach;
            this.ngayMuon = ngayMuon;
            this.hanTra = hanTra;
            this.soNgayConLai = soNgayConLai;
        }

        public String getMaChiTietMuon() { return maChiTietMuon; }
        public String getMaPhieuMuon() { return maPhieuMuon; }
        public String getMaDocGia() { return maDocGia; }
        public String getHoTenDocGia() { return hoTenDocGia; }
        public String getMaCuonSach() { return maCuonSach; }
        public String getTenDauSach() { return tenDauSach; }
        public LocalDateTime getNgayMuon() { return ngayMuon; }
        public LocalDateTime getHanTra() { return hanTra; }
        public Integer getSoNgayConLai() { return soNgayConLai; }
    }

    public static class BorrowByCategoryReportResponse {
        private final Integer thang;
        private final Integer nam;
        private final String maTheLoai;
        private final String tenTheLoai;
        private final Long soLuotMuon;
        private final BigDecimal tiLePhanTram;

        public BorrowByCategoryReportResponse(
                Integer thang,
                Integer nam,
                String maTheLoai,
                String tenTheLoai,
                Long soLuotMuon,
                BigDecimal tiLePhanTram
        ) {
            this.thang = thang;
            this.nam = nam;
            this.maTheLoai = maTheLoai;
            this.tenTheLoai = tenTheLoai;
            this.soLuotMuon = soLuotMuon;
            this.tiLePhanTram = tiLePhanTram;
        }

        public Integer getThang() { return thang; }
        public Integer getNam() { return nam; }
        public String getMaTheLoai() { return maTheLoai; }
        public String getTenTheLoai() { return tenTheLoai; }
        public Long getSoLuotMuon() { return soLuotMuon; }
        public BigDecimal getTiLePhanTram() { return tiLePhanTram; }
    }

    public static class LateReturnReportResponse {
        private final Integer thang;
        private final Integer nam;
        private final String maCuonSach;
        private final String tenDauSach;
        private final String maDocGia;
        private final String hoTenDocGia;
        private final LocalDateTime ngayMuon;
        private final LocalDateTime hanTra;
        private final LocalDateTime ngayTraThucTe;
        private final Integer soNgayTre;
        private final BigDecimal tienPhatTre;

        public LateReturnReportResponse(
                Integer thang,
                Integer nam,
                String maCuonSach,
                String tenDauSach,
                String maDocGia,
                String hoTenDocGia,
                LocalDateTime ngayMuon,
                LocalDateTime hanTra,
                LocalDateTime ngayTraThucTe,
                Integer soNgayTre,
                BigDecimal tienPhatTre
        ) {
            this.thang = thang;
            this.nam = nam;
            this.maCuonSach = maCuonSach;
            this.tenDauSach = tenDauSach;
            this.maDocGia = maDocGia;
            this.hoTenDocGia = hoTenDocGia;
            this.ngayMuon = ngayMuon;
            this.hanTra = hanTra;
            this.ngayTraThucTe = ngayTraThucTe;
            this.soNgayTre = soNgayTre;
            this.tienPhatTre = tienPhatTre;
        }

        public Integer getThang() { return thang; }
        public Integer getNam() { return nam; }
        public String getMaCuonSach() { return maCuonSach; }
        public String getTenDauSach() { return tenDauSach; }
        public String getMaDocGia() { return maDocGia; }
        public String getHoTenDocGia() { return hoTenDocGia; }
        public LocalDateTime getNgayMuon() { return ngayMuon; }
        public LocalDateTime getHanTra() { return hanTra; }
        public LocalDateTime getNgayTraThucTe() { return ngayTraThucTe; }
        public Integer getSoNgayTre() { return soNgayTre; }
        public BigDecimal getTienPhatTre() { return tienPhatTre; }
    }

    public static class PaymentReportResponse {
        private final String maPhieuThu;
        private final String maDocGia;
        private final String hoTenDocGia;
        private final String maNhanVienThu;
        private final String tenPhuongThuc;
        private final String loaiThu;
        private final BigDecimal soTienThu;
        private final LocalDateTime ngayThu;
        private final String trangThai;

        public PaymentReportResponse(
                String maPhieuThu,
                String maDocGia,
                String hoTenDocGia,
                String maNhanVienThu,
                String tenPhuongThuc,
                String loaiThu,
                BigDecimal soTienThu,
                LocalDateTime ngayThu,
                String trangThai
        ) {
            this.maPhieuThu = maPhieuThu;
            this.maDocGia = maDocGia;
            this.hoTenDocGia = hoTenDocGia;
            this.maNhanVienThu = maNhanVienThu;
            this.tenPhuongThuc = tenPhuongThuc;
            this.loaiThu = loaiThu;
            this.soTienThu = soTienThu;
            this.ngayThu = ngayThu;
            this.trangThai = trangThai;
        }

        public String getMaPhieuThu() { return maPhieuThu; }
        public String getMaDocGia() { return maDocGia; }
        public String getHoTenDocGia() { return hoTenDocGia; }
        public String getMaNhanVienThu() { return maNhanVienThu; }
        public String getTenPhuongThuc() { return tenPhuongThuc; }
        public String getLoaiThu() { return loaiThu; }
        public BigDecimal getSoTienThu() { return soTienThu; }
        public LocalDateTime getNgayThu() { return ngayThu; }
        public String getTrangThai() { return trangThai; }
    }
}
