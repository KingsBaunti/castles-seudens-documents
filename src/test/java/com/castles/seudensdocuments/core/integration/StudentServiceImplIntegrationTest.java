package com.castles.seudensdocuments.core.integration;

import com.castles.seudensdocuments.core.dto.StudentResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class StudentServiceImplIntegrationTest extends AbstractIntegrationTest{
    private Long defaultStudentId;

    @BeforeEach
    void setUp() {
        super.putInTestData(); // если нужно явно вызвать (обычно не требуется)

        // Создаём одного студента, которого будем использовать в большинстве тестов
        StudentResponseDto saved = studentService.createStudent(createDefaultStudentRequest());
        this.defaultStudentId = saved.getId();
    }

    @Test
    void getStudent_shouldReturnStudent() {
        StudentResponseDto found = studentService.getStudent(defaultStudentId);
        assertThat(found).isNotNull();
    }
}
