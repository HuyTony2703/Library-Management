package com.library.backend.dto.reader;

import java.math.BigDecimal;

public class MembershipPlanResponse {

    private final String maGoiThanhVien;
    private final String tenGoi;
    private final String moTa;
    private final String trangThai;
    private final BigDecimal giaTien;
    private final Integer thoiHanGoiTheoNgay;
    private final boolean goiHienTai;

    public MembershipPlanResponse(
            String maGoiThanhVien,
            String tenGoi,
            String moTa,
            String trangThai,
            BigDecimal giaTien,
            Integer thoiHanGoiTheoNgay,
            boolean goiHienTai
    ) {
        this.maGoiThanhVien = maGoiThanhVien;
        this.tenGoi = tenGoi;
        this.moTa = moTa;
        this.trangThai = trangThai;
        this.giaTien = giaTien;
        this.thoiHanGoiTheoNgay = thoiHanGoiTheoNgay;
        this.goiHienTai = goiHienTai;
    }

    public String getMaGoiThanhVien() { return maGoiThanhVien; }
    public String getTenGoi() { return tenGoi; }
    public String getMoTa() { return moTa; }
    public String getTrangThai() { return trangThai; }
    public BigDecimal getGiaTien() { return giaTien; }
    public Integer getThoiHanGoiTheoNgay() { return thoiHanGoiTheoNgay; }
    public boolean isGoiHienTai() { return goiHienTai; }
}
