package com.conectaedu.api.modules.user.school_director.repository;

import com.conectaedu.api.modules.user.school_director.domain.SchoolDirector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SchoolDirectorRepository extends JpaRepository<SchoolDirector, UUID> {
}
