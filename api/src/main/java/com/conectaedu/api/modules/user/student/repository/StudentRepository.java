package com.conectaedu.api.modules.user.student.repository;

import com.conectaedu.api.modules.user.student.domain.Student;
import com.conectaedu.api.shared.enums.StudentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {
    long countByStatus(StudentStatus status);
    long countByUniversityId(UUID universityId);
    List<Student> findByUniversityId(UUID universityId);

    @Query("SELECT DISTINCT s FROM Student s WHERE s.id IN (" +
           "SELECT d.student.id FROM Demand d WHERE d.school.id = :schoolId AND d.student IS NOT NULL) OR s.id IN (" +
           "SELECT c.id FROM Demand d JOIN d.candidates c WHERE d.school.id = :schoolId)")
    List<Student> findAllBySchoolId(@Param("schoolId") UUID schoolId);

    @Query("SELECT COUNT(s) > 0 FROM Student s WHERE s.id = :studentId AND (s.id IN (" +
           "SELECT d.student.id FROM Demand d WHERE d.school.id = :schoolId AND d.student IS NOT NULL) OR s.id IN (" +
           "SELECT c.id FROM Demand d JOIN d.candidates c WHERE d.school.id = :schoolId))")
    boolean isStudentLinkedToSchool(@Param("studentId") UUID studentId, @Param("schoolId") UUID schoolId);
}
