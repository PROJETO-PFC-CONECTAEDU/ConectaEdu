package com.conectaedu.api.modules.demand.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class DemandFinishedEvent extends ApplicationEvent {
    private final UUID demandId;

    public DemandFinishedEvent(Object source, UUID demandId) {
        super(source);
        this.demandId = demandId;
    }
}
