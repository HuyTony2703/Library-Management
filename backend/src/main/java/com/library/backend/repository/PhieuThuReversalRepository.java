package com.library.backend.repository;

import com.library.backend.entity.PhieuThuReversal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PhieuThuReversalRepository extends JpaRepository<PhieuThuReversal, String> {

    boolean existsByMaPhieuThuGoc(String maPhieuThuGoc);

    Optional<PhieuThuReversal> findByMaPhieuThuGoc(String maPhieuThuGoc);
}
