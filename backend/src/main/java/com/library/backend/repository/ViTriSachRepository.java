package com.library.backend.repository;

import com.library.backend.entity.ViTriSach;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ViTriSachRepository extends JpaRepository<ViTriSach, String> {
    @Query(value = """
            SELECT CASE WHEN COUNT(*) > 0 THEN CAST(1 AS bit) ELSE CAST(0 AS bit) END
            FROM VITRISACH vt
            INNER JOIN KESACH ks ON ks.MaKeSach = vt.MaKeSach
            INNER JOIN KHU k ON k.MaKhu = ks.MaKhu
            INNER JOIN CHINHANH cn ON cn.MaChiNhanh = k.MaChiNhanh
            WHERE vt.MaViTri = :locationId
              AND k.MaChiNhanh = :branchId
              AND cn.TrangThai = N'Hoạt động'
            """, nativeQuery = true)
    boolean existsActiveInBranch(@Param("locationId") String locationId, @Param("branchId") String branchId);
}
