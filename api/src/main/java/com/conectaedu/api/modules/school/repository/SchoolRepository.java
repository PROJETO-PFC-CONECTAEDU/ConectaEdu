package com.conectaedu.api.modules.school.repository;

import com.conectaedu.api.modules.school.domain.School;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SchoolRepository extends JpaRepository<School, UUID> {

    boolean existsByCie(String cie);

    Optional<School> findByCie(String cie);
}
