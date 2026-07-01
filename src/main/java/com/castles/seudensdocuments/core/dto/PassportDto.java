package com.castles.seudensdocuments.core.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PassportDto {
    private Long id;
    private String passportNumber;
    private LocalDate issueDate;
}
