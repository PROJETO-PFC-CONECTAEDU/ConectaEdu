package com.conectaedu.api.shared.audit.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

//o conjunto de alteracoes guarda apenas os campos no qual valor realmente mudou e descreve cada mudanca no formato: "campo: de antes para depois".
class AuditDiffTest {

    //sem nenhum campo informado nao ha o que descrever.
    @Test
    void deveNascerVazioESemDescricao() {
        AuditDiff alteracoes = AuditDiff.create();

        assertTrue(alteracoes.isEmpty());
        assertEquals("", alteracoes.describe());
    }

    //caminho feliz.
    @Test
    void deveRegistrarCampoQuandoOValorMudou() {
        AuditDiff alteracoes = AuditDiff.create()
                .field("name", "Universidade ABCD", "Universidade ABCDE");

        assertFalse(alteracoes.isEmpty());
        assertEquals("name: de \"Universidade ABCD\" para \"Universidade ABCDE\"", alteracoes.describe());
    }

    @Test
    void deveIgnorarCampoQuandoOValorContinuaIgual() {
        AuditDiff alteracoes = AuditDiff.create()
                .field("name", "Universidade ABCD", "Universidade ABCD")
                .field("requiredInternshipHours", 100, 100)
                .field("address", null, null);

        assertTrue(alteracoes.isEmpty());
        assertEquals("", alteracoes.describe());
    }

    //ordem em que foram informadas, separadas por ponto e virgula.
    @Test
    void deveDescreverVariasMudancasNaOrdemEmQueForamInformadas() {
        AuditDiff alteracoes = AuditDiff.create()
                .field("name", "Universidade ABCD", "Universidade ABCDE")
                .field("coordinator", "Joao Paulo", "Joao Paulo")
                .field("requiredInternshipHours", 100, 200);

        assertEquals(
                "name: de \"Universidade ABCD\" para \"Universidade ABCDE\"; "
                        + "requiredInternshipHours: de \"100\" para \"200\"",
                alteracoes.describe());
    }

    @Test
    void deveDescreverValorNuloSemAspas() {
        AuditDiff alteracoes = AuditDiff.create()
                .field("address", null, "R. teste, 200")
                .field("coordinator", "Joao Paulo", null);

        assertFalse(alteracoes.isEmpty());
        assertEquals(
                "address: de null para \"R. teste, 200\"; coordinator: de \"Joao Paulo\" para null",
                alteracoes.describe());
    }
}
