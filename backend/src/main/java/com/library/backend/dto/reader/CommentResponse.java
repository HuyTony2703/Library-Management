package com.library.backend.dto.reader;

import java.time.LocalDateTime;

public class CommentResponse {

    private final String maBinhLuan;
    private final String maDocGia;
    private final String hoTenDocGia;
    private final String maDauSach;
    private final String noiDung;
    private final LocalDateTime ngayBinhLuan;
    private final String trangThai;
    private final boolean cuaToi;

    public CommentResponse(
            String maBinhLuan,
            String maDocGia,
            String hoTenDocGia,
            String maDauSach,
            String noiDung,
            LocalDateTime ngayBinhLuan,
            String trangThai,
            boolean cuaToi
    ) {
        this.maBinhLuan = maBinhLuan;
        this.maDocGia = maDocGia;
        this.hoTenDocGia = hoTenDocGia;
        this.maDauSach = maDauSach;
        this.noiDung = noiDung;
        this.ngayBinhLuan = ngayBinhLuan;
        this.trangThai = trangThai;
        this.cuaToi = cuaToi;
    }

    public String getMaBinhLuan() { return maBinhLuan; }
    public String getMaDocGia() { return maDocGia; }
    public String getHoTenDocGia() { return hoTenDocGia; }
    public String getMaDauSach() { return maDauSach; }
    public String getNoiDung() { return noiDung; }
    public LocalDateTime getNgayBinhLuan() { return ngayBinhLuan; }
    public String getTrangThai() { return trangThai; }
    public boolean isCuaToi() { return cuaToi; }
}
