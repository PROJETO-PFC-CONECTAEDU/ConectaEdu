package com.conectaedu.api.modules.university.repository;

import com.conectaedu.api.modules.university.domain.University;
import com.conectaedu.api.shared.enums.UniversityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UniversityRepository extends JpaRepository<University, UUID> {

    Boolean existsByCnpj(String cnpj);

    Optional<University> findByCnpj(String cnpj);

    List<University> findByActiveTrueOrderByNameAsc();

    //Universidades que já podem receber estudantes.
    List<University> findByStatusAndActiveTrueOrderByNameAsc(UniversityStatus status);

    @Query("SELECT u FROM University u WHERE " +
           "(:search IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "u.cnpj LIKE CONCAT('%', :search, '%'))")
    List<University> findBySearch(@Param("search") String search);
}

