package com.library.backend.repository;

import com.library.backend.entity.ChiNhanh;
import com.library.backend.entity.NhanVien;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class StaffBranchRepository {

    private static final String ACTIVE = "Hoạt động";

    private final JdbcTemplate jdbcTemplate;
    private final ChiNhanhRepository chiNhanhRepository;

    public StaffBranchRepository(
            JdbcTemplate jdbcTemplate,
            ChiNhanhRepository chiNhanhRepository
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.chiNhanhRepository = chiNhanhRepository;
    }

    public List<StaffBranch> findEffectiveBranches(NhanVien nhanVien, LocalDate businessDate) {
        if (!assignmentTableExists()) {
            return findLegacyBranch(nhanVien);
        }

        String sql = """
                SELECT
                    cn.MaChiNhanh,
                    cn.TenChiNhanh,
                    nvc.LaMacDinh
                FROM NHANVIEN_CHINHANH nvc
                INNER JOIN CHINHANH cn ON cn.MaChiNhanh = nvc.MaChiNhanh
                WHERE nvc.MaNhanVien = ?
                  AND nvc.TrangThai = N'Hoạt động'
                  AND cn.TrangThai = N'Hoạt động'
                  AND nvc.NgayBatDau <= ?
                  AND (nvc.NgayKetThuc IS NULL OR nvc.NgayKetThuc >= ?)
                ORDER BY nvc.LaMacDinh DESC, cn.TenChiNhanh, cn.MaChiNhanh
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new StaffBranch(
                        rs.getString("MaChiNhanh"),
                        rs.getString("TenChiNhanh"),
                        rs.getBoolean("LaMacDinh")
                ),
                nhanVien.getMaNhanVien(),
                java.sql.Date.valueOf(businessDate),
                java.sql.Date.valueOf(businessDate)
        );
    }

    private boolean assignmentTableExists() {
        Integer result = jdbcTemplate.queryForObject(
                "SELECT CASE WHEN OBJECT_ID('dbo.NHANVIEN_CHINHANH', 'U') IS NULL THEN 0 ELSE 1 END",
                Integer.class
        );
        return result != null && result == 1;
    }

    private List<StaffBranch> findLegacyBranch(NhanVien nhanVien) {
        if (nhanVien.getMaChiNhanh() == null || nhanVien.getMaChiNhanh().isBlank()) {
            return List.of();
        }

        return chiNhanhRepository.findById(nhanVien.getMaChiNhanh())
                .filter(branch -> ACTIVE.equals(branch.getTrangThai()))
                .map(branch -> List.of(toLegacyBranch(branch)))
                .orElseGet(List::of);
    }

    private StaffBranch toLegacyBranch(ChiNhanh branch) {
        return new StaffBranch(branch.getMaChiNhanh(), branch.getTenChiNhanh(), true);
    }

    public record StaffBranch(String id, String name, boolean defaultBranch) {}
}
