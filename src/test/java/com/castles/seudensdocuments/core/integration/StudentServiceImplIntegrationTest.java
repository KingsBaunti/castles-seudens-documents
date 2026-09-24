package com.castles.seudensdocuments.core.integration;


import com.castles.seudensdocuments.core.dao.StudentRepository;
import com.castles.seudensdocuments.core.dto.StudentRequestDto;
import com.castles.seudensdocuments.core.dto.StudentResponseDto;
import com.castles.seudensdocuments.core.mapper.StudentMapperImpl;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
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



}
