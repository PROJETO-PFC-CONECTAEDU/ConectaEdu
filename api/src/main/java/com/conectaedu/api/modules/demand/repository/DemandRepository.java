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
}
