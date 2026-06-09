package com.library.backend.dto.reader;

public class ReaderMembershipRuleResponse {

    private final String maGoiThanhVien;
    private final String tenGoi;
    private final Integer soSachMuonToiDa;
    private final Integer soLanGiaHanToiDa;

    public ReaderMembershipRuleResponse(
            String maGoiThanhVien,
            String tenGoi,
            Integer soSachMuonToiDa,
            Integer soLanGiaHanToiDa
    ) {
        this.maGoiThanhVien = maGoiThanhVien;
        this.tenGoi = tenGoi;
        this.soSachMuonToiDa = soSachMuonToiDa;
        this.soLanGiaHanToiDa = soLanGiaHanToiDa;
    }

    public String getMaGoiThanhVien() { return maGoiThanhVien; }
    public String getTenGoi() { return tenGoi; }
    public Integer getSoSachMuonToiDa() { return soSachMuonToiDa; }
    public Integer getSoLanGiaHanToiDa() { return soLanGiaHanToiDa; }
}
