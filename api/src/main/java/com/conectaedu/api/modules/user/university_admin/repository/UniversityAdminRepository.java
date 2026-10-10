package com.conectaedu.api.modules.user.university_admin.repository;

import com.conectaedu.api.modules.user.university_admin.domain.UniversityAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UniversityAdminRepository extends JpaRepository<UniversityAdmin, UUID> {
}
