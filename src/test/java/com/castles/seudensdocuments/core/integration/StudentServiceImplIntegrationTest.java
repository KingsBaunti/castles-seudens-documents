package com.castles.seudensdocuments.core.integration;


import com.castles.seudensdocuments.core.dao.StudentRepository;
import com.castles.seudensdocuments.core.dto.StudentPageResponse;
import com.castles.seudensdocuments.core.dto.StudentRequestDto;
import com.castles.seudensdocuments.core.dto.StudentResponseDto;
import com.castles.seudensdocuments.core.dto.csv.CsvRowError;
import com.castles.seudensdocuments.core.dto.csv.CsvUploadResponse;
import com.castles.seudensdocuments.core.mapper.StudentMapperImpl;
import com.castles.seudensdocuments.core.model.Student;
import com.castles.seudensdocuments.core.spec.StudentFilterSpec;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Map;



import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
public class StudentServiceImplIntegrationTest extends AbstractIntegrationTest{
    @Autowired
    private StudentMapperImpl studentMapper;
    @Autowired
    private StudentRepository studentRepository;

    @Test
    void getStudent_shouldReturnStudent() {
        StudentResponseDto found = studentService.getStudent(defaultStudentId).getBody();
        assertThat(found).isNotNull();
        //Проверка полей студента
        assertThat(found.getId()).isEqualTo(defaultStudentId);
        assertThat(found.getFirstName()).isEqualTo("Тестовый");
        assertThat(found.getLastName()).isEqualTo("Пользователь");
        assertThat(found.getEmail()).isEqualTo("test@example.com");
        assertThat(found.getAvatar()).isNull();
        assertThat(found.getEnrollmentDate()).isEqualTo(LocalDate.now());
        //Проверка полей паспорта студента
        assertThat(found.getPassport().getPassportNumber()).isEqualTo("1234567890");
        assertThat(found.getPassport().getIssueDate()).isEqualTo(LocalDate.of(2020, 1, 1));
        //Проверка оценок студента
        assertThat(found.getTranscripts().get(0).getSubject()).isEqualTo("Mathematics");
        assertThat(found.getTranscripts().get(0).getGrade()).isEqualTo(85);
    }

    //Проверяем ошибку поиска по неверному ID
    @Test
    void getStudentByWrongID(){
        assertThatThrownBy(() ->
                studentService.getStudent(Long.MAX_VALUE)
        )
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Student not found with id:" + Long.MAX_VALUE);
    }

    //Проверяем обновление данных студента
    @Test
    void updateStudent(){

        StudentResponseDto found = studentService.getStudent(defaultStudentId).getBody();

        found.setFirstName("НовоеИмя");
        found.setLastName("НоваяФамилия");
        found.setEmail("new@example.com");
        found.setEnrollmentDate(LocalDate.of(2000, 1, 1));

        found.getPassport().setPassportNumber("9999999999");
        found.getPassport().setIssueDate(LocalDate.of(2000, 1, 1));

        found.getTranscripts().get(0).setSubject("Physics");
        found.getTranscripts().get(0).setGrade(67);

        //Обновляем студента в БД
        StudentResponseDto result = studentService.updateStudent(defaultStudentId, studentMapper.toRequestDto(found)).getBody();

        //Проверка полей студента после обновления
        assertThat(result).isNotNull();
        //Проверка полей студента
        assertThat(result.getId()).isEqualTo(found.getId());
        assertThat(result.getFirstName()).isEqualTo("НовоеИмя");
        assertThat(result.getLastName()).isEqualTo("НоваяФамилия");
        assertThat(result.getEmail()).isEqualTo("new@example.com");
        assertThat(result.getEnrollmentDate()).isEqualTo(LocalDate.of(2000, 1, 1));
        //Проверка полей паспорта студента
        assertThat(result.getPassport().getPassportNumber()).isEqualTo("9999999999");
        assertThat(result.getPassport().getIssueDate()).isEqualTo(LocalDate.of(2000, 1, 1));
        //Проверка оценок студента
        assertThat(result.getTranscripts().get(0).getSubject()).isEqualTo("Physics");
        assertThat(result.getTranscripts().get(0).getGrade()).isEqualTo(67);
    }

    //Проверяем попытку обновить данные по неправильному ID
    @Test
    void updateStudentByWrongId(){

        StudentRequestDto request = createDefaultStudentRequest();

        assertThatThrownBy(() ->
                studentService.updateStudent(Long.MAX_VALUE, request)
        )
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Student not found with id: " + Long.MAX_VALUE);
    }

    //Добавляем аватар студенту
    @Test
    void putStudentAvatar(){
        try {
            //читаем аватар в массив байтов
            ClassPathResource resource = new ClassPathResource("test-avatar.jpg");
            byte[] avatar = resource.getInputStream().readAllBytes();
            //Подготавливаем массив байтов к загрузки в БД как jpeg
            MockMultipartFile file = new MockMultipartFile("file", "test-avatar.jpg", "image/jpeg", avatar);
            //Добавляем аватар студенту
            studentService.uploadAvatar(defaultStudentId, file);

            byte[] savedAvatar = studentService.getAvatar(defaultStudentId).getBody();

            assertThat(savedAvatar).isEqualTo(avatar);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    //Добавляем слишком большой аватар студенту
    @Test
    void putTooBigAvatar() throws IOException {
        ClassPathResource resource = new ClassPathResource("test-avatar-TooBig.jpg");
        byte[] avatar = resource.getInputStream().readAllBytes();

        MockMultipartFile file = new MockMultipartFile("file", "test-avatar-TooBig.jpg", "image/jpeg", avatar);

        Map<String, String> body = studentService.uploadAvatar(defaultStudentId, file).getBody();



        assertThat(body).containsEntry("error", "Размер аватара не должен превышать 5 МБ");

    }

    //Добавляем аватар студенту с неправильным ID
    @Test
    void putAvatarToWrongId() throws IOException {
        ClassPathResource resource = new ClassPathResource("test-avatar.jpg");
        byte[] avatar = resource.getInputStream().readAllBytes();

        MockMultipartFile file = new MockMultipartFile("file", "test-avatar.jpg", "image/jpeg", avatar);

        ResponseEntity<Map<String, String>> response = studentService.uploadAvatar(Long.MAX_VALUE, file);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        Map<String, String> body = response.getBody();



        assertThat(body).containsEntry("error", "Студент с id " + Long.MAX_VALUE + " не найден");

    }

    //Получаем аватар по запросу
    @Test
    void getAvatar() throws IOException{

        //Добавляем аватар студенту
        ClassPathResource resource = new ClassPathResource("test-avatar.jpg");
        byte[] sourceAvatar = resource.getInputStream().readAllBytes();
        MockMultipartFile file = new MockMultipartFile("file", "test-avatar.jpg", "image/jpeg", sourceAvatar);
        studentService.uploadAvatar(defaultStudentId, file);

        //Получаем загруженный аватар
        byte[] resultAvatar = studentService.getAvatar(defaultStudentId).getBody();
        assertThat(resultAvatar).isNotNull().isEqualTo(sourceAvatar);
    }

    //Получаем аватар по неправильному ID
    @Test
    void getAvatarByWrongId(){

        assertThatThrownBy(() ->
                studentService.getAvatar(Long.MAX_VALUE))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Студент с id " + Long.MAX_VALUE + " не найден");
    }

    //Получаем студента с аватаром
    @Test
    void getStudentWithAvatar() throws IOException{

        //Ожидаемый аватар
        String expectedAvatar = Base64.getEncoder().encodeToString(
                new ClassPathResource("test-avatar.jpg")
                        .getInputStream()
                        .readAllBytes()
        );

        //читаем аватар в массив байтов
        ClassPathResource resource = new ClassPathResource("test-avatar.jpg");
        byte[] avatar = resource.getInputStream().readAllBytes();
        //Подготавливаем массив байтов к загрузки в БД как jpeg
        MockMultipartFile file = new MockMultipartFile("file", "test-avatar.jpg", "image/jpeg", avatar);
        //Добавляем аватар студенту
        studentService.uploadAvatar(defaultStudentId, file);
        //Отправляем запрос на JSON студента
        StudentResponseDto found = studentService.getStudent(defaultStudentId).getBody();
        assertThat(found).isNotNull();

        //Проверка полей студента
        assertThat(found.getId()).isEqualTo(defaultStudentId);
        assertThat(found.getFirstName()).isEqualTo("Тестовый");
        assertThat(found.getLastName()).isEqualTo("Пользователь");
        assertThat(found.getEmail()).isEqualTo("test@example.com");
        assertThat(found.getAvatar()).isEqualTo(expectedAvatar);
        assertThat(found.getEnrollmentDate()).isEqualTo(LocalDate.now());
        //Проверка полей паспорта студента
        assertThat(found.getPassport().getPassportNumber()).isEqualTo("1234567890");
        assertThat(found.getPassport().getIssueDate()).isEqualTo(LocalDate.of(2020, 1, 1));
        //Проверка оценок студента
        assertThat(found.getTranscripts().get(0).getSubject()).isEqualTo("Mathematics");
        assertThat(found.getTranscripts().get(0).getGrade()).isEqualTo(85);
    }

    //Пытаемся нагенерировать отрицательное число студентов
    @Test
    void generateUnderZeroStudents(){
        ResponseEntity<Map<String, Object>> response = studentService.generateStudents(-1);
        assertThat(response.getBody()).containsEntry("error", "В запросе на генерацию отрицательное число");
    }
    //Пытаемся нагенерировать нуль студентов
    @Test
    void generateZeroStudents(){
        ResponseEntity<Map<String, Object>> response = studentService.generateStudents(0);
        assertThat(response.getBody()).containsEntry("error", "В запросе на генерацию нуль");
    }

    //Генерируем одного студента
    @Test
    void generateOneStudent(){
        //Удаляем дефолтного студента перед генерацией и проверкой сгенерированных студентов
        studentRepository.deleteAll();
        ResponseEntity<Map<String, Object>> response = studentService.generateStudents(1);
        assertThat(studentRepository.count()).isEqualTo(1L);
        assertThat(response.getBody()).containsEntry("status", "Generated 1 students");
        log.info("Генерация студентов заняла - {}мс", response.getBody().get("executionTimeMs"));
    }
    //Генерируем 10 студентов
    @Test
    void generateTeenStudent(){
        //Удаляем дефолтного студента перед генерацией и проверкой сгенерированных студентов
        studentRepository.deleteAll();
        ResponseEntity<Map<String, Object>> response = studentService.generateStudents(10);
        assertThat(studentRepository.count()).isEqualTo(10L);
        assertThat(response.getBody()).containsEntry("status", "Generated 10 students");
        log.info("Генерация студентов заняла - {}мс", response.getBody().get("executionTimeMs"));
    }
    //Генерируем 100 студентов
    @Test
    void generateOneHundredStudent(){
        //Удаляем дефолтного студента перед генерацией и проверкой сгенерированных студентов
        studentRepository.deleteAll();
        ResponseEntity<Map<String, Object>> response = studentService.generateStudents(100);
        assertThat(studentRepository.count()).isEqualTo(100L);
        assertThat(response.getBody()).containsEntry("status", "Generated 100 students");
        log.info("Генерация студентов заняла - {}мс", response.getBody().get("executionTimeMs"));
    }
    //Генерируем 1000 студентов
    @Test
    void generateOneThousandStudent(){
//Удаляем дефолтного студента перед генерацией и проверкой сгенерированных студентов
        studentRepository.deleteAll();
        ResponseEntity<Map<String, Object>> response = studentService.generateStudents(1000);
        assertThat(studentRepository.count()).isEqualTo(1000L);
        assertThat(response.getBody()).containsEntry("status", "Generated 1000 students");
        log.info("Генерация студентов заняла - {}мс", response.getBody().get("executionTimeMs"));
    }
    //Генерируем 10 000 студентов
    @Test
    void generateTeenThousandStudent(){
        //Удаляем дефолтного студента перед генерацией и проверкой сгенерированных студентов
        studentRepository.deleteAll();
        ResponseEntity<Map<String, Object>> response = studentService.generateStudents(10_000);
        assertThat(studentRepository.count()).isEqualTo(10_000L);
        assertThat(response.getBody()).containsEntry("status", "Generated 10000 students");
        log.info("Генерация студентов заняла - {}мс", response.getBody().get("executionTimeMs"));
    }
    //Генерируем 100 000 студентов
    @Test
    void generateOneHundredThousandStudent(){
        //Удаляем дефолтного студента перед генерацией и проверкой сгенерированных студентов
        studentRepository.deleteAll();
        ResponseEntity<Map<String, Object>> response = studentService.generateStudents(100_000);
        assertThat(studentRepository.count()).isEqualTo(100_000L);
        assertThat(response.getBody()).containsEntry("status", "Generated 100000 students");
        log.info("Генерация студентов заняла - {}мс", response.getBody().get("executionTimeMs"));
    }


    //Генерация студентов со вставкой с помощью JDBC Template batchUpdate
    @Test
    void generateOneStudentWithJdbcBatchUpdate(){
        //Удаляем дефолтного студента перед генерацией и проверкой сгенерированных студентов
        studentRepository.deleteAll();
        ResponseEntity<Map<String, Object>> response = studentService.generateStudentsWithJdbcBatchUpdate(1);
        assertThat(studentRepository.count()).isEqualTo(1L);
        assertThat(response.getBody()).containsEntry("status", "Generated 1 students");
        log.info("Генерация студентов заняла - {}мс", response.getBody().get("executionTimeMs"));
    }
    //Генерируем 10 студентов
    @Test
    void generateTeenStudentWithJdbcBatchUpdate(){
        //Удаляем дефолтного студента перед генерацией и проверкой сгенерированных студентов
        studentRepository.deleteAll();
        ResponseEntity<Map<String, Object>> response = studentService.generateStudentsWithJdbcBatchUpdate(10);
        assertThat(studentRepository.count()).isEqualTo(10L);
        assertThat(response.getBody()).containsEntry("status", "Generated 10 students");
        log.info("Генерация студентов заняла - {}мс", response.getBody().get("executionTimeMs"));
    }
    //Генерируем 100 студентов
    @Test
    void generateOneHundredStudentWithJdbcBatchUpdate(){
        //Удаляем дефолтного студента перед генерацией и проверкой сгенерированных студентов
        studentRepository.deleteAll();
        ResponseEntity<Map<String, Object>> response = studentService.generateStudentsWithJdbcBatchUpdate(100);
        assertThat(studentRepository.count()).isEqualTo(100L);
        assertThat(response.getBody()).containsEntry("status", "Generated 100 students");
        log.info("Генерация студентов заняла - {}мс", response.getBody().get("executionTimeMs"));
    }
    //Генерируем 1000 студентов
    @Test
    void generateOneThousandStudentWithJdbcBatchUpdate(){
//Удаляем дефолтного студента перед генерацией и проверкой сгенерированных студентов
        studentRepository.deleteAll();
        ResponseEntity<Map<String, Object>> response = studentService.generateStudentsWithJdbcBatchUpdate(1000);
        assertThat(studentRepository.count()).isEqualTo(1000L);
        assertThat(response.getBody()).containsEntry("status", "Generated 1000 students");
        log.info("Генерация студентов заняла - {}мс", response.getBody().get("executionTimeMs"));
    }
    //Генерируем 10 000 студентов
    @Test
    void generateTeenThousandStudentWithJdbcBatchUpdate(){
        //Удаляем дефолтного студента перед генерацией и проверкой сгенерированных студентов
        studentRepository.deleteAll();
        ResponseEntity<Map<String, Object>> response = studentService.generateStudentsWithJdbcBatchUpdate(10_000);
        assertThat(studentRepository.count()).isEqualTo(10_000L);
        assertThat(response.getBody()).containsEntry("status", "Generated 10000 students");
        log.info("Генерация студентов заняла - {}мс", response.getBody().get("executionTimeMs"));
    }
    //Генерируем 100 000 студентов
    @Test
    void generateOneHundredThousandStudentWithJdbcBatchUpdate(){
        //Удаляем дефолтного студента перед генерацией и проверкой сгенерированных студентов
        studentRepository.deleteAll();
        ResponseEntity<Map<String, Object>> response = studentService.generateStudentsWithJdbcBatchUpdate(100_000);
        assertThat(studentRepository.count()).isEqualTo(100_000L);
        assertThat(response.getBody()).containsEntry("status", "Generated 100000 students");
        log.info("Генерация студентов заняла - {}мс", response.getBody().get("executionTimeMs"));
    }

    //Поиск всех студентов без фильтров
    @Test
    @Tag("Pageable")
    void getStudentsPageWithoutFilter() {
        StudentPageResponse resp = studentService.findAll(null, null, null, null, PageRequest.of(0, 1));
        assertThat(resp.getContent()).hasSize(1);
        assertThat(resp.getTotalItems()).isEqualTo(1);
        assertThat(resp.getContent().get(0).getEmail()).isEqualTo("test@example.com");
    }
    //Поиск всех студентов по name
    @Test
    @Tag("Pageable")
    void getStudentsPageWithNameFilter(){
        StudentPageResponse resp = studentService.findAll("ТЕСТ", null, null, null, PageRequest.of(0, 1));
        assertThat(resp.getContent().get(0).getEmail()).isEqualTo("test@example.com");
    }
    //Поиск студента по email
    @Test
    @Tag("Pageable")
    void getStudentsPageWithEmailFilter(){
        StudentPageResponse resp = studentService.findAll(null, "test@example.com", null, null, PageRequest.of(0, 1));
        assertThat(resp.getContent().get(0).getEmail()).isEqualTo("test@example.com");
    }
    //Поиск по дате from
    @Test
    @Tag("Pageable")
    void getStudentsPageWithFromFilter(){
        StudentPageResponse resp = studentService.findAll(null, null, LocalDate.now(), null, PageRequest.of(0, 1));
        assertThat(resp.getContent().get(0).getEmail()).isEqualTo("test@example.com");
    }
    //Поиск по дате to
    @Test
    @Tag("Pageable")
    void getStudentsPageWithToFilter(){
        StudentPageResponse resp = studentService.findAll(null, null, null, LocalDate.now(), PageRequest.of(0, 1));
        assertThat(resp.getContent().get(0).getEmail()).isEqualTo("test@example.com");
    }
    //Поиск по всем фильтрам
    @Test
    @Tag("Pageable")
    void getStudentsPageWitAllFilters(){
        StudentPageResponse resp = studentService.findAll("Тес", "test@example.com", LocalDate.now(), LocalDate.now(), PageRequest.of(0, 1));
        assertThat(resp.getContent().get(0).getEmail()).isEqualTo("test@example.com");
    }
    //Блок тестов для пагинации с использованием CriteriaAPI
    //Поиск всех студентов без фильтров
    @Test
    @Tag("Pageable")
    void getStudentsWithCriteriaPageWithoutFilter() {
        StudentPageResponse resp = studentService.findAllWithCriteria(null, null, null, null, PageRequest.of(0, 1));
        assertThat(resp.getContent()).hasSize(1);
        assertThat(resp.getTotalItems()).isEqualTo(1);
        assertThat(resp.getContent().get(0).getEmail()).isEqualTo("test@example.com");
    }
    //Поиск всех студентов по name
    @Test
    @Tag("Pageable")
    void getStudentsWithCriteriaPageWithNameFilter(){
        StudentPageResponse resp = studentService.findAllWithCriteria("ТЕСТ", null, null, null, PageRequest.of(0, 1));
        assertThat(resp.getContent().get(0).getEmail()).isEqualTo("test@example.com");
    }
    //Поиск студента по email
    @Test
    @Tag("Pageable")
    void getStudentsWithCriteriaPageWithEmailFilter(){
        StudentPageResponse resp = studentService.findAllWithCriteria(null, "test@example.com", null, null, PageRequest.of(0, 1));
        assertThat(resp.getContent().get(0).getEmail()).isEqualTo("test@example.com");
    }
    //Поиск по дате from
    @Test
    @Tag("Pageable")
    void getStudentsWithCriteriaPageWithFromFilter(){
        StudentPageResponse resp = studentService.findAllWithCriteria(null, null, LocalDate.now(), null, PageRequest.of(0, 1));
        assertThat(resp.getContent().get(0).getEmail()).isEqualTo("test@example.com");
    }
    //Поиск по дате to
    @Test
    @Tag("Pageable")
    void getStudentsWithCriteriaPageWithToFilter(){
        StudentPageResponse resp = studentService.findAllWithCriteria(null, null, null, LocalDate.now(), PageRequest.of(0, 1));
        assertThat(resp.getContent().get(0).getEmail()).isEqualTo("test@example.com");
    }
    //Поиск по всем фильтрам
    @Test
    @Tag("Pageable")
    void getStudentsWithCriteriaPageWitAllFilters(){
        StudentPageResponse resp = studentService.findAllWithCriteria("Тес", "test@example.com", LocalDate.now(), LocalDate.now(), PageRequest.of(0, 1));
        assertThat(resp.getContent().get(0).getEmail()).isEqualTo("test@example.com");
    }
    //Блок тестов для пагинации с использованием specification-arg-resolver
    //Поиск всех студентов без фильтров
    @Test
    @Tag("Pageable")
    void getStudentsWithSpecPageWithoutFilter() {
        StudentPageResponse resp = studentService.findAllWithSpec(null, PageRequest.of(0, 1));
        assertThat(resp.getContent()).hasSize(1);
        assertThat(resp.getTotalItems()).isEqualTo(1);
        assertThat(resp.getContent().get(0).getEmail()).isEqualTo("test@example.com");
    }
    //Поиск всех студентов по name
    @Test
    @Tag("Pageable")
    void getStudentsWithSpecPageWithNameFilter(){

        Specification<Student> spec = (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("firstName")), "ТЕСТ"),
                cb.like(cb.lower(root.get("lastName")), "ТЕСТ")
        );

        StudentPageResponse resp = studentService.findAllWithSpec(spec, PageRequest.of(0, 1));
        assertThat(resp.getContent().get(0).getEmail()).isEqualTo("test@example.com");
    }
    //Поиск студента по email
    @Test
    @Tag("Pageable")
    void getStudentsWithSpecPageWithEmailFilter(){

        Specification<Student> spec = (root, query, cb) -> cb.equal(
                cb.lower(root.get("email")), "test@example.com");

        StudentPageResponse resp = studentService.findAllWithSpec(spec, PageRequest.of(0, 1));
        assertThat(resp.getContent().get(0).getEmail()).isEqualTo("test@example.com");
    }
    //Поиск по дате from
    @Test
    @Tag("Pageable")
    void getStudentsWithSpecPageWithFromFilter(){

        Specification<Student> spec = (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("enrollmentDate"), LocalDate.now());

        StudentPageResponse resp = studentService.findAllWithSpec(spec, PageRequest.of(0, 1));
        assertThat(resp.getContent().get(0).getEmail()).isEqualTo("test@example.com");
    }
    //Поиск по дате to
    @Test
    @Tag("Pageable")
    void getStudentsWithSpecPageWithToFilter(){

        Specification<Student> spec = (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("enrollmentDate"), LocalDate.now());

        StudentPageResponse resp = studentService.findAllWithSpec(spec, PageRequest.of(0, 1));
        assertThat(resp.getContent().get(0).getEmail()).isEqualTo("test@example.com");
    }
    //Поиск по всем фильтрам
    @Test
    @Tag("Pageable")
    void getStudentsWithCSpecPageWitAllFilters(){

        Specification<Student> spec = (root, query, cb) -> cb.and(
            cb.or(
                    cb.like(cb.lower(root.get("firstName")), "%тес%"),
                    cb.like(cb.lower(root.get("lastName")), "%тес%")
            ),
            cb.equal(cb.lower(root.get("email")), "test@example.com"),
            cb.greaterThanOrEqualTo(root.get("enrollmentDate"), LocalDate.now()),
            cb.lessThanOrEqualTo(root.get("enrollmentDate"), LocalDate.now())
        );

        StudentPageResponse resp = studentService.findAllWithSpec(spec, PageRequest.of(0, 1));
        assertThat(resp.getContent().get(0).getEmail()).isEqualTo("test@example.com");
    }

    //Проверка конвертации пагинации в Excel
    @Test
    @Tag("PageableExportToExcel")
    void exportPageToExcel(){

        Specification<Student> spec = (root, query, cb) -> cb.and(
                cb.or(
                        cb.like(cb.lower(root.get("firstName")), "%тес%"),
                        cb.like(cb.lower(root.get("lastName")), "%тес%")
                ),
                cb.equal(cb.lower(root.get("email")), "test@example.com"),
                cb.greaterThanOrEqualTo(root.get("enrollmentDate"), LocalDate.now()),
                cb.lessThanOrEqualTo(root.get("enrollmentDate"), LocalDate.now())
        );

        ResponseEntity<byte[]> response = studentService.exportPageToExcel(spec, PageRequest.of(0,1));

        byte[] body = response.getBody();
        assertThat(body).isNotNull().isNotEmpty();

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(body))) {
            var sheet = workbook.getSheetAt(0);
            assertThat(sheet.getLastRowNum()).isEqualTo(1);
            assertThat(sheet.getRow(1).getCell(2).getStringCellValue()).isEqualTo("test@example.com");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    //Проверка конвертации пагинации в PDF
    @Test
    @Tag("PageableExportToPdf")
    void exportPageToPdf(){
        Specification<Student> spec = (root, query, cb) -> cb.and(
                cb.or(
                        cb.like(cb.lower(root.get("firstName")), "%тес%"),
                        cb.like(cb.lower(root.get("lastName")), "%тес%")
                ),
                cb.equal(cb.lower(root.get("email")), "test@example.com"),
                cb.greaterThanOrEqualTo(root.get("enrollmentDate"), LocalDate.now()),
                cb.lessThanOrEqualTo(root.get("enrollmentDate"), LocalDate.now())
        );

        ResponseEntity<byte[]> response = studentService.exportPageToPdf(spec, PageRequest.of(0,1));

        byte[] body = response.getBody();
        assertThat(body).isNotNull().isNotEmpty();

        try(PdfReader reader = new PdfReader(body)){
            assertThat(reader.getNumberOfPages()).isGreaterThanOrEqualTo(1);
            StringBuilder text = new StringBuilder();

            PdfTextExtractor extractor = new PdfTextExtractor(reader);

            for(int i = 1; i <= reader.getNumberOfPages(); i++){
                text.append(extractor.getTextFromPage(i));
            }

            String content = text.toString().replaceAll("\\s+", "").trim();

            assertThat(content).contains("Отчётпостудентам");
            assertThat(content).contains("Тестовый");
            assertThat(content).contains("Пользователь");
            assertThat(content).contains("test@example.com");
            assertThat(content).contains("1234567890");
        } catch (Exception e){
            throw new RuntimeException(e);
        }




    }

    //Тесты импорта из CSV файлов
    //Успешный импорт
    @Test
    @Tag("ImportFromCsv")
    void succesImportFromCsv(){

        //Файл с данными
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "students.csv",
                "text/csv",
                """
                name;email;enrollmentDate;passportNumber;passportIssueDate
                Ivan Petrov;ivan.petrov@example.com;01.09.2023;4511789012;20.11.2019
                Anna Sidorova;anna.sidorova@example.com;01.09.2023;4511789013;15.02.2020
                """.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        //Очищаем БД от дефолтного студента
        studentRepository.deleteAll();

        CsvUploadResponse response = studentService.getStudentsFromCsv(file);

        assertThat(response.getErrors()).isEmpty();
        assertThat(response.getSuccessCount()).isEqualTo(2);
        assertThat(studentRepository.count()).isEqualTo(2L);

    }
    //Импорт невалидных значений
    @Test
    @Tag("ImportFromCsv")
    void invalidImportFromCsv(){

        /*
        2 - пустое имя
        3 - Одно слово в имени
        4 - невалидный емайл
        5 - дата зачисления из будущего
        6 - пустой номер паспорта
        7 - дата выдачи паспорта из будущего
        */
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "students-invalid.csv",
                "text/csv",
                """
                name;email;enrollmentDate;passportNumber;passportIssueDate
                ;ivan.petrov@example.com;01.09.2023;4511789012;20.11.2019
                OnlyFirst;anna@example.com;01.09.2023;4511789013;15.02.2020
                Petr Sidorov;not-an-email;01.09.2023;4511789014;10.03.2018
                Maria Ivanova;maria@example.com;01.01.2099;4511789015;12.04.2017
                Alex Smirnov;alex@example.com;01.09.2023;;05.05.2016
                Olga Kuznetsova;olga@example.com;01.09.2023;4511789016;01.01.2099
                """.getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );

        studentRepository.deleteAll();

        CsvUploadResponse response = studentService.getStudentsFromCsv(file);

        assertThat(response.getSuccessCount()).isEqualTo(0);
        assertThat(studentRepository.count()).isEqualTo(0L);
        assertThat(response.getErrors())
                .extracting(CsvRowError::getMessage)
                .contains(
                        "Name is null or empty",
                        "Need first name and last name",
                        "Invalid email format: 'not-an-email'",
                        "Enrollment date from future",
                        "Passport number is empty",
                        "Passport Issue Date from future"
                );
        assertThat(response.getErrors())
                .extracting(CsvRowError::getRowNumber)
                .containsExactlyInAnyOrder(2, 3, 4, 5, 6, 7);



    }


}
