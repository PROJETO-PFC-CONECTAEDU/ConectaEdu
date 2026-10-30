package com.conectaedu.api.modules.internship.repository;

import com.conectaedu.api.modules.user.student.domain.Student;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

//Mesmo motivo do InternshipLessonRepository, a busca por universidade é consulta
//então ela nasce aqui e o StudentRepository fica como está.

public interface InternshipStudentRepository extends Repository<Student, UUID> {

    Optional<Student> findById(UUID id);

    //Alimenta a tela em que a faculdade acompanha as horas dos estudantes dela.

    List<Student> findByUniversity_IdOrderByNameAsc(UUID universityId);
}
