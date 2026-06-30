package com.library.backend.repository;

import com.library.backend.entity.KhoanNo;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface KhoanNoRepository extends JpaRepository<KhoanNo, String> {

    List<KhoanNo> findByMaDocGiaAndTrangThaiNotOrderByNgayPhatSinhAsc(
            String maDocGia,
            String trangThai
    );

    List<KhoanNo> findByMaDocGiaOrderByNgayPhatSinhDesc(String maDocGia);

    @Query("""
            SELECT kn FROM KhoanNo kn
            WHERE kn.maDocGia = :maDocGia
              AND kn.trangThai <> :trangThai
            ORDER BY kn.ngayPhatSinh ASC, kn.maKhoanNo ASC
            """)
    List<KhoanNo> findOutstandingByReaderForPreview(
            @Param("maDocGia") String maDocGia,
            @Param("trangThai") String trangThai
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT kn FROM KhoanNo kn WHERE kn.maKhoanNo = :maKhoanNo")
    Optional<KhoanNo> findByIdForUpdate(@Param("maKhoanNo") String maKhoanNo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT kn FROM KhoanNo kn
            WHERE kn.maDocGia = :maDocGia
              AND kn.trangThai <> :trangThai
            ORDER BY kn.ngayPhatSinh ASC, kn.maKhoanNo ASC
            """)
    List<KhoanNo> findOutstandingByReaderForUpdate(
            @Param("maDocGia") String maDocGia,
            @Param("trangThai") String trangThai
    );
}
