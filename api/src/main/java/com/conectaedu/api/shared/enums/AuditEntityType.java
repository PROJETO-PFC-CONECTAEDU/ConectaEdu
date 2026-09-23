package com.conectaedu.api.shared.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuditEntityType {
    SCHOOL("Escola"),
    UNIVERSITY("Universidade"),
    USER("Usuário"),
    STUDENT("Estudante");

    private final String label;
}
