package com.library.backend.dto.reader;

import com.library.backend.entity.DocGia;

import java.time.LocalDate;

public class ReaderProfileResponse {

    private final String maDocGia;
    private final String maTaiKhoan;
    private final String maNhomDocGia;
    private final String hoTen;
    private final LocalDate ngaySinh;
    private final String diaChi;
    private final String email;
    private final String soDienThoai;
    private final LocalDate ngayLapThe;
    private final LocalDate ngayHetHanThe;
    private final String trangThai;

    public ReaderProfileResponse(
            String maDocGia,
            String maTaiKhoan,
            String maNhomDocGia,
            String hoTen,
            LocalDate ngaySinh,
            String diaChi,
            String email,
            String soDienThoai,
            LocalDate ngayLapThe,
            LocalDate ngayHetHanThe,
            String trangThai
    ) {
        this.maDocGia = maDocGia;
        this.maTaiKhoan = maTaiKhoan;
        this.maNhomDocGia = maNhomDocGia;
        this.hoTen = hoTen;
        this.ngaySinh = ngaySinh;
        this.diaChi = diaChi;
        this.email = email;
        this.soDienThoai = soDienThoai;
        this.ngayLapThe = ngayLapThe;
        this.ngayHetHanThe = ngayHetHanThe;
        this.trangThai = trangThai;
    }

    public static ReaderProfileResponse from(DocGia docGia) {
        return new ReaderProfileResponse(
                docGia.getMaDocGia(),
                docGia.getMaTaiKhoan(),
                docGia.getMaNhomDocGia(),
                docGia.getHoTen(),
                docGia.getNgaySinh(),
                docGia.getDiaChi(),
                docGia.getEmail(),
                docGia.getSoDienThoai(),
                docGia.getNgayLapThe(),
                docGia.getNgayHetHanThe(),
                docGia.getTrangThai()
        );
    }

    public String getMaDocGia() { return maDocGia; }
    public String getMaTaiKhoan() { return maTaiKhoan; }
    public String getMaNhomDocGia() { return maNhomDocGia; }
    public String getHoTen() { return hoTen; }
    public LocalDate getNgaySinh() { return ngaySinh; }
    public String getDiaChi() { return diaChi; }
    public String getEmail() { return email; }
    public String getSoDienThoai() { return soDienThoai; }
    public LocalDate getNgayLapThe() { return ngayLapThe; }
    public LocalDate getNgayHetHanThe() { return ngayHetHanThe; }
    public String getTrangThai() { return trangThai; }
}
