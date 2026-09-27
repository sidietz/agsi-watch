package com.oberamsystems.ai.asgi_watch.repository;

import com.oberamsystems.ai.asgi_watch.dto.CountryDto;
import com.oberamsystems.ai.asgi_watch.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CountryRepository extends JpaRepository<Country, String> {

    @Query("""
        SELECT new com.oberamsystems.ai.asgi_watch.dto.CountryDto(
            c.code, c.name, COUNT(DISTINCT f.code)
        )
        FROM Country c
        JOIN Facility f ON f.country = c
        GROUP BY c.code, c.name
        ORDER BY COUNT(DISTINCT f.code) DESC, c.name ASC
    """)
    List<CountryDto> findCountriesWithFacilityCount();

    @Query("""
        SELECT c, cs
        FROM Country c
        LEFT JOIN CountryStorageData cs ON cs.country = c AND cs.gasDay = :gasDay
        ORDER BY cs.gasInStorage DESC NULLS LAST, c.name ASC
    """)
    List<Object[]> findCountriesWithStorageForGasDay(@Param("gasDay") LocalDate gasDay);
}
