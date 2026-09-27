package com.oberamsystems.ai.asgi_watch.repository;

import com.oberamsystems.ai.asgi_watch.entity.FacilityStorageData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FacilityStorageDataRepository extends JpaRepository<FacilityStorageData, Long> {

    @Query("""
        SELECT fs
        FROM FacilityStorageData fs
        JOIN FETCH fs.facility f
        JOIN FETCH f.operator o
        WHERE f.country.code = :countryCode
          AND (CAST(:fromDate AS java.time.LocalDate) IS NULL OR fs.gasDay >= :fromDate)
          AND (CAST(:toDate AS java.time.LocalDate) IS NULL OR fs.gasDay <= :toDate)
        ORDER BY f.name ASC, fs.gasDay ASC
    """)
    List<FacilityStorageData> findFacilityHistory(
        @Param("countryCode") String countryCode,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate);
}
