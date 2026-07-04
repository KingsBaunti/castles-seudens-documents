package com.castles.seudensdocuments.core.controller;


import com.castles.seudensdocuments.core.dto.StudentRequestDto;
import com.castles.seudensdocuments.core.dto.StudentResponseDto;
import com.castles.seudensdocuments.core.service.StudentService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;


    //Создание нового студента
    //POST /api/students
    @PostMapping
    public ResponseEntity<StudentResponseDto> createStudent(@RequestBody StudentRequestDto requestDto) {
        System.out.println("Received request: " + "\n"
                + requestDto.getEmail() + "\n"
                +requestDto.getEnrollmentDate());
        StudentResponseDto response = studentService.createStudent(requestDto);
        System.out.println("Received request: " + "\n"
                  +response.getEnrollmentDate());
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

}