package com.library.backend.repository;

import com.library.backend.entity.DauSach;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DauSachRepository extends JpaRepository<DauSach, String> {

    @Query(value = """
            SELECT TOP 1 * FROM DAUSACH
            WHERE REPLACE(REPLACE(UPPER(ISBN), '-', ''), ' ', '') = :isbn
            """, nativeQuery = true)
    Optional<DauSach> findByNormalizedIsbn(@Param("isbn") String isbn);
}
