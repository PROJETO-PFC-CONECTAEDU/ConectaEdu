package com.conectaedu.api.shared.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuditEntityType {
    SCHOOL("Escola"),
    UNIVERSITY("Universidade"),
    USER("Usuário"),
    STUDENT("Estudante"),
    SCHOOL_DIRECTOR("Diretor de Escola"),
    UNIVERSITY_ADMIN("Administrador de Universidade");

    private final String label;
}
