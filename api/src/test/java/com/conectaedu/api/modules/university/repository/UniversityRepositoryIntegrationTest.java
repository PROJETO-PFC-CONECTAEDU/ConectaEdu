package com.conectaedu.api.modules.university.repository;

import com.conectaedu.api.modules.university.domain.University;
import com.conectaedu.api.shared.enums.UniversityStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

//Persistencia em H2 de teste
//o CNPJ identifica a instituicao e so universidade aprovada e ativa pode receber estudantes.

@DataJpaTest
@ActiveProfiles("test")
class UniversityRepositoryIntegrationTest {

    @Autowired
    private UniversityRepository universityRepository;

    private University universidade(String nome, String cnpj, UniversityStatus situacao, boolean ativa) {
        University universidade = new University();
        universidade.setName(nome);
        universidade.setCnpj(cnpj);
        universidade.setCoordinator("Joao P");
        universidade.setAddress("R. teste, 200");
        universidade.setStatus(situacao);
        universidade.setActive(ativa);
        return universidade;
    }

    //Salvar e recuperar pelo CNPJ
    @Test
    void deveSalvarERecuperarUniversidadePeloCnpj() {
        // preparacao
        universityRepository.save(
                universidade("Universidade ABCD", "12345671000112", UniversityStatus.PENDING, false));

        // execucao
        Optional<University> encontrada = universityRepository.findByCnpj("12345671000112");

        // verificacao
        assertTrue(encontrada.isPresent());
        assertEquals("Universidade ABCD", encontrada.get().getName());
        assertEquals(UniversityStatus.PENDING, encontrada.get().getStatus());
        assertFalse(encontrada.get().isActive());
        assertTrue(encontrada.get().getId() != null);
        assertTrue(universityRepository.existsByCnpj("12345671000112"));
        assertFalse(universityRepository.existsByCnpj("99999999000199"));
    }

    //Consulta  lista de instituicoes aptas a receber estagiarios
    //mostra apenas as aprovadas e ativas
    @Test
    void deveListarApenasUniversidadesAprovadasEAtivasEmOrdemAlfabetica() {
        // preparacao
        universityRepository.save(
                universidade("Universidade A", "11111111000111", UniversityStatus.APPROVED, true));
        universityRepository.save(
                universidade("Universidade B", "22222222000122", UniversityStatus.APPROVED, true));
        universityRepository.save(
                universidade("Universidade C", "33333333000133", UniversityStatus.PENDING, false));
        universityRepository.save(
                universidade("Universidade D", "44444444000144", UniversityStatus.REJECTED, false));

        // execucao

        List<University> aptas = universityRepository
                .findByStatusAndActiveTrueOrderByNameAsc(UniversityStatus.APPROVED);

        // verificacao
        assertEquals(2, aptas.size());
        assertEquals("Universidade A", aptas.get(0).getName());
        assertEquals("Universidade B", aptas.get(1).getName());
        assertEquals(4, universityRepository.count());
    }
}