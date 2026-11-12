package com.conectaedu.api.modules.school.repository;

import com.conectaedu.api.modules.school.domain.School;
import com.conectaedu.api.shared.enums.SchoolStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
class SchoolRepositoryIntegrationTest {

    @Autowired
    private SchoolRepository schoolRepository;

    private School escola(String nome, String cie) {
        School escola = new School();
        escola.setName(nome);
        escola.setCie(cie);
        escola.setDirector("Maria S");
        escola.setAddress("R. teste, 100");
        escola.setLatitude(-23.5505);
        escola.setLongitude(-46.6333);
        escola.setCreatedAt(LocalDateTime.now());
        return escola;
    }

    @Test
    void deveSalvarERecuperarEscolaPeloCie() {

        schoolRepository.save(escola("Escola Estadual ABCD", "123456"));

        Optional<School> encontrada = schoolRepository.findByCie("123456");

        assertTrue(encontrada.isPresent());
        assertNotNull(encontrada.get().getId());
        assertEquals("Escola Estadual ABCD", encontrada.get().getName());
        assertEquals(SchoolStatus.PENDING_ACTIVATION, encontrada.get().getStatus());
        assertFalse(encontrada.get().isActive());
        assertTrue(schoolRepository.existsByCie("123456"));
        assertFalse(schoolRepository.existsByCie("999999"));
        assertTrue(schoolRepository.findByCie("999999").isEmpty());
    }

    //o banco nao aceita escola sem CIE.
    @Test
    void deveRecusarEscolaSemCie() {

        School semCie = escola("Escola Sem CIE", null);

        assertThrows(DataIntegrityViolationException.class,
                () -> schoolRepository.saveAndFlush(semCie));
    }
}
