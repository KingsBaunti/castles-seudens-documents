package com.castles.seudensdocuments.core.mapper.csv;

import com.castles.seudensdocuments.core.dto.csv.StudentCsvDto;
import com.castles.seudensdocuments.core.model.Passport;
import com.castles.seudensdocuments.core.model.Student;
import org.mapstruct.*;

import java.util.ArrayList;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface StudentCsvMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "firstName", ignore = true)
    @Mapping(target = "lastName", ignore = true)
    @Mapping(target = "passport", ignore = true)
    @Mapping(target = "transcripts", ignore = true)
    @Mapping(target = "avatar", ignore = true)
    Student toEntity(StudentCsvDto dto);

    @AfterMapping
    default void afterMapping(StudentCsvDto dto, @MappingTarget Student student){
        String[] parts = dto.getName().trim().split("\\s+", 2);
        student.setFirstName(parts[0]);
        student.setLastName(parts[1]);

        Passport passport = new Passport();
        passport.setStudent(student);
        passport.setPassportNumber(dto.getPassportNumber());
        passport.setIssueDate(dto.getPassportIssueDate());
        student.setPassport(passport);

        student.setTranscripts(new ArrayList<>());
    }
}
