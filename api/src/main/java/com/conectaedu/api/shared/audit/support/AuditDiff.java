package com.conectaedu.api.shared.audit.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AuditDiff {

    private record Change(String field, Object before, Object after) {}

    private final List<Change> changes = new ArrayList<>();

    public static AuditDiff create() { return new AuditDiff(); }

    public AuditDiff field(String field, Object before, Object after) {
        if (!Objects.equals(before, after)) {
            changes.add(new Change(field, before, after));
        }
        return this;
    }

    public boolean isEmpty() { return changes.isEmpty(); }

    public String describe() {
        return changes.stream()
                .map(c -> "%s: de %s para %s".formatted(c.field(), fmt(c.before()), fmt(c.after())))
                .collect(Collectors.joining("; "));
    }

    private static String fmt(Object v) { return v == null ? "null" : "\"" + v + "\""; }
}