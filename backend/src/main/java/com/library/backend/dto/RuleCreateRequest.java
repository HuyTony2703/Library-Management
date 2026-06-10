package com.library.backend.dto;

import java.math.BigDecimal;
import java.util.List;

public class RuleCreateRequest {

    private String maPhienBan;
    private String tenPhienBan;
    private String maNhanVienThayDoi;
    private String ghiChu;

    private SystemParameterRequest thamSo;
    private List<MembershipPriceRuleRequest> giaGoiTheoNhom;
    private List<PackageBorrowRuleRequest> quyDinhGoi;
    private List<CategoryBorrowRuleRequest> quyDinhMuonTheoTheLoai;

    public String getMaPhienBan() { return maPhienBan; }
    public void setMaPhienBan(String maPhienBan) { this.maPhienBan = maPhienBan; }

    public String getTenPhienBan() { return tenPhienBan; }
    public void setTenPhienBan(String tenPhienBan) { this.tenPhienBan = tenPhienBan; }

    public String getMaNhanVienThayDoi() { return maNhanVienThayDoi; }
    public void setMaNhanVienThayDoi(String maNhanVienThayDoi) { this.maNhanVienThayDoi = maNhanVienThayDoi; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public SystemParameterRequest getThamSo() { return thamSo; }
    public void setThamSo(SystemParameterRequest thamSo) { this.thamSo = thamSo; }

    public List<MembershipPriceRuleRequest> getGiaGoiTheoNhom() { return giaGoiTheoNhom; }
    public void setGiaGoiTheoNhom(List<MembershipPriceRuleRequest> giaGoiTheoNhom) { this.giaGoiTheoNhom = giaGoiTheoNhom; }

    public List<PackageBorrowRuleRequest> getQuyDinhGoi() { return quyDinhGoi; }
    public void setQuyDinhGoi(List<PackageBorrowRuleRequest> quyDinhGoi) { this.quyDinhGoi = quyDinhGoi; }

    public List<CategoryBorrowRuleRequest> getQuyDinhMuonTheoTheLoai() { return quyDinhMuonTheoTheLoai; }
    public void setQuyDinhMuonTheoTheLoai(List<CategoryBorrowRuleRequest> quyDinhMuonTheoTheLoai) {
        this.quyDinhMuonTheoTheLoai = quyDinhMuonTheoTheLoai;
    }

    public static class SystemParameterRequest {
        private String maThamSo;
        private Integer tuoiToiThieu;
        private Integer tuoiToiDa;
        private Integer thoiHanTheTheoThang;
        private Integer khoangCachNamXuatBan;
        private Integer soNgayNhacTruocHan;
        private Integer soNgayGiuDatTruoc;
        private BigDecimal mucPhatTreMoiNgay;

        public String getMaThamSo() { return maThamSo; }
        public void setMaThamSo(String maThamSo) { this.maThamSo = maThamSo; }

        public Integer getTuoiToiThieu() { return tuoiToiThieu; }
        public void setTuoiToiThieu(Integer tuoiToiThieu) { this.tuoiToiThieu = tuoiToiThieu; }

        public Integer getTuoiToiDa() { return tuoiToiDa; }
        public void setTuoiToiDa(Integer tuoiToiDa) { this.tuoiToiDa = tuoiToiDa; }

        public Integer getThoiHanTheTheoThang() { return thoiHanTheTheoThang; }
        public void setThoiHanTheTheoThang(Integer thoiHanTheTheoThang) { this.thoiHanTheTheoThang = thoiHanTheTheoThang; }

        public Integer getKhoangCachNamXuatBan() { return khoangCachNamXuatBan; }
        public void setKhoangCachNamXuatBan(Integer khoangCachNamXuatBan) { this.khoangCachNamXuatBan = khoangCachNamXuatBan; }

        public Integer getSoNgayNhacTruocHan() { return soNgayNhacTruocHan; }
        public void setSoNgayNhacTruocHan(Integer soNgayNhacTruocHan) { this.soNgayNhacTruocHan = soNgayNhacTruocHan; }

        public Integer getSoNgayGiuDatTruoc() { return soNgayGiuDatTruoc; }
        public void setSoNgayGiuDatTruoc(Integer soNgayGiuDatTruoc) { this.soNgayGiuDatTruoc = soNgayGiuDatTruoc; }

        public BigDecimal getMucPhatTreMoiNgay() { return mucPhatTreMoiNgay; }
        public void setMucPhatTreMoiNgay(BigDecimal mucPhatTreMoiNgay) { this.mucPhatTreMoiNgay = mucPhatTreMoiNgay; }
    }

    public static class MembershipPriceRuleRequest {
        private String maGiaGoi;
        private String maGoiThanhVien;
        private String maNhomDocGia;
        private BigDecimal giaTien;
        private Integer thoiHanGoiTheoNgay;

        public String getMaGiaGoi() { return maGiaGoi; }
        public void setMaGiaGoi(String maGiaGoi) { this.maGiaGoi = maGiaGoi; }

        public String getMaGoiThanhVien() { return maGoiThanhVien; }
        public void setMaGoiThanhVien(String maGoiThanhVien) { this.maGoiThanhVien = maGoiThanhVien; }

        public String getMaNhomDocGia() { return maNhomDocGia; }
        public void setMaNhomDocGia(String maNhomDocGia) { this.maNhomDocGia = maNhomDocGia; }

        public BigDecimal getGiaTien() { return giaTien; }
        public void setGiaTien(BigDecimal giaTien) { this.giaTien = giaTien; }

        public Integer getThoiHanGoiTheoNgay() { return thoiHanGoiTheoNgay; }
        public void setThoiHanGoiTheoNgay(Integer thoiHanGoiTheoNgay) { this.thoiHanGoiTheoNgay = thoiHanGoiTheoNgay; }
    }

    public static class PackageBorrowRuleRequest {
        private String maQuyDinhGoi;
        private String maGoiThanhVien;
        private Integer soSachMuonToiDa;
        private Integer soLanGiaHanToiDa;

        public String getMaQuyDinhGoi() { return maQuyDinhGoi; }
        public void setMaQuyDinhGoi(String maQuyDinhGoi) { this.maQuyDinhGoi = maQuyDinhGoi; }

        public String getMaGoiThanhVien() { return maGoiThanhVien; }
        public void setMaGoiThanhVien(String maGoiThanhVien) { this.maGoiThanhVien = maGoiThanhVien; }

        public Integer getSoSachMuonToiDa() { return soSachMuonToiDa; }
        public void setSoSachMuonToiDa(Integer soSachMuonToiDa) { this.soSachMuonToiDa = soSachMuonToiDa; }

        public Integer getSoLanGiaHanToiDa() { return soLanGiaHanToiDa; }
        public void setSoLanGiaHanToiDa(Integer soLanGiaHanToiDa) { this.soLanGiaHanToiDa = soLanGiaHanToiDa; }
    }

    public static class CategoryBorrowRuleRequest {
        private String maQuyDinhMuon;
        private String maGoiThanhVien;
        private String maTheLoai;
        private Integer soNgayMuon;
        private Integer soNgayGiaHanMoiLan;

        public String getMaQuyDinhMuon() { return maQuyDinhMuon; }
        public void setMaQuyDinhMuon(String maQuyDinhMuon) { this.maQuyDinhMuon = maQuyDinhMuon; }

        public String getMaGoiThanhVien() { return maGoiThanhVien; }
        public void setMaGoiThanhVien(String maGoiThanhVien) { this.maGoiThanhVien = maGoiThanhVien; }

        public String getMaTheLoai() { return maTheLoai; }
        public void setMaTheLoai(String maTheLoai) { this.maTheLoai = maTheLoai; }

        public Integer getSoNgayMuon() { return soNgayMuon; }
        public void setSoNgayMuon(Integer soNgayMuon) { this.soNgayMuon = soNgayMuon; }

        public Integer getSoNgayGiaHanMoiLan() { return soNgayGiaHanMoiLan; }
        public void setSoNgayGiaHanMoiLan(Integer soNgayGiaHanMoiLan) { this.soNgayGiaHanMoiLan = soNgayGiaHanMoiLan; }
    }
}
