package com.library.backend.repository;

import com.library.backend.dto.DauSachListQuery;
import com.library.backend.dto.DauSachResponse;
import com.library.backend.dto.EntityPickerOptionResponse;
import com.library.backend.dto.PageResponse;
import com.library.backend.exception.BusinessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Repository
public class DauSachPageRepository {

    private static final Map<String, String> SORT_COLUMNS = Map.of(
            "id", "ds.MaDauSach",
            "title", "ds.TenDauSach",
            "isbn", "ds.ISBN",
            "publisher", "nxb.TenNhaXuatBan",
            "year", "ds.NamXuatBan",
            "language", "ds.NgonNgu",
            "value", "ds.TriGia",
            "status", "ds.TrangThai",
            "totalcopies", "copySummary.TongSoBan",
            "availablecopies", "copySummary.SoBanSanCo"
    );

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public DauSachPageRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public PageResponse<DauSachResponse> findPage(DauSachListQuery query) {
        SortSpec sort = parseSort(query.sort());
        MapSqlParameterSource params = new MapSqlParameterSource();
        String whereClause = buildWhereClause(query, params);

        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT_BIG(*) FROM DAUSACH ds " + whereClause,
                params,
                Long.class
        );
        long totalItems = total == null ? 0 : total;

        params.addValue("offset", (long) (query.page() - 1) * query.pageSize());
        params.addValue("pageSize", query.pageSize());

        String orderBy = " ORDER BY " + sort.column() + " " + sort.direction();
        if (!"ds.MaDauSach".equals(sort.column())) {
            orderBy += ", ds.MaDauSach ASC";
        }

        List<BookRow> rows = jdbcTemplate.query(
                """
                SELECT ds.MaDauSach, ds.MaNhaXuatBan, nxb.TenNhaXuatBan,
                       ds.TenDauSach, ds.ISBN,
                       ds.NamXuatBan, ds.NgonNgu, ds.SoTrang, ds.MoTa, ds.AnhBia,
                       ds.TriGia, ds.TrangThai,
                       copySummary.TongSoBan, copySummary.SoBanSanCo
                FROM DAUSACH ds
                LEFT JOIN NHAXUATBAN nxb ON nxb.MaNhaXuatBan = ds.MaNhaXuatBan
                OUTER APPLY (
                    SELECT
                        COUNT_BIG(*) AS TongSoBan,
                        COALESCE(SUM(CASE WHEN cs.MaTrangThai = 'TT_SANCO' THEN CAST(1 AS BIGINT) ELSE CAST(0 AS BIGINT) END), 0) AS SoBanSanCo
                    FROM CUONSACH cs
                    WHERE cs.MaDauSach = ds.MaDauSach
                ) copySummary
                """ + whereClause + orderBy + " OFFSET :offset ROWS FETCH NEXT :pageSize ROWS ONLY",
                params,
                (rs, rowNum) -> new BookRow(
                        rs.getString("MaDauSach"),
                        rs.getString("MaNhaXuatBan"),
                        rs.getString("TenNhaXuatBan"),
                        rs.getString("TenDauSach"),
                        rs.getString("ISBN"),
                        (Integer) rs.getObject("NamXuatBan"),
                        rs.getString("NgonNgu"),
                        (Integer) rs.getObject("SoTrang"),
                        rs.getString("MoTa"),
                        rs.getString("AnhBia"),
                        rs.getBigDecimal("TriGia"),
                        rs.getString("TrangThai"),
                        rs.getLong("TongSoBan"),
                        rs.getLong("SoBanSanCo")
                )
        );

        RelationData authors = loadRelations(rows, "DAUSACH_TACGIA", "TACGIA", "MaTacGia", "TenTacGia");
        RelationData categories = loadRelations(rows, "DAUSACH_THELOAI", "THELOAI", "MaTheLoai", "TenTheLoai");
        List<DauSachResponse> items = rows.stream()
                .map(row -> row.toResponse(
                        authors.ids().getOrDefault(row.id(), List.of()),
                        authors.names().getOrDefault(row.id(), List.of()),
                        categories.ids().getOrDefault(row.id(), List.of()),
                        categories.names().getOrDefault(row.id(), List.of())
                ))
                .toList();

        int totalPages = totalItems == 0 ? 0 : (int) ((totalItems + query.pageSize() - 1) / query.pageSize());
        return new PageResponse<>(items, query.page(), query.pageSize(), totalItems, totalPages);
    }

    public List<String> findMatchingIds(DauSachListQuery query, List<String> excludedIds) {
        return findMatchingIds(query, excludedIds, null);
    }

    public List<String> findMatchingIds(DauSachListQuery query, List<String> excludedIds, Integer maxRows) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        String whereClause = buildWhereClause(query, params);
        List<String> conditions = new ArrayList<>();
        if (whereClause != null && !whereClause.isBlank()) {
            conditions.add(whereClause.substring(" WHERE ".length()));
        }
        if (excludedIds != null && !excludedIds.isEmpty()) {
            conditions.add("ds.MaDauSach NOT IN (:excludedIds)");
            params.addValue("excludedIds", excludedIds);
        }
        String where = conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions);
        String top = "";
        if (maxRows != null) {
            top = "TOP (:maxRows) ";
            params.addValue("maxRows", maxRows);
        }
        return jdbcTemplate.queryForList(
                "SELECT " + top + "ds.MaDauSach FROM DAUSACH ds " + where + " ORDER BY ds.MaDauSach ASC",
                params,
                String.class
        );
    }

    public List<EntityPickerOptionResponse> searchForPicker(String query, int limit, boolean activeOnly) {
        String normalizedQuery = query.trim();
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("query", normalizedQuery)
                .addValue("containsQuery", "%" + escapeLike(normalizedQuery) + "%")
                .addValue("startsWithQuery", escapeLike(normalizedQuery) + "%")
                .addValue("limit", limit);

        String activeCondition = activeOnly ? " AND ds.TrangThai = N'Hoạt động'" : "";
        return jdbcTemplate.query(
                """
                SELECT TOP (:limit)
                       ds.MaDauSach, ds.TenDauSach, ds.ISBN, ds.NamXuatBan,
                       ds.TrangThai, nxb.TenNhaXuatBan
                FROM DAUSACH ds
                LEFT JOIN NHAXUATBAN nxb ON nxb.MaNhaXuatBan = ds.MaNhaXuatBan
                WHERE (
                    ds.MaDauSach COLLATE Vietnamese_100_CI_AI LIKE :containsQuery ESCAPE '\\'
                    OR ds.TenDauSach COLLATE Vietnamese_100_CI_AI LIKE :containsQuery ESCAPE '\\'
                    OR ds.ISBN COLLATE Vietnamese_100_CI_AI LIKE :containsQuery ESCAPE '\\'
                )
                """ + activeCondition + """
                ORDER BY
                    CASE
                        WHEN ds.MaDauSach COLLATE Vietnamese_100_CI_AI = :query THEN 0
                        WHEN ds.ISBN COLLATE Vietnamese_100_CI_AI = :query THEN 1
                        WHEN ds.TenDauSach COLLATE Vietnamese_100_CI_AI = :query THEN 2
                        WHEN ds.MaDauSach COLLATE Vietnamese_100_CI_AI LIKE :startsWithQuery ESCAPE '\\' THEN 3
                        WHEN ds.ISBN COLLATE Vietnamese_100_CI_AI LIKE :startsWithQuery ESCAPE '\\' THEN 4
                        WHEN ds.TenDauSach COLLATE Vietnamese_100_CI_AI LIKE :startsWithQuery ESCAPE '\\' THEN 5
                        ELSE 6
                    END,
                    ds.TenDauSach ASC,
                    ds.MaDauSach ASC
                """,
                params,
                (rs, rowNum) -> {
                    String code = rs.getString("MaDauSach");
                    String isbn = rs.getString("ISBN");
                    boolean exactMatch = normalizedQuery.equalsIgnoreCase(code)
                            || (isbn != null && normalizedQuery.equalsIgnoreCase(isbn));
                    Map<String, Object> metadata = new LinkedHashMap<>();
                    if (isbn != null && !isbn.isBlank()) {
                        metadata.put("isbn", isbn);
                    }
                    String publisher = rs.getString("TenNhaXuatBan");
                    if (publisher != null && !publisher.isBlank()) {
                        metadata.put("publisher", publisher);
                    }
                    Object publicationYear = rs.getObject("NamXuatBan");
                    if (publicationYear != null) {
                        metadata.put("publicationYear", publicationYear);
                    }
                    metadata.put("status", rs.getString("TrangThai"));
                    return new EntityPickerOptionResponse(
                            code,
                            rs.getString("TenDauSach"),
                            code,
                            metadata,
                            exactMatch
                    );
                }
        );
    }

    public List<EntityPickerOptionResponse> searchAuthors(String query, int limit) {
        return searchCatalogOptions(query, limit, "TACGIA", "MaTacGia", "TenTacGia", null);
    }

    public List<EntityPickerOptionResponse> searchCategories(String query, int limit) {
        return searchCatalogOptions(query, limit, "THELOAI", "MaTheLoai", "TenTheLoai", "TrangThai = N'Hoạt động'");
    }

    public List<EntityPickerOptionResponse> searchPublishers(String query, int limit) {
        return searchCatalogOptions(query, limit, "NHAXUATBAN", "MaNhaXuatBan", "TenNhaXuatBan", null);
    }

    private List<EntityPickerOptionResponse> searchCatalogOptions(
            String query,
            int limit,
            String table,
            String idColumn,
            String labelColumn,
            String fixedCondition
    ) {
        String normalizedQuery = query.trim();
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("query", normalizedQuery)
                .addValue("containsQuery", "%" + escapeLike(normalizedQuery) + "%")
                .addValue("startsWithQuery", escapeLike(normalizedQuery) + "%")
                .addValue("limit", limit);
        String condition = fixedCondition == null ? "" : " AND " + fixedCondition;
        String sql = "SELECT TOP (:limit) " + idColumn + " AS EntityId, "
                + labelColumn + " AS EntityLabel FROM " + table
                + " WHERE (" + idColumn + " COLLATE Vietnamese_100_CI_AI LIKE :containsQuery ESCAPE '\\'"
                + " OR " + labelColumn + " COLLATE Vietnamese_100_CI_AI LIKE :containsQuery ESCAPE '\\')"
                + condition
                + " ORDER BY CASE WHEN " + idColumn + " COLLATE Vietnamese_100_CI_AI = :query THEN 0"
                + " WHEN " + labelColumn + " COLLATE Vietnamese_100_CI_AI = :query THEN 1"
                + " WHEN " + idColumn + " COLLATE Vietnamese_100_CI_AI LIKE :startsWithQuery ESCAPE '\\' THEN 2"
                + " WHEN " + labelColumn + " COLLATE Vietnamese_100_CI_AI LIKE :startsWithQuery ESCAPE '\\' THEN 3"
                + " ELSE 4 END, " + labelColumn + ", " + idColumn;

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            String id = rs.getString("EntityId");
            return new EntityPickerOptionResponse(
                    id,
                    rs.getString("EntityLabel"),
                    id,
                    Map.of(),
                    normalizedQuery.equalsIgnoreCase(id)
            );
        });
    }

    static SortSpec parseSort(String sort) {
        String normalized = sort == null ? "title,asc" : sort.trim();
        String[] parts = normalized.split(",", -1);
        if (parts.length != 2) {
            throw new BusinessException("Sort phải có định dạng field,asc hoặc field,desc");
        }

        String field = parts[0].trim().toLowerCase(Locale.ROOT);
        String direction = parts[1].trim().toLowerCase(Locale.ROOT);
        String column = SORT_COLUMNS.get(field);
        if (column == null) {
            throw new BusinessException("Trường sort không được hỗ trợ: " + parts[0].trim());
        }
        if (!direction.equals("asc") && !direction.equals("desc")) {
            throw new BusinessException("Chiều sort chỉ được là asc hoặc desc");
        }
        return new SortSpec(column, direction.toUpperCase(Locale.ROOT));
    }

    private String buildWhereClause(DauSachListQuery query, MapSqlParameterSource params) {
        List<String> conditions = new ArrayList<>();
        if (query.search() != null) {
            params.addValue("search", "%" + escapeLike(query.search()) + "%");
            conditions.add("""
                    (ds.MaDauSach COLLATE Vietnamese_100_CI_AI LIKE :search ESCAPE '\\'
                     OR ds.TenDauSach COLLATE Vietnamese_100_CI_AI LIKE :search ESCAPE '\\'
                     OR ds.ISBN COLLATE Vietnamese_100_CI_AI LIKE :search ESCAPE '\\'
                     OR EXISTS (
                         SELECT 1 FROM DAUSACH_TACGIA dstg
                         INNER JOIN TACGIA tg ON tg.MaTacGia = dstg.MaTacGia
                         WHERE dstg.MaDauSach = ds.MaDauSach
                           AND tg.TenTacGia COLLATE Vietnamese_100_CI_AI LIKE :search ESCAPE '\\'
                     )
                     OR EXISTS (
                         SELECT 1 FROM DAUSACH_THELOAI dstl
                         INNER JOIN THELOAI tl ON tl.MaTheLoai = dstl.MaTheLoai
                         WHERE dstl.MaDauSach = ds.MaDauSach
                           AND tl.TenTheLoai COLLATE Vietnamese_100_CI_AI LIKE :search ESCAPE '\\'
                     ))
                    """);
        }
        addInFilter(conditions, params, "ds.TrangThai", "statusIds", query.statusIds());
        addInFilter(conditions, params, "ds.MaNhaXuatBan", "publisherIds", query.publisherIds());
        addExistsFilter(conditions, params, "DAUSACH_TACGIA", "MaTacGia", "authorIds", query.authorIds());
        addExistsFilter(conditions, params, "DAUSACH_THELOAI", "MaTheLoai", "categoryIds", query.categoryIds());
        if (query.yearFrom() != null) {
            conditions.add("ds.NamXuatBan >= :yearFrom");
            params.addValue("yearFrom", query.yearFrom());
        }
        if (query.yearTo() != null) {
            conditions.add("ds.NamXuatBan <= :yearTo");
            params.addValue("yearTo", query.yearTo());
        }
        if (query.language() != null) {
            conditions.add("ds.NgonNgu COLLATE Vietnamese_100_CI_AI = :language");
            params.addValue("language", query.language());
        }
        if (query.hasIsbn() != null) {
            conditions.add(query.hasIsbn() ? "NULLIF(LTRIM(RTRIM(ds.ISBN)), '') IS NOT NULL" : "NULLIF(LTRIM(RTRIM(ds.ISBN)), '') IS NULL");
        }
        if (query.hasCover() != null) {
            conditions.add(query.hasCover() ? "NULLIF(LTRIM(RTRIM(ds.AnhBia)), '') IS NOT NULL" : "NULLIF(LTRIM(RTRIM(ds.AnhBia)), '') IS NULL");
        }
        return conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions);
    }

    private void addInFilter(List<String> conditions, MapSqlParameterSource params, String column, String name, List<String> values) {
        if (!values.isEmpty()) {
            conditions.add(column + " IN (:" + name + ")");
            params.addValue(name, values);
        }
    }

    private void addExistsFilter(List<String> conditions, MapSqlParameterSource params, String table, String column, String name, List<String> values) {
        if (!values.isEmpty()) {
            conditions.add("EXISTS (SELECT 1 FROM " + table + " rel WHERE rel.MaDauSach = ds.MaDauSach AND rel." + column + " IN (:" + name + "))");
            params.addValue(name, values);
        }
    }

    private RelationData loadRelations(
            List<BookRow> rows,
            String relationTable,
            String entityTable,
            String idColumn,
            String nameColumn
    ) {
        if (rows.isEmpty()) {
            return new RelationData(Map.of(), Map.of());
        }
        List<String> ids = rows.stream().map(BookRow::id).toList();
        Map<String, List<String>> relationIds = new LinkedHashMap<>();
        Map<String, List<String>> relationNames = new LinkedHashMap<>();
        jdbcTemplate.query(
                "SELECT rel.MaDauSach, rel." + idColumn + " AS RelationId, entity." + nameColumn
                        + " AS RelationName FROM " + relationTable + " rel INNER JOIN " + entityTable
                        + " entity ON entity." + idColumn + " = rel." + idColumn
                        + " WHERE rel.MaDauSach IN (:ids) ORDER BY rel.MaDauSach, entity." + nameColumn + ", rel." + idColumn,
                Map.of("ids", ids),
                (RowCallbackHandler) rs -> {
                    relationIds
                        .computeIfAbsent(rs.getString("MaDauSach"), ignored -> new ArrayList<>())
                        .add(rs.getString("RelationId"));
                    relationNames
                            .computeIfAbsent(rs.getString("MaDauSach"), ignored -> new ArrayList<>())
                            .add(rs.getString("RelationName"));
                }
        );
        return new RelationData(relationIds, relationNames);
    }

    private String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    record SortSpec(String column, String direction) {
    }

    private record BookRow(
            String id,
            String publisherId,
            String publisherName,
            String title,
            String isbn,
            Integer year,
            String language,
            Integer pages,
            String description,
            String cover,
            BigDecimal value,
            String status,
            Long totalCopies,
            Long availableCopies
    ) {
        DauSachResponse toResponse(
                List<String> authorIds,
                List<String> authorNames,
                List<String> categoryIds,
                List<String> categoryNames
        ) {
            return new DauSachResponse(id, publisherId, title, isbn, year, language, pages,
                    description, cover, value, status, authorIds, categoryIds,
                    publisherName, authorNames, categoryNames, totalCopies, availableCopies);
        }
    }

    private record RelationData(
            Map<String, List<String>> ids,
            Map<String, List<String>> names
    ) {
    }
}
