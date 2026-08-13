package com.castles.seudensdocuments.core.controller;


import com.castles.seudensdocuments.core.dto.StudentRequestDto;
import com.castles.seudensdocuments.core.dto.StudentResponseDto;
import com.castles.seudensdocuments.core.service.StudentService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;


    //Создание нового студента
    //POST /api/students
    @PostMapping
    public ResponseEntity<StudentResponseDto> createStudent(@RequestBody StudentRequestDto requestDto) {

        StudentResponseDto response = studentService.createStudent(requestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //Получение студента по ID
    //GET /api/students/{id}
    @GetMapping("/{id}")
    public ResponseEntity<StudentResponseDto> getStudent(@PathVariable Long id) {
        StudentResponseDto response = studentService.getStudent(id);
        return ResponseEntity.ok(response);
    }


    //Полное обновление студента по ID
    //PUT /api/students/{id}
    @PutMapping("/{id}")
    public ResponseEntity<StudentResponseDto> updateStudent(
            @PathVariable Long id,
            @RequestBody StudentRequestDto requestDto) {
        StudentResponseDto response = studentService.updateStudent(id, requestDto);
        return ResponseEntity.ok(response);
    }


    //Удаление студента по ID
    //DELETE /api/students/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteStudent(@PathVariable Long id) {
        String message = studentService.deleteStudent(id);
        return ResponseEntity.ok(Map.of("status", message));
    }

    //Загрузка аватарки
    //POST /api/students/{id}/avatar
    @PostMapping("/{id}/avatar")
    public ResponseEntity<Map<String, String>> uploadStudentAvatar(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file)  {
        try {
            studentService.uploadAvatar(id, file);

            return ResponseEntity.ok(
                    Map.of("status",
                            "Аватарка успешно загружена для студента с id " + id)
            );
        } catch (EntityNotFoundException e){
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }

    }

    //Получение аватарки по id
    //GET /api/students/{id}/avatar
    @GetMapping("/{id}/avatar")
    public ResponseEntity<byte[]> downloadStudentAvatar(@PathVariable Long id){
        byte[] avatar = studentService.getAvatar(id);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"avatar_" + id + ".jpg\""
                )
                .body(avatar);
    }


}