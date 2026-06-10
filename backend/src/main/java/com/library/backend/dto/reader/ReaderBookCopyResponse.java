package com.library.backend.dto.reader;

public class ReaderBookCopyResponse {

    private final String maCuonSach;
    private final String maChiNhanh;
    private final String tenChiNhanh;
    private final String maViTri;
    private final String viTriHienThi;
    private final String maTrangThai;
    private final String tenTrangThai;

    public ReaderBookCopyResponse(
            String maCuonSach,
            String maChiNhanh,
            String tenChiNhanh,
            String maViTri,
            String viTriHienThi,
            String maTrangThai,
            String tenTrangThai
    ) {
        this.maCuonSach = maCuonSach;
        this.maChiNhanh = maChiNhanh;
        this.tenChiNhanh = tenChiNhanh;
        this.maViTri = maViTri;
        this.viTriHienThi = viTriHienThi;
        this.maTrangThai = maTrangThai;
        this.tenTrangThai = tenTrangThai;
    }

    public String getMaCuonSach() { return maCuonSach; }
    public String getMaChiNhanh() { return maChiNhanh; }
    public String getTenChiNhanh() { return tenChiNhanh; }
    public String getMaViTri() { return maViTri; }
    public String getViTriHienThi() { return viTriHienThi; }
    public String getMaTrangThai() { return maTrangThai; }
    public String getTenTrangThai() { return tenTrangThai; }
}

