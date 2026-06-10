package com.library.backend.dto.reader;

import java.time.LocalDateTime;

public class ReaderCurrentLoanResponse {

    private final String maChiTietMuon;
    private final String maPhieuMuon;
    private final String maCuonSach;
    private final String maDauSach;
    private final String tenDauSach;
    private final String anhBia;
    private final LocalDateTime ngayMuon;
    private final LocalDateTime hanTra;
    private final String trangThai;
    private final String maChiNhanh;
    private final String tenChiNhanh;
    private final Integer soNgayGiaHanMoiLan;
    private final Integer soLanGiaHanToiDa;
    private final Integer soLanDaGiaHan;
    private final Boolean coTheGiaHan;
    private final String lyDoKhongTheGiaHan;

    public ReaderCurrentLoanResponse(
            String maChiTietMuon,
            String maPhieuMuon,
            String maCuonSach,
            String maDauSach,
            String tenDauSach,
            String anhBia,
            LocalDateTime ngayMuon,
            LocalDateTime hanTra,
            String trangThai,
            String maChiNhanh,
            String tenChiNhanh,
            Integer soNgayGiaHanMoiLan,
            Integer soLanGiaHanToiDa,
            Integer soLanDaGiaHan,
            Boolean coTheGiaHan,
            String lyDoKhongTheGiaHan
    ) {
        this.maChiTietMuon = maChiTietMuon;
        this.maPhieuMuon = maPhieuMuon;
        this.maCuonSach = maCuonSach;
        this.maDauSach = maDauSach;
        this.tenDauSach = tenDauSach;
        this.anhBia = anhBia;
        this.ngayMuon = ngayMuon;
        this.hanTra = hanTra;
        this.trangThai = trangThai;
        this.maChiNhanh = maChiNhanh;
        this.tenChiNhanh = tenChiNhanh;
        this.soNgayGiaHanMoiLan = soNgayGiaHanMoiLan;
        this.soLanGiaHanToiDa = soLanGiaHanToiDa;
        this.soLanDaGiaHan = soLanDaGiaHan;
        this.coTheGiaHan = coTheGiaHan;
        this.lyDoKhongTheGiaHan = lyDoKhongTheGiaHan;
    }

    public String getMaChiTietMuon() { return maChiTietMuon; }
    public String getMaPhieuMuon() { return maPhieuMuon; }
    public String getMaCuonSach() { return maCuonSach; }
    public String getMaDauSach() { return maDauSach; }
    public String getTenDauSach() { return tenDauSach; }
    public String getAnhBia() { return anhBia; }
    public LocalDateTime getNgayMuon() { return ngayMuon; }
    public LocalDateTime getHanTra() { return hanTra; }
    public String getTrangThai() { return trangThai; }
    public String getMaChiNhanh() { return maChiNhanh; }
    public String getTenChiNhanh() { return tenChiNhanh; }
    public Integer getSoNgayGiaHanMoiLan() { return soNgayGiaHanMoiLan; }
    public Integer getSoLanGiaHanToiDa() { return soLanGiaHanToiDa; }
    public Integer getSoLanDaGiaHan() { return soLanDaGiaHan; }
    public Boolean getCoTheGiaHan() { return coTheGiaHan; }
    public String getLyDoKhongTheGiaHan() { return lyDoKhongTheGiaHan; }
}

