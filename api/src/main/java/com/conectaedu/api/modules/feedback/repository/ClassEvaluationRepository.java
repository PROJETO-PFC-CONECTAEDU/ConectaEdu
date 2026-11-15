package com.conectaedu.api.modules.feedback.repository;

import com.conectaedu.api.modules.feedback.domain.ClassEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

//aqui o autor já é o estudante.
public interface ClassEvaluationRepository extends JpaRepository<ClassEvaluation, UUID> {

    boolean existsByDemand_IdAndStudent_Id(UUID demandId, UUID studentId);

    List<ClassEvaluation> findByDemand_IdOrderByCreatedAtDesc(UUID demandId);

    List<ClassEvaluation> findByDemand_Director_IdOrderByCreatedAtDesc(UUID directorId);

    @Query("SELECT AVG((e.engagement + e.progress + e.infrastructure) / 3.0) " +
           "FROM ClassEvaluation e WHERE e.demand.school.id = :schoolId")
    Double getAverageScoreBySchool(@Param("schoolId") UUID schoolId);

    @Query("SELECT AVG((e.engagement + e.progress + e.infrastructure) / 3.0) " +
           "FROM ClassEvaluation e WHERE e.student.university.id = :universityId")
    Double getAverageScoreByUniversity(@Param("universityId") UUID universityId);
}
