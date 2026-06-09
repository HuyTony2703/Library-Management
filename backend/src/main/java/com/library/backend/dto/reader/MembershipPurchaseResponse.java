package com.library.backend.dto.reader;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MembershipPurchaseResponse {

    private final String maLichSuGoi;
    private final String maDocGia;
    private final String maGoiThanhVien;
    private final String tenGoi;
    private final String maPhieuThu;
    private final String maPhuongThuc;
    private final BigDecimal soTienThu;
    private final LocalDate ngayBatDau;
    private final LocalDate ngayKetThuc;
    private final String trangThai;
    private final String ghiChu;

    public MembershipPurchaseResponse(
            String maLichSuGoi,
            String maDocGia,
            String maGoiThanhVien,
            String tenGoi,
            String maPhieuThu,
            String maPhuongThuc,
            BigDecimal soTienThu,
            LocalDate ngayBatDau,
            LocalDate ngayKetThuc,
            String trangThai,
            String ghiChu
    ) {
        this.maLichSuGoi = maLichSuGoi;
        this.maDocGia = maDocGia;
        this.maGoiThanhVien = maGoiThanhVien;
        this.tenGoi = tenGoi;
        this.maPhieuThu = maPhieuThu;
        this.maPhuongThuc = maPhuongThuc;
        this.soTienThu = soTienThu;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.trangThai = trangThai;
        this.ghiChu = ghiChu;
    }

    public String getMaLichSuGoi() { return maLichSuGoi; }
    public String getMaDocGia() { return maDocGia; }
    public String getMaGoiThanhVien() { return maGoiThanhVien; }
    public String getTenGoi() { return tenGoi; }
    public String getMaPhieuThu() { return maPhieuThu; }
    public String getMaPhuongThuc() { return maPhuongThuc; }
    public BigDecimal getSoTienThu() { return soTienThu; }
    public LocalDate getNgayBatDau() { return ngayBatDau; }
    public LocalDate getNgayKetThuc() { return ngayKetThuc; }
    public String getTrangThai() { return trangThai; }
    public String getGhiChu() { return ghiChu; }
}
