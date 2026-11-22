package com.conectaedu.api.modules.school.repository;

import com.conectaedu.api.modules.school.domain.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SchoolRepository extends JpaRepository<School, UUID> {

    boolean existsByCie(String cie);

    Optional<School> findByCie(String cie);

    List<School> findByActiveTrue();

    @Query("SELECT s FROM School s WHERE " +
           "(:search IS NULL OR LOWER(s.cie) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<School> findBySearch(@Param("search") String search);
}
