package com.conectaedu.api.modules.lesson.repository;

import com.conectaedu.api.modules.lesson.domain.Lesson;
import com.conectaedu.api.shared.enums.LessonStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface LessonRepository extends JpaRepository<Lesson, UUID> {

    //Na ordem natural compila igual e deixa marcar aula em cima de aula.

    @Query("SELECT COUNT(l) > 0 FROM Lesson l WHERE l.student.id = :studentId " +
            "AND l.status IN :status " +
            "AND CAST(l.scheduledStart AS date) = CAST(:date AS date)")
    boolean existsByStudentIdAndDate(
            @Param("studentId") UUID studentId,
            @Param("status") Collection<LessonStatus> status,
            @Param("date") LocalDateTime date);

    @Query("SELECT COUNT(l) > 0 FROM Lesson l WHERE l.demand.id = :demandId " +
            "AND l.status IN :status " +
            "AND CAST(l.scheduledStart AS date) = CAST(:date AS date)")
    boolean existsByDemandIdAndDate(
            @Param("demandId") UUID demandId,
            @Param("status") Collection<LessonStatus> status,
            @Param("date") LocalDateTime date);

    //Confirma que a demanda teve algum cancelamento, sem precisar saber qual aula foi.

    boolean existsByDemand_IdAndStatus(UUID demandId, LessonStatus status);

    boolean existsByDemand_IdAndStudent_IdAndStatusIn(UUID demandId, UUID studentId, Collection<LessonStatus> statuses);
    
    @Query("SELECT COUNT(l1) > 0 FROM Lesson l1, Lesson l2 " +
           "WHERE l1.demand.id = :demandId AND l2.student.id = :studentId " +
           "AND l1.status IN :busyStatuses AND l2.status IN :busyStatuses " +
           "AND l1.demand.id <> l2.demand.id " +
           "AND CAST(l1.scheduledStart AS date) = CAST(l2.scheduledStart AS date)")
    boolean existsOverlapBetweenDemandAndStudent(
            @Param("demandId") UUID demandId,
            @Param("studentId") UUID studentId,
            @Param("busyStatuses") Collection<LessonStatus> busyStatuses);

    List<Lesson> findByDemand_Id(UUID demandId);

    List<Lesson> findByStudent_IdAndScheduledStartBetweenOrderByScheduledStartAsc(
            UUID studentId, LocalDateTime from, LocalDateTime to);

    List<Lesson> findByStudent_IdAndStatusAndScheduledStartBetweenOrderByScheduledStartAsc(
            UUID studentId, LessonStatus status, LocalDateTime from, LocalDateTime to);

    List<Lesson> findByStudent_IdAndStatus(UUID studentId, LessonStatus status);

    List<Lesson> findBySchool_IdOrderByScheduledStartAsc(UUID schoolId);

    boolean existsByDemand_IdAndStatusInAndScheduledStartLessThanAndScheduledEndGreaterThan(
            UUID demandId, Collection<LessonStatus> statuses, LocalDateTime end, LocalDateTime start);

    boolean existsByStudent_IdAndStatusInAndScheduledStartLessThanAndScheduledEndGreaterThan(
            UUID studentId, Collection<LessonStatus> statuses, LocalDateTime end, LocalDateTime start);

    @Query(value = """
            SELECT TO_CHAR(l.scheduled_start, 'MM/YYYY') as period, COUNT(l.id) as count
            FROM lesson l
            INNER JOIN users s ON l.student_id = s.id
            WHERE (:universityId IS NULL OR s.university_id = :universityId)
              AND (:schoolId IS NULL OR l.school_id = :schoolId)
              AND (:demandId IS NULL OR l.demand_id = :demandId)
              AND (:studentId IS NULL OR l.student_id = :studentId)
              AND (:startDate IS NULL OR l.scheduled_start >= :startDate)
              AND (:endDate IS NULL OR l.scheduled_start <= :endDate)
              AND l.status = 'VALIDATED'
            GROUP BY period
            ORDER BY MIN(l.scheduled_start) ASC
            """, nativeQuery = true)
    List<Map<String, Object>> countByFiltersAndPeriod(
            @Param("universityId") UUID universityId,
            @Param("schoolId") UUID schoolId,
            @Param("demandId") UUID demandId,
            @Param("studentId") UUID studentId,
            @Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate);

    @Query(value = """
            SELECT TO_CHAR(l.scheduled_start, 'MM/YYYY') as period, d.title as demand, COUNT(l.id) as count
            FROM lesson l
            INNER JOIN demand d ON l.demand_id = d.id
            WHERE (:schoolId IS NULL OR l.school_id = :schoolId)
              AND (:demandId IS NULL OR l.demand_id = :demandId)
              AND (:startDate IS NULL OR l.scheduled_start >= :startDate)
              AND (:endDate IS NULL OR l.scheduled_start <= :endDate)
              AND l.status = 'VALIDATED'
            GROUP BY period, demand
            ORDER BY MIN(l.scheduled_start) ASC
            """, nativeQuery = true)
    List<Map<String, Object>> countBySchoolAndPeriodAndDemandFiltered(
            @Param("schoolId") UUID schoolId,
            @Param("demandId") UUID demandId,
            @Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate);

    @Query(value = """
            SELECT l.nine_box_performance as performance, l.nine_box_potential as potential, COUNT(l.id) as count
            FROM lesson l
            INNER JOIN users s ON l.student_id = s.id
            WHERE (:universityId IS NULL OR s.university_id = :universityId)
              AND (:schoolId IS NULL OR l.school_id = :schoolId)
              AND (:demandId IS NULL OR l.demand_id = :demandId)
              AND (:studentId IS NULL OR l.student_id = :studentId)
              AND (:startDate IS NULL OR l.scheduled_start >= :startDate)
              AND (:endDate IS NULL OR l.scheduled_start <= :endDate)
              AND l.status = 'VALIDATED' AND l.nine_box_performance IS NOT NULL AND l.nine_box_potential IS NOT NULL
            GROUP BY performance, potential
            """, nativeQuery = true)
    List<Map<String, Object>> countNineBoxByFilters(
            @Param("universityId") UUID universityId,
            @Param("schoolId") UUID schoolId,
            @Param("demandId") UUID demandId,
            @Param("studentId") UUID studentId,
            @Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate);

    @Query("SELECT COUNT(l) FROM Lesson l WHERE (:schoolId IS NULL OR l.school.id = :schoolId) " +
            "AND (:demandId IS NULL OR l.demand.id = :demandId) " +
            "AND (:startDate IS NULL OR l.scheduledStart >= :startDate) " +
            "AND (:endDate IS NULL OR l.scheduledStart <= :endDate) " +
            "AND l.status = 'VALIDATED'")
    long countValidatedByFilters(
            @Param("schoolId") UUID schoolId, 
            @Param("demandId") UUID demandId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);


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

    @Query("SELECT l FROM Lesson l WHERE l.student.id = :studentId AND l.status = 'VALIDATED' " +
            "AND (:startDate IS NULL OR l.scheduledStart >= :startDate) " +
            "AND (:endDate IS NULL OR l.scheduledStart <= :endDate)")
    List<Lesson> findValidatedByStudentAndPeriod(
            @Param("studentId") UUID studentId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
