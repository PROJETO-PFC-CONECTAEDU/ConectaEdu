package com.conectaedu.api.modules.feedback.repository;

import com.conectaedu.api.modules.feedback.domain.ClassEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

//aqui o autor já é o estudante.
public interface ClassEvaluationRepository extends JpaRepository<ClassEvaluation, UUID> {

    boolean existsByDemand_IdAndStudent_Id(UUID demandId, UUID studentId);

    List<ClassEvaluation> findByDemand_IdOrderByCreatedAtDesc(UUID demandId);
}
