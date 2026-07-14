package com.castles.seudensdocuments.core.integration;


import annotation.EnableTestContainers;
import com.castles.seudensdocuments.core.dto.PassportDto;
import com.castles.seudensdocuments.core.dto.StudentRequestDto;
import com.castles.seudensdocuments.core.dto.StudentResponseDto;
import com.castles.seudensdocuments.core.dto.TranscriptDto;
import com.castles.seudensdocuments.core.service.StudentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.time.LocalDate;
import java.util.List;


//Базовый класс для интиграционных тестов
@EnableTestContainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class AbstractIntegrationTest {
    //Тестирумый класс
    @Autowired
    protected StudentService studentService;

    @Container
    protected static final PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>("postgres:16.0")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test")
                    .withReuse(true)//Поднимается тестовая БД один раз для всех тестов, а не для каждого по отдельности
                    .withExposedPorts(5434);
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:postgresql://localhost:5434/testdb");
        registry.add("spring.datasource.username", () -> "test");
        registry.add("spring.datasource.password", () -> "test");
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    //Для выполнения SQL
    @Autowired
    protected JdbcTemplate jdbcTemplate;


    @BeforeEach
    public void putInTestData(){
        //Очистка таблиц
        jdbcTemplate.execute("""
                TRUNCATE TABLE student, passport, transcript 
                RESTART IDENTITY CASCADE
                """);
        StudentRequestDto defaultStudent = createDefaultStudentRequest();
        StudentResponseDto saved = studentService.createStudent(defaultStudent);
    }


    @AfterEach
    public void cleanDatabase(){}


    //Создание тестового студента
    protected StudentRequestDto createDefaultStudentRequest() {
        StudentRequestDto request = new StudentRequestDto();
        request.setFirstName("Тестовый");
        request.setLastName("Пользователь");
        request.setEmail("test@example.com");
        request.setEnrollmentDate(LocalDate.now());

        PassportDto passport = new PassportDto();
        passport.setPassportNumber("1234567890");
        passport.setIssueDate(LocalDate.of(2020, 1, 1));
        request.setPassport(passport);

        TranscriptDto transcript = new TranscriptDto();
        transcript.setSubject("Mathematics");
        transcript.setGrade(85);
        request.setTranscripts(List.of(transcript));

        return request;
    }

}
