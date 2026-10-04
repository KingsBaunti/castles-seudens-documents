package com.castles.seudensdocuments.core.controller;


import com.castles.seudensdocuments.core.dto.StudentPageResponse;
import com.castles.seudensdocuments.core.dto.StudentRequestDto;
import com.castles.seudensdocuments.core.dto.StudentResponseDto;
import com.castles.seudensdocuments.core.service.StudentService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
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

    //Добавление Х случайно сгенерированных студентов
    @PostMapping("/generate/{numberOfStudents}")
    public ResponseEntity<Map<String, Object>> generateNumberOfStudents(@PathVariable int numberOfStudents){
        return studentService.generateStudents(numberOfStudents);
    }

    //Генерация случайных студентов с вставкой через batchUpdate
    @PostMapping("generate-batch/{numberOfStudents}")
    public ResponseEntity<Map<String, Object>> generateButchNumberOfStudents(@PathVariable int numberOfStudents){
        return studentService.generateStudentsWithJdbcBatchUpdate(numberOfStudents);
    }

    //Поиск с пагинацией и фильтрацией
    //GET /api/students?name=Ivan&email=ivan@mail.ru&enrollmentDateFrom=2023-01-01&enrollmentDateTo=2024-01-01&page=0&size=2&sort=firstName,asc
    //Без фильтров — все студенты, страница 0, по 10
    //GET /api/students
    //GET /api/students?name=ivan
    @GetMapping
    public StudentPageResponse getStudent(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) LocalDate enrollmentDateFrom,
            @RequestParam(required = false) LocalDate enrollmentDateTo,
            @PageableDefault(size = 10, page = 0, sort = "id")Pageable pageable
            ){

        try {
            //Если сортировка по имени, то заменяем её на фамилию
            if(pageable.getSort().getOrderFor("name") != null){
                Sort.Order order = pageable.getSort().getOrderFor("name");
                pageable = PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        Sort.by(order.getDirection(), "firstName")
                );
            }

            return studentService.findAll(name, email, enrollmentDateFrom, enrollmentDateTo, pageable);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
    //Поиск с пагинацией и фильтрацией с использованием CriteriaAPI
    @GetMapping("/criteria")
    public StudentPageResponse getStudentWithCriteria(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) LocalDate enrollmentDateFrom,
            @RequestParam(required = false) LocalDate enrollmentDateTo,
            @PageableDefault(size = 10, page = 0, sort = "id")Pageable pageable
    ){

        try {
            //Если сортировка по имени, то заменяем её на фамилию
            if(pageable.getSort().getOrderFor("name") != null){
                Sort.Order order = pageable.getSort().getOrderFor("name");
                pageable = PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        Sort.by(order.getDirection(), "firstName")
                );
            }

            return studentService.findAllWithCriteria(name, email, enrollmentDateFrom, enrollmentDateTo, pageable);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}