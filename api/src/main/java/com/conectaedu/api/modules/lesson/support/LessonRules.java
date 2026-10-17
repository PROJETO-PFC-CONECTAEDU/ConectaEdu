package com.conectaedu.api.modules.lesson.support;

import com.conectaedu.api.shared.enums.LessonStatus;

import java.util.EnumSet;
import java.util.Set;

//Os números das regras ficam aqui para não aparecerem repetidos dentro dos services.

public final class LessonRules {

    private LessonRules() {}

    public static final int MIN_NOTICE_DAYS = 2;
    public static final int MIN_ENROLLED = 3;
    public static final int MAX_ENROLLED = 10;
    public static final int CONFIRMATION_DEADLINE_DAYS = 3;
    public static final int CONFIRMATION_CUTOFF_HOURS = 24;
    public static final int REGISTRATION_WINDOW_HOURS = 48;
    public static final int SIGNATURE_REMINDER_DAYS = 5;
    public static final int ESCALATION_DAYS = 10;

    //Aula cancelada ou não realizada não ocupa agenda.

    public static final Set<LessonStatus> BUSY_STATUSES = EnumSet.of(
            LessonStatus.SCHEDULED,
            LessonStatus.CONFIRMED,
            LessonStatus.COMPLETED,
            LessonStatus.VALIDATED);
}