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
           "FROM StudentEvaluation e WHERE e.student.university.id = :universityId")
    Double getAverageScoreByUniversity(@Param("universityId") UUID universityId);

    @Query("SELECT AVG((e.knowledge + e.punctuality + e.teaching + e.commitment + e.empathy) / 5.0) " +
           "FROM StudentEvaluation e WHERE e.demand.school.id = :schoolId")
    Double getAverageScoreBySchool(@Param("schoolId") UUID schoolId);
}
