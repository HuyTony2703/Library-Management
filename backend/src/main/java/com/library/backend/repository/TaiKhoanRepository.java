package com.library.backend.repository;

import com.library.backend.entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TaiKhoanRepository extends JpaRepository<TaiKhoan, String> {

    boolean existsByTenDangNhap(String tenDangNhap);

    boolean existsByEmailDangNhap(String emailDangNhap);

    Optional<TaiKhoan> findByTenDangNhap(String tenDangNhap);

    Optional<TaiKhoan> findByEmailDangNhap(String emailDangNhap);

    @Query(value = """
            SELECT CASE WHEN EXISTS (
                SELECT 1 FROM DOCGIA dg
                JOIN DOCGIA_KHOA dk ON dk.MaDocGia = dg.MaDocGia
                WHERE dg.MaTaiKhoan = :accountId AND dk.PhamVi = 'LOGIN'
                  AND dk.MoKhoaLuc IS NULL
                  AND (dk.KhoaDen IS NULL OR dk.KhoaDen >= CAST(SYSDATETIME() AS DATE))
            ) THEN CAST(1 AS BIT) ELSE CAST(0 AS BIT) END
            """, nativeQuery = true)
    boolean hasActiveReaderLoginLock(@Param("accountId") String accountId);
}
