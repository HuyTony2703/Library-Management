package com.library.backend.dto.reader;

import java.time.LocalDateTime;

public class ReaderReservationResponse {

    private final String maPhieuDatTruoc;
    private final String maDocGia;
    private final String maDauSach;
    private final String tenDauSach;
    private final String maCuonSachDuocGiu;
    private final String maChiNhanh;
    private final LocalDateTime ngayDat;
    private final LocalDateTime ngayHetHanGiuCho;
    private final String trangThai;
    private final String ghiChu;

    public ReaderReservationResponse(
            String maPhieuDatTruoc,
            String maDocGia,
            String maDauSach,
            String tenDauSach,
            String maCuonSachDuocGiu,
            String maChiNhanh,
            LocalDateTime ngayDat,
            LocalDateTime ngayHetHanGiuCho,
            String trangThai,
            String ghiChu
    ) {
        this.maPhieuDatTruoc = maPhieuDatTruoc;
        this.maDocGia = maDocGia;
        this.maDauSach = maDauSach;
        this.tenDauSach = tenDauSach;
        this.maCuonSachDuocGiu = maCuonSachDuocGiu;
        this.maChiNhanh = maChiNhanh;
        this.ngayDat = ngayDat;
        this.ngayHetHanGiuCho = ngayHetHanGiuCho;
        this.trangThai = trangThai;
        this.ghiChu = ghiChu;
    }

    public String getMaPhieuDatTruoc() { return maPhieuDatTruoc; }
    public String getMaDocGia() { return maDocGia; }
    public String getMaDauSach() { return maDauSach; }
    public String getTenDauSach() { return tenDauSach; }
    public String getMaCuonSachDuocGiu() { return maCuonSachDuocGiu; }
    public String getMaChiNhanh() { return maChiNhanh; }
    public LocalDateTime getNgayDat() { return ngayDat; }
    public LocalDateTime getNgayHetHanGiuCho() { return ngayHetHanGiuCho; }
    public String getTrangThai() { return trangThai; }
    public String getGhiChu() { return ghiChu; }
}

