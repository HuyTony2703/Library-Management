package com.library.backend.dto.reader;

import java.time.LocalDateTime;

public class ReaderRenewalResponse {

    private final String maGiaHan;
    private final String maChiTietMuon;
    private final String maDocGia;
    private final String maCuonSach;
    private final String tenDauSach;
    private final LocalDateTime ngayGiaHan;
    private final LocalDateTime hanTraCu;
    private final LocalDateTime hanTraMoi;
    private final Integer soNgayGiaHan;
    private final Integer lanGiaHanThu;
    private final String trangThai;
    private final String lyDoTuChoi;

    public ReaderRenewalResponse(
            String maGiaHan,
            String maChiTietMuon,
            String maDocGia,
            String maCuonSach,
            String tenDauSach,
            LocalDateTime ngayGiaHan,
            LocalDateTime hanTraCu,
            LocalDateTime hanTraMoi,
            Integer soNgayGiaHan,
            Integer lanGiaHanThu,
            String trangThai,
            String lyDoTuChoi
    ) {
        this.maGiaHan = maGiaHan;
        this.maChiTietMuon = maChiTietMuon;
        this.maDocGia = maDocGia;
        this.maCuonSach = maCuonSach;
        this.tenDauSach = tenDauSach;
        this.ngayGiaHan = ngayGiaHan;
        this.hanTraCu = hanTraCu;
        this.hanTraMoi = hanTraMoi;
        this.soNgayGiaHan = soNgayGiaHan;
        this.lanGiaHanThu = lanGiaHanThu;
        this.trangThai = trangThai;
        this.lyDoTuChoi = lyDoTuChoi;
    }

    public String getMaGiaHan() { return maGiaHan; }
    public String getMaChiTietMuon() { return maChiTietMuon; }
    public String getMaDocGia() { return maDocGia; }
    public String getMaCuonSach() { return maCuonSach; }
    public String getTenDauSach() { return tenDauSach; }
    public LocalDateTime getNgayGiaHan() { return ngayGiaHan; }
    public LocalDateTime getHanTraCu() { return hanTraCu; }
    public LocalDateTime getHanTraMoi() { return hanTraMoi; }
    public Integer getSoNgayGiaHan() { return soNgayGiaHan; }
    public Integer getLanGiaHanThu() { return lanGiaHanThu; }
    public String getTrangThai() { return trangThai; }
    public String getLyDoTuChoi() { return lyDoTuChoi; }
}

