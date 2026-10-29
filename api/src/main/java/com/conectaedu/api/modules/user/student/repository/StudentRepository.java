package com.conectaedu.api.modules.user.student.repository;

import com.conectaedu.api.modules.user.student.domain.Student;
import com.conectaedu.api.shared.enums.StudentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {
    long countByStatus(StudentStatus status);
    long countByUniversityId(UUID universityId);
}
