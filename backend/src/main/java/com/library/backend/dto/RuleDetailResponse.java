package com.library.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class RuleDetailResponse {

    private String maPhienBan;
    private String tenPhienBan;
    private LocalDateTime ngayApDung;
    private String maNhanVienThayDoi;
    private String ghiChu;
    private String trangThai;

    private SystemParameterResponse thamSo;
    private List<MembershipPriceRuleResponse> giaGoiTheoNhom;
    private List<PackageBorrowRuleResponse> quyDinhGoi;
    private List<CategoryBorrowRuleResponse> quyDinhMuonTheoTheLoai;

    public RuleDetailResponse(
            String maPhienBan,
            String tenPhienBan,
            LocalDateTime ngayApDung,
            String maNhanVienThayDoi,
            String ghiChu,
            String trangThai,
            SystemParameterResponse thamSo,
            List<MembershipPriceRuleResponse> giaGoiTheoNhom,
            List<PackageBorrowRuleResponse> quyDinhGoi,
            List<CategoryBorrowRuleResponse> quyDinhMuonTheoTheLoai
    ) {
        this.maPhienBan = maPhienBan;
        this.tenPhienBan = tenPhienBan;
        this.ngayApDung = ngayApDung;
        this.maNhanVienThayDoi = maNhanVienThayDoi;
        this.ghiChu = ghiChu;
        this.trangThai = trangThai;
        this.thamSo = thamSo;
        this.giaGoiTheoNhom = giaGoiTheoNhom;
        this.quyDinhGoi = quyDinhGoi;
        this.quyDinhMuonTheoTheLoai = quyDinhMuonTheoTheLoai;
    }

    public String getMaPhienBan() { return maPhienBan; }
    public String getTenPhienBan() { return tenPhienBan; }
    public LocalDateTime getNgayApDung() { return ngayApDung; }
    public String getMaNhanVienThayDoi() { return maNhanVienThayDoi; }
    public String getGhiChu() { return ghiChu; }
    public String getTrangThai() { return trangThai; }
    public SystemParameterResponse getThamSo() { return thamSo; }
    public List<MembershipPriceRuleResponse> getGiaGoiTheoNhom() { return giaGoiTheoNhom; }
    public List<PackageBorrowRuleResponse> getQuyDinhGoi() { return quyDinhGoi; }
    public List<CategoryBorrowRuleResponse> getQuyDinhMuonTheoTheLoai() { return quyDinhMuonTheoTheLoai; }

    public static class SystemParameterResponse {
        private String maThamSo;
        private Integer tuoiToiThieu;
        private Integer tuoiToiDa;
        private Integer thoiHanTheTheoThang;
        private Integer khoangCachNamXuatBan;
        private Integer soNgayNhacTruocHan;
        private Integer soNgayGiuDatTruoc;
        private BigDecimal mucPhatTreMoiNgay;

        public SystemParameterResponse(
                String maThamSo,
                Integer tuoiToiThieu,
                Integer tuoiToiDa,
                Integer thoiHanTheTheoThang,
                Integer khoangCachNamXuatBan,
                Integer soNgayNhacTruocHan,
                Integer soNgayGiuDatTruoc,
                BigDecimal mucPhatTreMoiNgay
        ) {
            this.maThamSo = maThamSo;
            this.tuoiToiThieu = tuoiToiThieu;
            this.tuoiToiDa = tuoiToiDa;
            this.thoiHanTheTheoThang = thoiHanTheTheoThang;
            this.khoangCachNamXuatBan = khoangCachNamXuatBan;
            this.soNgayNhacTruocHan = soNgayNhacTruocHan;
            this.soNgayGiuDatTruoc = soNgayGiuDatTruoc;
            this.mucPhatTreMoiNgay = mucPhatTreMoiNgay;
        }

        public String getMaThamSo() { return maThamSo; }
        public Integer getTuoiToiThieu() { return tuoiToiThieu; }
        public Integer getTuoiToiDa() { return tuoiToiDa; }
        public Integer getThoiHanTheTheoThang() { return thoiHanTheTheoThang; }
        public Integer getKhoangCachNamXuatBan() { return khoangCachNamXuatBan; }
        public Integer getSoNgayNhacTruocHan() { return soNgayNhacTruocHan; }
        public Integer getSoNgayGiuDatTruoc() { return soNgayGiuDatTruoc; }
        public BigDecimal getMucPhatTreMoiNgay() { return mucPhatTreMoiNgay; }
    }

    public static class MembershipPriceRuleResponse {
        private String maGiaGoi;
        private String maGoiThanhVien;
        private String maNhomDocGia;
        private BigDecimal giaTien;
        private Integer thoiHanGoiTheoNgay;

        public MembershipPriceRuleResponse(String maGiaGoi, String maGoiThanhVien, String maNhomDocGia, BigDecimal giaTien, Integer thoiHanGoiTheoNgay) {
            this.maGiaGoi = maGiaGoi;
            this.maGoiThanhVien = maGoiThanhVien;
            this.maNhomDocGia = maNhomDocGia;
            this.giaTien = giaTien;
            this.thoiHanGoiTheoNgay = thoiHanGoiTheoNgay;
        }

        public String getMaGiaGoi() { return maGiaGoi; }
        public String getMaGoiThanhVien() { return maGoiThanhVien; }
        public String getMaNhomDocGia() { return maNhomDocGia; }
        public BigDecimal getGiaTien() { return giaTien; }
        public Integer getThoiHanGoiTheoNgay() { return thoiHanGoiTheoNgay; }
    }

    public static class PackageBorrowRuleResponse {
        private String maQuyDinhGoi;
        private String maGoiThanhVien;
        private Integer soSachMuonToiDa;
        private Integer soLanGiaHanToiDa;

        public PackageBorrowRuleResponse(String maQuyDinhGoi, String maGoiThanhVien, Integer soSachMuonToiDa, Integer soLanGiaHanToiDa) {
            this.maQuyDinhGoi = maQuyDinhGoi;
            this.maGoiThanhVien = maGoiThanhVien;
            this.soSachMuonToiDa = soSachMuonToiDa;
            this.soLanGiaHanToiDa = soLanGiaHanToiDa;
        }

        public String getMaQuyDinhGoi() { return maQuyDinhGoi; }
        public String getMaGoiThanhVien() { return maGoiThanhVien; }
        public Integer getSoSachMuonToiDa() { return soSachMuonToiDa; }
        public Integer getSoLanGiaHanToiDa() { return soLanGiaHanToiDa; }
    }

    public static class CategoryBorrowRuleResponse {
        private String maQuyDinhMuon;
        private String maGoiThanhVien;
        private String maTheLoai;
        private Integer soNgayMuon;
        private Integer soNgayGiaHanMoiLan;

        public CategoryBorrowRuleResponse(String maQuyDinhMuon, String maGoiThanhVien, String maTheLoai, Integer soNgayMuon, Integer soNgayGiaHanMoiLan) {
            this.maQuyDinhMuon = maQuyDinhMuon;
            this.maGoiThanhVien = maGoiThanhVien;
            this.maTheLoai = maTheLoai;
            this.soNgayMuon = soNgayMuon;
            this.soNgayGiaHanMoiLan = soNgayGiaHanMoiLan;
        }

        public String getMaQuyDinhMuon() { return maQuyDinhMuon; }
        public String getMaGoiThanhVien() { return maGoiThanhVien; }
        public String getMaTheLoai() { return maTheLoai; }
        public Integer getSoNgayMuon() { return soNgayMuon; }
        public Integer getSoNgayGiaHanMoiLan() { return soNgayGiaHanMoiLan; }
    }
}
