package com.conectaedu.api.modules.demand.repository;

import com.conectaedu.api.modules.demand.domain.Demand;
import com.conectaedu.api.shared.enums.DemandStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface DemandRepository extends JpaRepository<Demand, UUID> {
    List<Demand> findByStatus(DemandStatus status);

    List<Demand> findByStatusAndStudentId(DemandStatus status, UUID studentId);

    long countByStatus(DemandStatus status);

    long countBySchoolId(UUID schoolId);

    long countBySchoolIdAndStatus(UUID schoolId, DemandStatus status);

    long countByStudentIdAndStatus(UUID studentId, DemandStatus status);
    
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(d) FROM Demand d JOIN d.candidates c WHERE c.id = :studentId")
    long countByCandidateId(UUID studentId);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(CAST(d.totalHours AS double)) FROM Demand d WHERE d.student.id = :studentId AND d.status = 'FINISHED'")
    Double sumTotalHoursByStudentId(UUID studentId);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(CAST(d.totalHours AS double)) FROM Demand d WHERE d.student.university.id = :universityId AND d.status = 'FINISHED'")
    Double sumTotalHoursByUniversityId(UUID universityId);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(d) FROM Demand d WHERE d.student.university.id = :universityId AND d.status = 'FINISHED'")
    long countFinishedDemandsByUniversityId(UUID universityId);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(DISTINCT d.student) FROM Demand d WHERE d.student.university.id = :universityId AND d.status = 'ONGOING'")
    long countActiveStudentsByUniversityId(UUID universityId);
    
    java.util.Optional<Demand> findFirstByStudentIdAndStatus(UUID studentId, DemandStatus status);
}
