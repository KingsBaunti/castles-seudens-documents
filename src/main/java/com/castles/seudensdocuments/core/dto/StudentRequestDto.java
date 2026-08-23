//DTO Request для операций запросов когда ID может быть неизвестен

package com.castles.seudensdocuments.core.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class StudentRequestDto {
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate enrollmentDate;
    private PassportDto passport;
    private List<TranscriptDto> transcripts;
}
