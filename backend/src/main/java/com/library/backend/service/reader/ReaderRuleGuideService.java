package com.library.backend.service.reader;

import com.library.backend.dto.reader.ReaderBorrowRuleResponse;
import com.library.backend.dto.reader.ReaderCurrentRuleResponse;
import com.library.backend.dto.reader.ReaderMembershipRuleResponse;
import com.library.backend.exception.BusinessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReaderRuleGuideService {

    private static final String ACTIVE_POLICY = "Đang áp dụng";

    private final JdbcTemplate jdbcTemplate;

    public ReaderRuleGuideService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ReaderCurrentRuleResponse getCurrentRules() {
        String maPhienBan = getCurrentPolicyVersion();
        RuleBaseInfo baseInfo = getBaseRuleInfo(maPhienBan);

        return new ReaderCurrentRuleResponse(
                baseInfo.maPhienBan(),
                baseInfo.tenPhienBan(),
                baseInfo.ngayApDung(),
                baseInfo.ghiChu(),
                baseInfo.tuoiToiThieu(),
                baseInfo.tuoiToiDa(),
                baseInfo.thoiHanTheTheoThang(),
                baseInfo.khoangCachNamXuatBan(),
                baseInfo.soNgayNhacTruocHan(),
                baseInfo.soNgayGiuDatTruoc(),
                baseInfo.mucPhatTreMoiNgay(),
                getMembershipRules(maPhienBan),
                getBorrowRules(maPhienBan)
        );
    }

    private String getCurrentPolicyVersion() {
        List<String> result = jdbcTemplate.query(
                """
                SELECT TOP 1 MaPhienBan
                FROM PHIENBANQUYDINH
                WHERE TrangThai = ?
                ORDER BY NgayApDung DESC
                """,
                (rs, rowNum) -> rs.getString("MaPhienBan"),
                ACTIVE_POLICY
        );

        if (result.isEmpty()) {
            throw new BusinessException("Chưa có phiên bản quy định đang áp dụng");
        }

        return result.get(0);
    }

    private RuleBaseInfo getBaseRuleInfo(String maPhienBan) {
        List<RuleBaseInfo> result = jdbcTemplate.query(
                """
                SELECT
                    pb.MaPhienBan,
                    pb.TenPhienBan,
                    pb.NgayApDung,
                    pb.GhiChu,
                    ts.TuoiToiThieu,
                    ts.TuoiToiDa,
                    ts.ThoiHanTheTheoThang,
                    ts.KhoangCachNamXuatBan,
                    ts.SoNgayNhacTruocHan,
                    ts.SoNgayGiuDatTruoc,
                    ts.MucPhatTreMoiNgay
                FROM PHIENBANQUYDINH pb
                INNER JOIN THAMSOQUYDINH ts
                    ON pb.MaPhienBan = ts.MaPhienBan
                WHERE pb.MaPhienBan = ?
                """,
                (rs, rowNum) -> new RuleBaseInfo(
                        rs.getString("MaPhienBan"),
                        rs.getString("TenPhienBan"),
                        toLocalDateTime(rs.getTimestamp("NgayApDung")),
                        rs.getString("GhiChu"),
                        rs.getInt("TuoiToiThieu"),
                        rs.getInt("TuoiToiDa"),
                        rs.getInt("ThoiHanTheTheoThang"),
                        rs.getInt("KhoangCachNamXuatBan"),
                        rs.getInt("SoNgayNhacTruocHan"),
                        rs.getInt("SoNgayGiuDatTruoc"),
                        rs.getBigDecimal("MucPhatTreMoiNgay")
                ),
                maPhienBan
        );

        if (result.isEmpty()) {
            throw new BusinessException("Không tìm thấy tham số quy định cho phiên bản hiện tại");
        }

        return result.get(0);
    }

    private List<ReaderMembershipRuleResponse> getMembershipRules(String maPhienBan) {
        return jdbcTemplate.query(
                """
                SELECT
                    qdg.MaGoiThanhVien,
                    g.TenGoi,
                    qdg.SoSachMuonToiDa,
                    qdg.SoLanGiaHanToiDa
                FROM QUYDINHGOI qdg
                INNER JOIN GOITHANHVIEN g
                    ON qdg.MaGoiThanhVien = g.MaGoiThanhVien
                WHERE qdg.MaPhienBan = ?
                ORDER BY qdg.SoSachMuonToiDa ASC, g.TenGoi ASC
                """,
                (rs, rowNum) -> new ReaderMembershipRuleResponse(
                        rs.getString("MaGoiThanhVien"),
                        rs.getString("TenGoi"),
                        rs.getInt("SoSachMuonToiDa"),
                        rs.getInt("SoLanGiaHanToiDa")
                ),
                maPhienBan
        );
    }

    private List<ReaderBorrowRuleResponse> getBorrowRules(String maPhienBan) {
        return jdbcTemplate.query(
                """
                SELECT
                    qd.MaGoiThanhVien,
                    g.TenGoi,
                    qd.MaTheLoai,
                    tl.TenTheLoai,
                    qd.SoNgayMuon,
                    qd.SoNgayGiaHanMoiLan
                FROM QUYDINHMUON_THELOAI qd
                INNER JOIN GOITHANHVIEN g
                    ON qd.MaGoiThanhVien = g.MaGoiThanhVien
                INNER JOIN THELOAI tl
                    ON qd.MaTheLoai = tl.MaTheLoai
                WHERE qd.MaPhienBan = ?
                ORDER BY g.TenGoi ASC, tl.TenTheLoai ASC
                """,
                (rs, rowNum) -> new ReaderBorrowRuleResponse(
                        rs.getString("MaGoiThanhVien"),
                        rs.getString("TenGoi"),
                        rs.getString("MaTheLoai"),
                        rs.getString("TenTheLoai"),
                        rs.getInt("SoNgayMuon"),
                        rs.getInt("SoNgayGiaHanMoiLan")
                ),
                maPhienBan
        );
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private record RuleBaseInfo(
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
            BigDecimal mucPhatTreMoiNgay
    ) {
    }
}
