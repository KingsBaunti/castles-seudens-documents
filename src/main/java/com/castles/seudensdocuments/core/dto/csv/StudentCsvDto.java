package com.castles.seudensdocuments.core.dto.csv;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

//DTO для чтения студентов из CSV файлов
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentCsvDto {

    @CsvBindByName(column = "name")
    private String name;

    @CsvBindByName(column = "email")
    private String email;

    @CsvBindByName(column = "enrollmentDate")
    @CsvDate("dd.MM.yyyy")
    private LocalDate enrollmentDate;

    @CsvBindByName(column = "passportNumber")
    private String passportNumber;

    @CsvBindByName(column = "passportIssueDate")
    @CsvDate("dd.MM.yyyy")
    private LocalDate passportIssueDate;
}
