package com.conectaedu.api.modules.internship.support;

//Os números da entrega ficam aqui para não aparecerem repetidos dentro do service.

public final class InternshipRules {

    private InternshipRules() {}

    //Vale quando a universidade ainda não definiu a carga dela.

    public static final int DEFAULT_REQUIRED_HOURS = 400;

    //Uma casa decimal é o que separa 1h30 de 1h, a aula de 90 minutos vira 1,5.

    public static final int HOURS_SCALE = 1;
}
