package com.oberamsystems.ai.asgi_watch.repository;

import com.oberamsystems.ai.asgi_watch.entity.Facility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, String> {

    @Query("""
        SELECT f, fs
        FROM Facility f
        LEFT JOIN FacilityStorageData fs ON fs.facility = f AND fs.gasDay = :gasDay
        ORDER BY fs.gasInStorage DESC NULLS LAST, f.name ASC
    """)
    List<Object[]> findFacilitiesWithStorageForGasDay(@Param("gasDay") LocalDate gasDay);
}
