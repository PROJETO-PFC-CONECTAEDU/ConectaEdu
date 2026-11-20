package com.conectaedu.api.modules.feedback.repository;

import com.conectaedu.api.modules.feedback.domain.StudentEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface StudentEvaluationRepository extends JpaRepository<StudentEvaluation, UUID> {

    //o mesmo autor não avalia duas vezes o mesmo estudante na mesma necessidade.

    boolean existsByDemand_IdAndStudent_IdAndEvaluatedBy(UUID demandId, UUID studentId, UUID evaluatedBy);

    //basta uma avaliação do diretor para a necessidade deixar de ser pendente.

    boolean existsByDemand_Id(UUID demandId);

    List<StudentEvaluation> findByStudent_IdAndCreatedAtBetweenOrderByCreatedAtDesc(
            UUID studentId, LocalDateTime from, LocalDateTime to);

    List<StudentEvaluation> findByDemand_IdOrderByCreatedAtDesc(UUID demandId);


    @Query("SELECT AVG((e.knowledge + e.punctuality + e.teaching + e.commitment + e.empathy) / 5.0) " +
           "FROM StudentEvaluation e " +
           "WHERE (:universityId IS NULL OR e.student.university.id = :universityId) " +
           "AND (:schoolId IS NULL OR e.demand.school.id = :schoolId) " +
           "AND (:demandId IS NULL OR e.demand.id = :demandId) " +
           "AND (:studentId IS NULL OR e.student.id = :studentId)")
    Double getAverageScoreByFilters(
            @Param("universityId") UUID universityId,
            @Param("schoolId") UUID schoolId,
            @Param("demandId") UUID demandId,
            @Param("studentId") UUID studentId);
}
