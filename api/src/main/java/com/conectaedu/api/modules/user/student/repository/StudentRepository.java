package com.conectaedu.api.modules.user.student.repository;

import com.conectaedu.api.modules.user.student.domain.Student;
import com.conectaedu.api.shared.enums.StudentStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {
    @EntityGraph(attributePaths = "interestAreas")
    List<Student> findByUniversityId(UUID universityId);

    @Override
    @EntityGraph(attributePaths = "interestAreas")
    List<Student> findAll();

    @Query("SELECT COUNT(DISTINCT s) FROM Student s " +
           "LEFT JOIN Demand d ON d.student.id = s.id " +
           "WHERE s.status = :status " +
           "AND (:universityId IS NULL OR s.university.id = :universityId) " +
           "AND (:schoolId IS NULL OR d.school.id = :schoolId) " +
           "AND (:demandId IS NULL OR d.id = :demandId)")
    long countByStatusAndFilters(
            @Param("status") StudentStatus status,
            @Param("universityId") UUID universityId,
            @Param("schoolId") UUID schoolId,
            @Param("demandId") UUID demandId);

    @EntityGraph(attributePaths = "interestAreas")
    @Query("SELECT DISTINCT s FROM Student s WHERE s.id IN (" +
           "SELECT d.student.id FROM Demand d WHERE d.school.id = :schoolId AND d.student IS NOT NULL) OR s.id IN (" +
           "SELECT c.id FROM Demand d JOIN d.candidates c WHERE d.school.id = :schoolId)")
    List<Student> findAllBySchoolId(@Param("schoolId") UUID schoolId);

    @Query("SELECT COUNT(s) > 0 FROM Student s WHERE s.id = :studentId AND (s.id IN (" +
           "SELECT d.student.id FROM Demand d WHERE d.school.id = :schoolId AND d.student IS NOT NULL) OR s.id IN (" +
           "SELECT c.id FROM Demand d JOIN d.candidates c WHERE d.school.id = :schoolId))")
    boolean isStudentLinkedToSchool(@Param("studentId") UUID studentId, @Param("schoolId") UUID schoolId);

    @EntityGraph(attributePaths = "interestAreas")
    Optional<Student> findWithInterestAreasById(UUID id);

    @EntityGraph(attributePaths = "interestAreas")
    @Query("SELECT s FROM Student s WHERE " +
           "(:search IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Student> findBySearch(@Param("search") String search);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Student s WHERE s.id = :id")
    Optional<Student> findByIdWithLock(UUID id);
}
