package com.library.backend.repository;

import com.library.backend.dto.BookCopyLocationFiltersResponse;
import com.library.backend.dto.CuonSachListItemResponse;
import com.library.backend.dto.CuonSachListQuery;
import com.library.backend.dto.LocationFilterOptionResponse;
import com.library.backend.dto.PageResponse;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class CuonSachPageRepository {
    private static final Map<String, String> SORT_COLUMNS = Map.of(
            "id", "cs.MaCuonSach",
            "title", "ds.TenDauSach",
            "branch", "cn.TenChiNhanh",
            "location", "vt.MaViTriHienThi",
            "status", "tt.TenTrangThai",
            "importedAt", "cs.NgayNhapSach",
            "barcode", "cs.MaVach"
    );

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public CuonSachPageRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public PageResponse<CuonSachListItemResponse> findPage(
            CuonSachListQuery query,
            List<String> effectiveBranches,
            boolean unrestrictedBranches
    ) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        List<String> conditions = buildConditions(query, effectiveBranches, unrestrictedBranches, params);
        String from = """
                FROM CUONSACH cs
                INNER JOIN DAUSACH ds ON ds.MaDauSach = cs.MaDauSach
                INNER JOIN CHINHANH cn ON cn.MaChiNhanh = cs.MaChiNhanh
                INNER JOIN VITRISACH vt ON vt.MaViTri = cs.MaViTri
                INNER JOIN KESACH ks ON ks.MaKeSach = vt.MaKeSach
                INNER JOIN KHU k ON k.MaKhu = ks.MaKhu
                INNER JOIN TRANGTHAICUONSACH tt ON tt.MaTrangThai = cs.MaTrangThai
                """;
        String where = conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions);
        long total = jdbcTemplate.queryForObject("SELECT COUNT(*) " + from + where, params, Long.class);

        String[] sortParts = query.sort() == null ? new String[0] : query.sort().split(",", 2);
        String sortColumn = SORT_COLUMNS.getOrDefault(sortParts.length > 0 ? sortParts[0] : "", "cs.NgayNhapSach");
        String direction = sortParts.length > 1 && "asc".equalsIgnoreCase(sortParts[1]) ? "ASC" : "DESC";
        params.addValue("offset", (query.page() - 1) * query.pageSize());
        params.addValue("pageSize", query.pageSize());

        String stableTieBreak = "cs.MaCuonSach".equals(sortColumn) ? "" : ", cs.MaCuonSach ASC";
        String sql = """
                SELECT cs.MaCuonSach, cs.MaDauSach, ds.TenDauSach, ds.ISBN,
                       cs.MaChiNhanh, cn.TenChiNhanh,
                       k.MaKhu, k.TenKhu, ks.MaKeSach, ks.TenKeSach,
                       cs.MaViTri, vt.MaViTriHienThi,
                       cs.MaTrangThai, tt.TenTrangThai,
                       cs.MaVach, cs.MaQRCode, cs.NgayNhapSach, cs.GhiChu
                """ + from + where
                + " ORDER BY " + sortColumn + " " + direction + stableTieBreak
                + " OFFSET :offset ROWS FETCH NEXT :pageSize ROWS ONLY";
        List<CuonSachListItemResponse> items = jdbcTemplate.query(sql, params, (rs, rowNum) -> mapCopy(rs));
        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / query.pageSize());
        return new PageResponse<>(items, query.page(), query.pageSize(), total, totalPages);
    }

    public List<CuonSachListItemResponse> findAll(List<String> effectiveBranches, boolean unrestrictedBranches) {
        if (!unrestrictedBranches && effectiveBranches.isEmpty()) return List.of();
        MapSqlParameterSource params = new MapSqlParameterSource();
        String branchFilter = "";
        if (!unrestrictedBranches) {
            branchFilter = " WHERE cs.MaChiNhanh IN (:effectiveBranches)";
            params.addValue("effectiveBranches", effectiveBranches);
        }
        String sql = """
                SELECT cs.MaCuonSach, cs.MaDauSach, ds.TenDauSach, ds.ISBN,
                       cs.MaChiNhanh, cn.TenChiNhanh,
                       k.MaKhu, k.TenKhu, ks.MaKeSach, ks.TenKeSach,
                       cs.MaViTri, vt.MaViTriHienThi,
                       cs.MaTrangThai, tt.TenTrangThai,
                       cs.MaVach, cs.MaQRCode, cs.NgayNhapSach, cs.GhiChu
                FROM CUONSACH cs
                INNER JOIN DAUSACH ds ON ds.MaDauSach = cs.MaDauSach
                INNER JOIN CHINHANH cn ON cn.MaChiNhanh = cs.MaChiNhanh
                INNER JOIN VITRISACH vt ON vt.MaViTri = cs.MaViTri
                INNER JOIN KESACH ks ON ks.MaKeSach = vt.MaKeSach
                INNER JOIN KHU k ON k.MaKhu = ks.MaKhu
                INNER JOIN TRANGTHAICUONSACH tt ON tt.MaTrangThai = cs.MaTrangThai
                """ + branchFilter + " ORDER BY cs.MaCuonSach";
        return jdbcTemplate.query(sql, params, (rs, rowNum) -> mapCopy(rs));
    }

    public List<String> findMatchingIds(
            CuonSachListQuery query,
            List<String> effectiveBranches,
            boolean unrestrictedBranches,
            List<String> excludedIds
    ) {
        return findMatchingIds(query, effectiveBranches, unrestrictedBranches, excludedIds, null);
    }

    public List<String> findMatchingIds(
            CuonSachListQuery query,
            List<String> effectiveBranches,
            boolean unrestrictedBranches,
            List<String> excludedIds,
            Integer maxRows
    ) {
        if (!unrestrictedBranches && effectiveBranches.isEmpty()) return List.of();
        MapSqlParameterSource params = new MapSqlParameterSource();
        List<String> conditions = buildConditions(query, effectiveBranches, unrestrictedBranches, params);
        if (excludedIds != null && !excludedIds.isEmpty()) {
            conditions.add("cs.MaCuonSach NOT IN (:excludedIds)");
            params.addValue("excludedIds", excludedIds);
        }
        String where = conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions);
        String top = "";
        if (maxRows != null) {
            top = "TOP (:maxRows) ";
            params.addValue("maxRows", maxRows);
        }
        String sql = """
                SELECT %s cs.MaCuonSach
                FROM CUONSACH cs
                INNER JOIN DAUSACH ds ON ds.MaDauSach = cs.MaDauSach
                INNER JOIN CHINHANH cn ON cn.MaChiNhanh = cs.MaChiNhanh
                INNER JOIN VITRISACH vt ON vt.MaViTri = cs.MaViTri
                INNER JOIN KESACH ks ON ks.MaKeSach = vt.MaKeSach
                INNER JOIN KHU k ON k.MaKhu = ks.MaKhu
                INNER JOIN TRANGTHAICUONSACH tt ON tt.MaTrangThai = cs.MaTrangThai
                """.formatted(top) + where + " ORDER BY cs.MaCuonSach ASC";
        return jdbcTemplate.queryForList(sql, params, String.class);
    }

    public BookCopyLocationFiltersResponse findLocationOptions(List<String> branches, boolean unrestrictedBranches) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        String where = "";
        if (!unrestrictedBranches) {
            if (branches.isEmpty()) return new BookCopyLocationFiltersResponse(List.of(), List.of(), List.of());
            where = " WHERE k.MaChiNhanh IN (:branches)";
            params.addValue("branches", branches);
        }
        List<LocationFilterOptionResponse> areas = jdbcTemplate.query(
                "SELECT k.MaKhu value, k.TenKhu label, k.MaChiNhanh branchId, CAST(NULL AS VARCHAR(30)) parentId FROM KHU k"
                        + where + " ORDER BY k.TenKhu", params, this::mapOption);
        List<LocationFilterOptionResponse> shelves = jdbcTemplate.query(
                "SELECT ks.MaKeSach value, ks.TenKeSach label, k.MaChiNhanh branchId, k.MaKhu parentId FROM KESACH ks INNER JOIN KHU k ON k.MaKhu = ks.MaKhu"
                        + where + " ORDER BY k.TenKhu, ks.TenKeSach", params, this::mapOption);
        List<LocationFilterOptionResponse> locations = jdbcTemplate.query(
                "SELECT vt.MaViTri value, vt.MaViTriHienThi label, k.MaChiNhanh branchId, ks.MaKeSach parentId FROM VITRISACH vt INNER JOIN KESACH ks ON ks.MaKeSach = vt.MaKeSach INNER JOIN KHU k ON k.MaKhu = ks.MaKhu"
                        + where + " ORDER BY k.TenKhu, ks.TenKeSach, vt.MaViTriHienThi", params, this::mapOption);
        return new BookCopyLocationFiltersResponse(areas, shelves, locations);
    }

    public Optional<CuonSachListItemResponse> findById(
            String copyId,
            List<String> effectiveBranches,
            boolean unrestrictedBranches
    ) {
        if (!unrestrictedBranches && effectiveBranches.isEmpty()) return Optional.empty();
        MapSqlParameterSource params = new MapSqlParameterSource("copyId", copyId);
        String branchFilter = "";
        if (!unrestrictedBranches) {
            branchFilter = " AND cs.MaChiNhanh IN (:effectiveBranches)";
            params.addValue("effectiveBranches", effectiveBranches);
        }
        String sql = """
                SELECT cs.MaCuonSach, cs.MaDauSach, ds.TenDauSach, ds.ISBN,
                       cs.MaChiNhanh, cn.TenChiNhanh,
                       k.MaKhu, k.TenKhu, ks.MaKeSach, ks.TenKeSach,
                       cs.MaViTri, vt.MaViTriHienThi,
                       cs.MaTrangThai, tt.TenTrangThai,
                       cs.MaVach, cs.MaQRCode, cs.NgayNhapSach, cs.GhiChu
                FROM CUONSACH cs
                INNER JOIN DAUSACH ds ON ds.MaDauSach = cs.MaDauSach
                INNER JOIN CHINHANH cn ON cn.MaChiNhanh = cs.MaChiNhanh
                INNER JOIN VITRISACH vt ON vt.MaViTri = cs.MaViTri
                INNER JOIN KESACH ks ON ks.MaKeSach = vt.MaKeSach
                INNER JOIN KHU k ON k.MaKhu = ks.MaKhu
                INNER JOIN TRANGTHAICUONSACH tt ON tt.MaTrangThai = cs.MaTrangThai
                WHERE cs.MaCuonSach = :copyId
                """ + branchFilter;
        return jdbcTemplate.query(sql, params, (rs, rowNum) -> mapCopy(rs)).stream().findFirst();
    }

    private List<String> buildConditions(
            CuonSachListQuery query,
            List<String> effectiveBranches,
            boolean unrestrictedBranches,
            MapSqlParameterSource params
    ) {
        List<String> conditions = new ArrayList<>();
        if (!unrestrictedBranches) {
            conditions.add("cs.MaChiNhanh IN (:effectiveBranches)");
            params.addValue("effectiveBranches", effectiveBranches);
        }
        if (query.search() != null && !query.search().isBlank()) {
            conditions.add("(cs.MaCuonSach LIKE :search ESCAPE '\\' OR cs.MaVach LIKE :search ESCAPE '\\' OR cs.MaQRCode LIKE :search ESCAPE '\\' OR ds.TenDauSach COLLATE Vietnamese_100_CI_AI LIKE :search ESCAPE '\\' OR ds.ISBN LIKE :search ESCAPE '\\')");
            params.addValue("search", "%" + escapeLike(query.search().trim()) + "%");
        }
        addIn(conditions, params, "cs.MaTrangThai", "statusIds", query.statusIds());
        if (unrestrictedBranches) addIn(conditions, params, "cs.MaChiNhanh", "branchIds", query.branchIds());
        addIn(conditions, params, "cs.MaDauSach", "titleIds", query.titleIds());
        addIn(conditions, params, "k.MaKhu", "areaIds", query.areaIds());
        addIn(conditions, params, "ks.MaKeSach", "shelfIds", query.shelfIds());
        addIn(conditions, params, "vt.MaViTri", "locationIds", query.locationIds());
        if (query.importedFrom() != null) { conditions.add("cs.NgayNhapSach >= :importedFrom"); params.addValue("importedFrom", query.importedFrom()); }
        if (query.importedTo() != null) { conditions.add("cs.NgayNhapSach <= :importedTo"); params.addValue("importedTo", query.importedTo()); }
        addPresence(conditions, "cs.MaVach", query.hasBarcode());
        addPresence(conditions, "cs.MaQRCode", query.hasQr());
        return conditions;
    }

    private void addIn(List<String> conditions, MapSqlParameterSource params, String column, String name, List<String> values) {
        if (values != null && !values.isEmpty()) { conditions.add(column + " IN (:" + name + ")"); params.addValue(name, values); }
    }

    private void addPresence(List<String> conditions, String column, Boolean present) {
        if (present != null) conditions.add(present ? "NULLIF(LTRIM(RTRIM(" + column + ")), '') IS NOT NULL" : "NULLIF(LTRIM(RTRIM(" + column + ")), '') IS NULL");
    }

    private LocationFilterOptionResponse mapOption(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new LocationFilterOptionResponse(rs.getString("value"), rs.getString("label"), rs.getString("branchId"), rs.getString("parentId"));
    }

    private CuonSachListItemResponse mapCopy(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new CuonSachListItemResponse(
                rs.getString("MaCuonSach"), rs.getString("MaDauSach"), rs.getString("TenDauSach"), rs.getString("ISBN"),
                rs.getString("MaChiNhanh"), rs.getString("TenChiNhanh"),
                rs.getString("MaKhu"), rs.getString("TenKhu"), rs.getString("MaKeSach"), rs.getString("TenKeSach"),
                rs.getString("MaViTri"), rs.getString("MaViTriHienThi"),
                rs.getString("MaTrangThai"), rs.getString("TenTrangThai"),
                rs.getString("MaVach"), rs.getString("MaQRCode"),
                rs.getDate("NgayNhapSach").toLocalDate(), rs.getString("GhiChu")
        );
    }

    private String escapeLike(String value) { return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_"); }
}
