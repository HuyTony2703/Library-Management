package com.library.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public class MuonSachResponse {

    private final String maPhieuMuon;
    private final String maDocGia;
    private final String maNhanVienLap;
    private final String maChiNhanh;
    private final String maPhienBanQuyDinh;
    private final LocalDateTime ngayMuon;
    private final String trangThai;
    private final String ghiChu;
    private final String idempotencyKey;
    private final List<ChiTietMuonResponse> chiTiet;
    private final PrintData printData;

    public MuonSachResponse(
            String maPhieuMuon,
            String maDocGia,
            String maNhanVienLap,
            String maChiNhanh,
            String maPhienBanQuyDinh,
            LocalDateTime ngayMuon,
            String trangThai,
            List<ChiTietMuonResponse> chiTiet
    ) {
        this(maPhieuMuon, maDocGia, maNhanVienLap, maChiNhanh, maPhienBanQuyDinh,
                ngayMuon, trangThai, null, null, chiTiet, null);
    }

    public MuonSachResponse(
            String maPhieuMuon,
            String maDocGia,
            String maNhanVienLap,
            String maChiNhanh,
            String maPhienBanQuyDinh,
            LocalDateTime ngayMuon,
            String trangThai,
            String ghiChu,
            String idempotencyKey,
            List<ChiTietMuonResponse> chiTiet,
            PrintData printData
    ) {
        this.maPhieuMuon = maPhieuMuon;
        this.maDocGia = maDocGia;
        this.maNhanVienLap = maNhanVienLap;
        this.maChiNhanh = maChiNhanh;
        this.maPhienBanQuyDinh = maPhienBanQuyDinh;
        this.ngayMuon = ngayMuon;
        this.trangThai = trangThai;
        this.ghiChu = ghiChu;
        this.idempotencyKey = idempotencyKey;
        this.chiTiet = chiTiet;
        this.printData = printData;
    }

    public String getMaPhieuMuon() { return maPhieuMuon; }
    public String getMaDocGia() { return maDocGia; }
    public String getMaNhanVienLap() { return maNhanVienLap; }
    public String getMaChiNhanh() { return maChiNhanh; }
    public String getMaPhienBanQuyDinh() { return maPhienBanQuyDinh; }
    public LocalDateTime getNgayMuon() { return ngayMuon; }
    public String getTrangThai() { return trangThai; }
    public String getGhiChu() { return ghiChu; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public List<ChiTietMuonResponse> getChiTiet() { return chiTiet; }
    public PrintData getPrintData() { return printData; }

    public String getLoanId() { return maPhieuMuon; }
    public String getReaderId() { return maDocGia; }
    public String getStaffId() { return maNhanVienLap; }
    public String getBranchId() { return maChiNhanh; }
    public LocalDateTime getCreatedAt() { return ngayMuon; }
    public String getStatus() { return trangThai; }
    public String getNote() { return ghiChu; }
    public List<ChiTietMuonResponse> getItems() { return chiTiet; }

    public static class ChiTietMuonResponse {
        private final String maChiTietMuon;
        private final String maCuonSach;
        private final String maQuyDinhMuon;
        private final LocalDateTime ngayMuon;
        private final LocalDateTime hanTra;
        private final String trangThai;

        public ChiTietMuonResponse(
                String maChiTietMuon,
                String maCuonSach,
                String maQuyDinhMuon,
                LocalDateTime ngayMuon,
                LocalDateTime hanTra,
                String trangThai
        ) {
            this.maChiTietMuon = maChiTietMuon;
            this.maCuonSach = maCuonSach;
            this.maQuyDinhMuon = maQuyDinhMuon;
            this.ngayMuon = ngayMuon;
            this.hanTra = hanTra;
            this.trangThai = trangThai;
        }

        public String getMaChiTietMuon() { return maChiTietMuon; }
        public String getMaCuonSach() { return maCuonSach; }
        public String getMaQuyDinhMuon() { return maQuyDinhMuon; }
        public LocalDateTime getNgayMuon() { return ngayMuon; }
        public LocalDateTime getHanTra() { return hanTra; }
        public String getTrangThai() { return trangThai; }

        public String getLoanDetailId() { return maChiTietMuon; }
        public String getCopyId() { return maCuonSach; }
        public String getRuleId() { return maQuyDinhMuon; }
        public LocalDateTime getBorrowedAt() { return ngayMuon; }
        public LocalDateTime getDueAt() { return hanTra; }
        public String getStatus() { return trangThai; }
    }

    public record PrintData(
            String loanId,
            String readerId,
            String staffId,
            String branchId,
            LocalDateTime createdAt,
            String note,
            List<ChiTietMuonResponse> items
    ) {
    }
}
