package com.castles.seudensdocuments.core.integration;

import com.castles.seudensdocuments.core.dto.PassportDto;
import com.castles.seudensdocuments.core.dto.StudentRequestDto;
import com.castles.seudensdocuments.core.dto.StudentResponseDto;
import com.castles.seudensdocuments.core.dto.TranscriptDto;
import com.castles.seudensdocuments.core.mapper.StudentMapper;
import com.castles.seudensdocuments.core.mapper.StudentMapperImpl;
import com.castles.seudensdocuments.core.service.StudentService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class StudentServiceImplIntegrationTest extends AbstractIntegrationTest{
    @Autowired
    private StudentMapperImpl studentMapper;

    @Test
    void getStudent_shouldReturnStudent() {
        StudentResponseDto found = studentService.getStudent(defaultStudentId);
        assertThat(found).isNotNull();
        //Проверка полей студента
        assertThat(found.getId()).isEqualTo(defaultStudentId);
        assertThat(found.getFirstName()).isEqualTo("Тестовый");
        assertThat(found.getLastName()).isEqualTo("Пользователь");
        assertThat(found.getEmail()).isEqualTo("test@example.com");
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

        StudentResponseDto found = studentService.getStudent(defaultStudentId);

        found.setFirstName("НовоеИмя");
        found.setLastName("НоваяФамилия");
        found.setEmail("new@example.com");
        found.setEnrollmentDate(LocalDate.of(2000, 1, 1));

        found.getPassport().setPassportNumber("9999999999");
        found.getPassport().setIssueDate(LocalDate.of(2000, 1, 1));

        found.getTranscripts().get(0).setSubject("Physics");
        found.getTranscripts().get(0).setGrade(67);

        //Обновляем студента в БД
        StudentResponseDto result = studentService.updateStudent(defaultStudentId, studentMapper.toRequestDto(found));

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
    void updateStudentWithWrongId(){

        StudentRequestDto request = createDefaultStudentRequest();

        assertThatThrownBy(() ->
                studentService.updateStudent(Long.MAX_VALUE, request)
        )
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Student not found with id: " + Long.MAX_VALUE);
    }


    @Test
    void putStudentAvatar(){
        try {
            ClassPathResource resource = new ClassPathResource("test-avatar.jpg");
            byte[] avatar = resource.getInputStream().readAllBytes();

            MockMultipartFile file = new MockMultipartFile("file", "test-avatar.jpg", "image/jpeg", avatar);

            studentService.uploadAvatar(defaultStudentId, file);

            byte[] savedAvatar = studentService.getAvatar(defaultStudentId);

            assertThat(savedAvatar).isEqualTo(avatar);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    void putTooBigAvatar() throws IOException {
        ClassPathResource resource = new ClassPathResource("test-avatar-TooBig.jpg");
        byte[] avatar = resource.getInputStream().readAllBytes();

        MockMultipartFile file = new MockMultipartFile("file", "test-avatar-TooBig.jpg", "image/jpeg", avatar);

        assertThatThrownBy(() ->
                studentService.uploadAvatar(defaultStudentId, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Размер аватара не должен превышать 5 МБ");

    }
}
