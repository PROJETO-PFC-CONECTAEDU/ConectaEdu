package com.conectaedu.api.modules.internship.repository;

import com.conectaedu.api.modules.lesson.domain.Lesson;
import com.conectaedu.api.shared.enums.LessonStatus;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.UUID;


//As consultas moram aqui porque o Spring Data aceita mais de um repositório por entidade.
//Estende Repository, não JpaRepository, o módulo só lê, e o que não é declarado não existe.

public interface InternshipLessonRepository extends Repository<Lesson, UUID> {

    //A ordem da assinatura é a ordem em que as aulas viraram hora, o service percorre
    //somando até cruzar a carga.

    List<Lesson> findByStudent_IdAndStatusOrderBySignedAtAsc(UUID studentId, LessonStatus status);

    //Pendentes e canceladas aparecem só como quantidade na tela, então não vale trazer as linhas.

    long countByStudent_IdAndStatus(UUID studentId, LessonStatus status);
}