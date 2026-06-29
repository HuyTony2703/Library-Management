package com.library.backend.repository;

import com.library.backend.entity.ChiTietPhieuMuon;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChiTietPhieuMuonRepository extends JpaRepository<ChiTietPhieuMuon, String> {

    List<ChiTietPhieuMuon> findByMaPhieuMuon(String maPhieuMuon);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ct FROM ChiTietPhieuMuon ct WHERE ct.maChiTietMuon = :maChiTietMuon")
    Optional<ChiTietPhieuMuon> findByIdForUpdate(
            @Param("maChiTietMuon") String maChiTietMuon
    );
}
