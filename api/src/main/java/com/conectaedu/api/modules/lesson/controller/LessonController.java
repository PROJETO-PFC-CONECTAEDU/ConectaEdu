package com.conectaedu.api.modules.lesson.controller;

import com.conectaedu.api.modules.lesson.dto.request.LessonCancellationRequestDTO;
import com.conectaedu.api.modules.lesson.dto.request.LessonRegistrationRequestDTO;
import com.conectaedu.api.modules.lesson.dto.request.LessonSchedulingRequestDTO;
import com.conectaedu.api.modules.lesson.dto.response.LessonResponseDTO;
import com.conectaedu.api.modules.lesson.interfaces.ILessonFacade;
import com.conectaedu.api.shared.enums.LessonStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final ILessonFacade lessonFacade;

    //Quem abre a aula é a escola o estudante entra depois confirmando.

    @PostMapping
    @PreAuthorize("hasRole('SCHOOL_DIRECTOR')")
    public ResponseEntity<LessonResponseDTO> schedule(
            @RequestBody @Valid LessonSchedulingRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lessonFacade.scheduleLesson(request));
    }

    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<LessonResponseDTO> confirm(@PathVariable UUID id) {
        return ResponseEntity.ok(lessonFacade.confirmLesson(id));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('SCHOOL_DIRECTOR', 'STUDENT')")
    public ResponseEntity<LessonResponseDTO> cancel(@PathVariable UUID id,
                                                    @RequestBody @Valid LessonCancellationRequestDTO request) {
        return ResponseEntity.ok(lessonFacade.cancelLesson(id, request));
    }

    @PatchMapping("/{id}/register")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<LessonResponseDTO> register(@PathVariable UUID id,
                                                      @RequestBody @Valid LessonRegistrationRequestDTO request) {
        return ResponseEntity.ok(lessonFacade.registerLesson(id, request));
    }

    @PatchMapping("/{id}/sign")
    @PreAuthorize("hasAnyRole('SCHOOL_DIRECTOR', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<LessonResponseDTO> sign(@PathVariable UUID id) {
        return ResponseEntity.ok(lessonFacade.signLesson(id));
    }

    //O status vem opcional por causa da aba "Todas", que não filtra nada. As outras três mandam SCHEDULED, CONFIRMED ou CANCELED.
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'SCHOOL_DIRECTOR', 'PLATFORM_ADMIN')")
    public ResponseEntity<List<LessonResponseDTO>> getByStudent(
            @PathVariable UUID studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) LessonStatus status) {
        return ResponseEntity.ok(lessonFacade.getLessonsByStudent(studentId, from, to, status));
    }
}
