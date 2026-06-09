package com.library.backend.dto.reader;

public class ReaderBorrowRuleResponse {

    private final String maGoiThanhVien;
    private final String tenGoi;
    private final String maTheLoai;
    private final String tenTheLoai;
    private final Integer soNgayMuon;
    private final Integer soNgayGiaHanMoiLan;

    public ReaderBorrowRuleResponse(
            String maGoiThanhVien,
            String tenGoi,
            String maTheLoai,
            String tenTheLoai,
            Integer soNgayMuon,
            Integer soNgayGiaHanMoiLan
    ) {
        this.maGoiThanhVien = maGoiThanhVien;
        this.tenGoi = tenGoi;
        this.maTheLoai = maTheLoai;
        this.tenTheLoai = tenTheLoai;
        this.soNgayMuon = soNgayMuon;
        this.soNgayGiaHanMoiLan = soNgayGiaHanMoiLan;
    }

    public String getMaGoiThanhVien() { return maGoiThanhVien; }
    public String getTenGoi() { return tenGoi; }
    public String getMaTheLoai() { return maTheLoai; }
    public String getTenTheLoai() { return tenTheLoai; }
    public Integer getSoNgayMuon() { return soNgayMuon; }
    public Integer getSoNgayGiaHanMoiLan() { return soNgayGiaHanMoiLan; }
}
