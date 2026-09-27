package com.oberamsystems.ai.asgi_watch.repository;

import com.oberamsystems.ai.asgi_watch.entity.Operator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OperatorRepository extends JpaRepository<Operator, String> {

    @Query("""
        SELECT o, os
        FROM Operator o
        LEFT JOIN OperatorStorageData os ON os.operator = o AND os.gasDay = :gasDay
        ORDER BY os.gasInStorage DESC NULLS LAST, o.name ASC
    """)
    List<Object[]> findOperatorsWithStorageForGasDay(@Param("gasDay") LocalDate gasDay);
}
