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

        return studentService.createStudent(requestDto);
    }

    //Получение студента по ID
    //GET /api/students/{id}
    @GetMapping("/{id}")
    public ResponseEntity<StudentResponseDto> getStudent(@PathVariable Long id) {
        return  studentService.getStudent(id);
    }


    //Полное обновление студента по ID
    //PUT /api/students/{id}
    @PutMapping("/{id}")
    public ResponseEntity<StudentResponseDto> updateStudent(@PathVariable Long id, @RequestBody StudentRequestDto requestDto) {
        return studentService.updateStudent(id, requestDto);
    }


    //Удаление студента по ID
    //DELETE /api/students/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteStudent(@PathVariable Long id) {
        return studentService.deleteStudent(id);
    }

    //Загрузка аватарки
    //POST /api/students/{id}/avatar
    @PostMapping("/{id}/avatar")
    public ResponseEntity<Map<String, String>> uploadStudentAvatar(@PathVariable Long id, @RequestParam("file") MultipartFile file)  {
        return studentService.uploadAvatar(id, file);
    }

    //Получение аватарки по id
    //GET /api/students/{id}/avatar
    @GetMapping("/{id}/avatar")
    public ResponseEntity<byte[]> downloadStudentAvatar(@PathVariable Long id){
        return studentService.getAvatar(id);
    }


}