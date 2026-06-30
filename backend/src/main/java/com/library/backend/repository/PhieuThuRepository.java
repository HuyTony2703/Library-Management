package com.library.backend.repository;

import com.library.backend.entity.PhieuThu;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PhieuThuRepository extends JpaRepository<PhieuThu, String> {

    List<PhieuThu> findByMaDocGiaOrderByNgayThuDesc(String maDocGia);

    Optional<PhieuThu> findByIdempotencyKey(String idempotencyKey);

    boolean existsByMaGiaoDichNgoai(String maGiaoDichNgoai);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT pt FROM PhieuThu pt WHERE pt.maPhieuThu = :maPhieuThu")
    Optional<PhieuThu> findByIdForUpdate(@Param("maPhieuThu") String maPhieuThu);
}
