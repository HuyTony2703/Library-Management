package com.library.backend.dto.reader;

import java.time.LocalDateTime;

public class ReaderNotificationResponse {

    private final String maThongBao;
    private final String maLoaiThongBao;
    private final String tenLoaiThongBao;
    private final String tieuDe;
    private final String noiDung;
    private final LocalDateTime ngayTao;
    private final boolean guiTrongApp;
    private final boolean guiEmail;
    private final String trangThaiEmail;
    private final int soLanThuGuiEmail;
    private final boolean daDoc;
    private final LocalDateTime thoiGianDoc;

    public ReaderNotificationResponse(
            String maThongBao,
            String maLoaiThongBao,
            String tenLoaiThongBao,
            String tieuDe,
            String noiDung,
            LocalDateTime ngayTao,
            boolean guiTrongApp,
            boolean guiEmail,
            String trangThaiEmail,
            int soLanThuGuiEmail,
            boolean daDoc,
            LocalDateTime thoiGianDoc
    ) {
        this.maThongBao = maThongBao;
        this.maLoaiThongBao = maLoaiThongBao;
        this.tenLoaiThongBao = tenLoaiThongBao;
        this.tieuDe = tieuDe;
        this.noiDung = noiDung;
        this.ngayTao = ngayTao;
        this.guiTrongApp = guiTrongApp;
        this.guiEmail = guiEmail;
        this.trangThaiEmail = trangThaiEmail;
        this.soLanThuGuiEmail = soLanThuGuiEmail;
        this.daDoc = daDoc;
        this.thoiGianDoc = thoiGianDoc;
    }

    public String getMaThongBao() {
        return maThongBao;
    }

    public String getMaLoaiThongBao() {
        return maLoaiThongBao;
    }

    public String getTenLoaiThongBao() {
        return tenLoaiThongBao;
    }

    public String getTieuDe() {
        return tieuDe;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public boolean isGuiTrongApp() {
        return guiTrongApp;
    }

    public boolean isGuiEmail() {
        return guiEmail;
    }

    public String getTrangThaiEmail() {
        return trangThaiEmail;
    }

    public int getSoLanThuGuiEmail() {
        return soLanThuGuiEmail;
    }

    public boolean isDaDoc() {
        return daDoc;
    }

    public LocalDateTime getThoiGianDoc() {
        return thoiGianDoc;
    }
}
