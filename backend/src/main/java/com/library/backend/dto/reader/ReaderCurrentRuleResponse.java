package com.library.backend.dto.reader;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ReaderCurrentRuleResponse {

    private final String maPhienBan;
    private final String tenPhienBan;
    private final LocalDateTime ngayApDung;
    private final String ghiChu;
    private final Integer tuoiToiThieu;
    private final Integer tuoiToiDa;
    private final Integer thoiHanTheTheoThang;
    private final Integer khoangCachNamXuatBan;
    private final Integer soNgayNhacTruocHan;
    private final Integer soNgayGiuDatTruoc;
    private final BigDecimal mucPhatTreMoiNgay;
    private final List<ReaderMembershipRuleResponse> quyDinhGoi;
    private final List<ReaderBorrowRuleResponse> quyDinhMuonTheoTheLoai;

    public ReaderCurrentRuleResponse(
            String maPhienBan,
            String tenPhienBan,
            LocalDateTime ngayApDung,
            String ghiChu,
            Integer tuoiToiThieu,
            Integer tuoiToiDa,
            Integer thoiHanTheTheoThang,
            Integer khoangCachNamXuatBan,
            Integer soNgayNhacTruocHan,
            Integer soNgayGiuDatTruoc,
            BigDecimal mucPhatTreMoiNgay,
            List<ReaderMembershipRuleResponse> quyDinhGoi,
            List<ReaderBorrowRuleResponse> quyDinhMuonTheoTheLoai
    ) {
        this.maPhienBan = maPhienBan;
        this.tenPhienBan = tenPhienBan;
        this.ngayApDung = ngayApDung;
        this.ghiChu = ghiChu;
        this.tuoiToiThieu = tuoiToiThieu;
        this.tuoiToiDa = tuoiToiDa;
        this.thoiHanTheTheoThang = thoiHanTheTheoThang;
        this.khoangCachNamXuatBan = khoangCachNamXuatBan;
        this.soNgayNhacTruocHan = soNgayNhacTruocHan;
        this.soNgayGiuDatTruoc = soNgayGiuDatTruoc;
        this.mucPhatTreMoiNgay = mucPhatTreMoiNgay;
        this.quyDinhGoi = quyDinhGoi;
        this.quyDinhMuonTheoTheLoai = quyDinhMuonTheoTheLoai;
    }

    public String getMaPhienBan() { return maPhienBan; }
    public String getTenPhienBan() { return tenPhienBan; }
    public LocalDateTime getNgayApDung() { return ngayApDung; }
    public String getGhiChu() { return ghiChu; }
    public Integer getTuoiToiThieu() { return tuoiToiThieu; }
    public Integer getTuoiToiDa() { return tuoiToiDa; }
    public Integer getThoiHanTheTheoThang() { return thoiHanTheTheoThang; }
    public Integer getKhoangCachNamXuatBan() { return khoangCachNamXuatBan; }
    public Integer getSoNgayNhacTruocHan() { return soNgayNhacTruocHan; }
    public Integer getSoNgayGiuDatTruoc() { return soNgayGiuDatTruoc; }
    public BigDecimal getMucPhatTreMoiNgay() { return mucPhatTreMoiNgay; }
    public List<ReaderMembershipRuleResponse> getQuyDinhGoi() { return quyDinhGoi; }
    public List<ReaderBorrowRuleResponse> getQuyDinhMuonTheoTheLoai() { return quyDinhMuonTheoTheLoai; }
}
