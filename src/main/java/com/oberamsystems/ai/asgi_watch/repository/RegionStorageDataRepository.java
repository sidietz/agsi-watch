package com.oberamsystems.ai.asgi_watch.repository;

import com.oberamsystems.ai.asgi_watch.entity.RegionStorageData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RegionStorageDataRepository extends JpaRepository<RegionStorageData, Long> {

    @Query("""
        SELECT rs
        FROM RegionStorageData rs
        JOIN FETCH rs.region r
        WHERE rs.gasDay = :gasDay
        ORDER BY r.code ASC
    """)
    List<RegionStorageData> findByGasDayWithRegion(@Param("gasDay") LocalDate gasDay);
}
