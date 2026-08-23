package com.castles.seudensdocuments.core.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
@Data
public class StudentResponseDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String avatar; //avatar в формате Base64
    private LocalDate enrollmentDate;
    private PassportDto passport;
    private List<TranscriptDto> transcripts;
}
