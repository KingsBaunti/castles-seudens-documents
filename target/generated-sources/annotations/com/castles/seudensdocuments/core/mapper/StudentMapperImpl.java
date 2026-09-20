package com.castles.seudensdocuments.core.mapper;

import com.castles.seudensdocuments.core.dto.PassportDto;
import com.castles.seudensdocuments.core.dto.StudentRequestDto;
import com.castles.seudensdocuments.core.dto.StudentResponseDto;
import com.castles.seudensdocuments.core.dto.TranscriptDto;
import com.castles.seudensdocuments.core.model.Passport;
import com.castles.seudensdocuments.core.model.Student;
import com.castles.seudensdocuments.core.model.Transcript;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-07T23:50:15+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.12 (Microsoft)"
)
@Component
public class StudentMapperImpl implements StudentMapper {

    @Override
    public Student toEntity(StudentRequestDto requestDto) {
        if ( requestDto == null ) {
            return null;
        }

        Student student = new Student();

        student.setFirstName( requestDto.getFirstName() );
        student.setLastName( requestDto.getLastName() );
        student.setEmail( requestDto.getEmail() );
        student.setEnrollmentDate( requestDto.getEnrollmentDate() );

        return student;
    }

    @Override
    public StudentResponseDto toResponseDto(Student entity) {
        if ( entity == null ) {
            return null;
        }

        StudentResponseDto studentResponseDto = new StudentResponseDto();

        studentResponseDto.setEnrollmentDate( entity.getEnrollmentDate() );
        studentResponseDto.setId( entity.getId() );
        studentResponseDto.setFirstName( entity.getFirstName() );
        studentResponseDto.setLastName( entity.getLastName() );
        studentResponseDto.setEmail( entity.getEmail() );
        studentResponseDto.setPassport( passportToPassportDto( entity.getPassport() ) );
        studentResponseDto.setTranscripts( transcriptListToTranscriptDtoList( entity.getTranscripts() ) );

        studentResponseDto.setAvatar( entity.getAvatar() != null ? java.util.Base64.getEncoder().encodeToString(entity.getAvatar()) : null );

        return studentResponseDto;
    }

    @Override
    public void updateEntity(Student entity, StudentRequestDto requestDto) {
        if ( requestDto == null ) {
            return;
        }

        if ( requestDto.getFirstName() != null ) {
            entity.setFirstName( requestDto.getFirstName() );
        }
        if ( requestDto.getLastName() != null ) {
            entity.setLastName( requestDto.getLastName() );
        }
        if ( requestDto.getEmail() != null ) {
            entity.setEmail( requestDto.getEmail() );
        }
        if ( requestDto.getEnrollmentDate() != null ) {
            entity.setEnrollmentDate( requestDto.getEnrollmentDate() );
        }
    }

    @Override
    public StudentResponseDto toResponseDto(StudentRequestDto requestDto) {
        if ( requestDto == null ) {
            return null;
        }

        StudentResponseDto studentResponseDto = new StudentResponseDto();

        studentResponseDto.setFirstName( requestDto.getFirstName() );
        studentResponseDto.setLastName( requestDto.getLastName() );
        studentResponseDto.setEmail( requestDto.getEmail() );
        studentResponseDto.setEnrollmentDate( requestDto.getEnrollmentDate() );
        studentResponseDto.setPassport( requestDto.getPassport() );
        List<TranscriptDto> list = requestDto.getTranscripts();
        if ( list != null ) {
            studentResponseDto.setTranscripts( new ArrayList<TranscriptDto>( list ) );
        }

        return studentResponseDto;
    }

    @Override
    public StudentRequestDto toRequestDto(StudentResponseDto responseDto) {
        if ( responseDto == null ) {
            return null;
        }

        StudentRequestDto studentRequestDto = new StudentRequestDto();

        studentRequestDto.setFirstName( responseDto.getFirstName() );
        studentRequestDto.setLastName( responseDto.getLastName() );
        studentRequestDto.setEmail( responseDto.getEmail() );
        studentRequestDto.setEnrollmentDate( responseDto.getEnrollmentDate() );
        studentRequestDto.setPassport( responseDto.getPassport() );
        List<TranscriptDto> list = responseDto.getTranscripts();
        if ( list != null ) {
            studentRequestDto.setTranscripts( new ArrayList<TranscriptDto>( list ) );
        }

        return studentRequestDto;
    }

    protected PassportDto passportToPassportDto(Passport passport) {
        if ( passport == null ) {
            return null;
        }

        PassportDto passportDto = new PassportDto();

        passportDto.setId( passport.getId() );
        passportDto.setPassportNumber( passport.getPassportNumber() );
        passportDto.setIssueDate( passport.getIssueDate() );

        return passportDto;
    }

    protected TranscriptDto transcriptToTranscriptDto(Transcript transcript) {
        if ( transcript == null ) {
            return null;
        }

        TranscriptDto transcriptDto = new TranscriptDto();

        transcriptDto.setId( transcript.getId() );
        transcriptDto.setSubject( transcript.getSubject() );
        transcriptDto.setGrade( transcript.getGrade() );

        return transcriptDto;
    }

    protected List<TranscriptDto> transcriptListToTranscriptDtoList(List<Transcript> list) {
        if ( list == null ) {
            return null;
        }

        List<TranscriptDto> list1 = new ArrayList<TranscriptDto>( list.size() );
        for ( Transcript transcript : list ) {
            list1.add( transcriptToTranscriptDto( transcript ) );
        }

        return list1;
    }
}
