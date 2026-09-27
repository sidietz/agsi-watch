package com.oberamsystems.ai.asgi_watch.repository;

import com.oberamsystems.ai.asgi_watch.entity.CountryStorageData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CountryStorageDataRepository extends JpaRepository<CountryStorageData, Long> {

    @Query("""
        SELECT DISTINCT c.gasDay
        FROM CountryStorageData c
        ORDER BY c.gasDay DESC
    """)
    List<LocalDate> findDistinctGasDays();

    @Query("""
        SELECT MAX(c.gasDay)
        FROM CountryStorageData c
    """)
    LocalDate findLatestGasDay();

    @Query("""
        SELECT cs
        FROM CountryStorageData cs
        JOIN FETCH cs.country c
        WHERE cs.gasDay = :gasDay
    """)
    List<CountryStorageData> findByGasDayWithCountry(@Param("gasDay") LocalDate gasDay);
}
