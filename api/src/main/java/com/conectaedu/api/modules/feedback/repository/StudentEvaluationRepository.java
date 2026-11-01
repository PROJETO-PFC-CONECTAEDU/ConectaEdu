package com.conectaedu.api.modules.feedback.repository;

import com.conectaedu.api.modules.feedback.domain.StudentEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
