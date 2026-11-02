package com.conectaedu.api.modules.internship.support;

import java.math.BigDecimal;
import java.math.RoundingMode;

//Os números da entrega ficam aqui para não aparecerem repetidos dentro do service.

public final class InternshipRules {

    private InternshipRules() {}

    private static final BigDecimal MINUTES_IN_AN_HOUR = BigDecimal.valueOf(60);

    //Vale quando a universidade ainda não definiu a carga dela.

    public static final int DEFAULT_REQUIRED_HOURS = 400;

    //Uma casa decimal é o que separa 1h30 de 1h, a aula de 90 minutos vira 1,5.

    public static final int HOURS_SCALE = 1;

    public static BigDecimal toHours(long minutes) {
        return BigDecimal.valueOf(minutes)
                .divide(MINUTES_IN_AN_HOUR, HOURS_SCALE, RoundingMode.HALF_UP);
    }
}
