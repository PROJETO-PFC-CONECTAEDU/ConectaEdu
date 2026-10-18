package com.conectaedu.api.modules.lesson.repository;

import com.conectaedu.api.modules.lesson.domain.Lesson;
import com.conectaedu.api.shared.enums.LessonStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface LessonRepository extends JpaRepository<Lesson, UUID> {

    //Na ordem natural compila igual e deixa marcar aula em cima de aula.

    boolean existsByStudent_IdAndStatusInAndScheduledStartLessThanAndScheduledEndGreaterThan(
            UUID studentId, Collection<LessonStatus> status,
            LocalDateTime newEnd, LocalDateTime newStart);

    //Confirma que a demanda teve algum cancelamento, sem precisar saber qual aula foi.

    boolean existsByDemand_IdAndStatus(UUID demandId, LessonStatus status);

    List<Lesson> findByStudent_IdAndScheduledStartBetweenOrderByScheduledStartAsc(
            UUID studentId, LocalDateTime from, LocalDateTime to);

    List<Lesson> findByStudent_IdAndStatusAndScheduledStartBetweenOrderByScheduledStartAsc(
            UUID studentId, LessonStatus status, LocalDateTime from, LocalDateTime to);

    List<Lesson> findByStudent_IdAndStatus(UUID studentId, LessonStatus status);

    //O prazo de confirmação é o menor entre 3 dias do agendamento e 24h antes da aula.
    //Venceu se passou de um dos dois, e é esse OR que a consulta derivada não escreve.

    @Query("""
            select l from Lesson l
            where l.status = :status
              and (l.createdAt < :createdBefore or l.scheduledStart < :startingBefore)
            """)
    List<Lesson> findExpiredConfirmations(@Param("status") LessonStatus status,
                                          @Param("createdBefore") LocalDateTime createdBefore,
                                          @Param("startingBefore") LocalDateTime startingBefore);

    List<Lesson> findByStatusAndScheduledEndBefore(LessonStatus status, LocalDateTime deadline);

    List<Lesson> findByStatusAndRegisteredAtBeforeAndReminderSentAtIsNull(
            LessonStatus status, LocalDateTime deadline);

    List<Lesson> findByStatusAndRegisteredAtBeforeAndEscalatedAtIsNull(
            LessonStatus status, LocalDateTime deadline);
}
