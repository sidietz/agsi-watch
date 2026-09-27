package com.oberamsystems.ai.asgi_watch.repository;

import com.oberamsystems.ai.asgi_watch.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RegionRepository extends JpaRepository<Region, String> {

    @Query("""
        SELECT r, rs
        FROM Region r
        LEFT JOIN RegionStorageData rs ON rs.region = r AND rs.gasDay = :gasDay
        ORDER BY r.code ASC
    """)
    List<Object[]> findRegionsWithStorageForGasDay(@Param("gasDay") LocalDate gasDay);
}
