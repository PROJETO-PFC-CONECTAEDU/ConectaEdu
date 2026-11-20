package com.conectaedu.api.modules.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardFilterDTO {
    private UUID schoolId;
    private UUID demandId;
    private UUID studentId;
    private LocalDate startDate;
    private LocalDate endDate;
}
