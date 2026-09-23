package com.castles.seudensdocuments.core.mapper;

import com.castles.seudensdocuments.core.dto.PassportDto;
import com.castles.seudensdocuments.core.model.Passport;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-20T20:38:04+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.12 (Microsoft)"
)
@Component
public class PassportMapperImpl implements PassportMapper {

    @Override
    public Passport toEntity(PassportDto dto) {
        if ( dto == null ) {
            return null;
        }

        Passport passport = new Passport();

        passport.setPassportNumber( dto.getPassportNumber() );
        passport.setIssueDate( dto.getIssueDate() );

        return passport;
    }

    @Override
    public PassportDto toDto(Passport entity) {
        if ( entity == null ) {
            return null;
        }

        PassportDto passportDto = new PassportDto();

        passportDto.setId( entity.getId() );
        passportDto.setPassportNumber( entity.getPassportNumber() );
        passportDto.setIssueDate( entity.getIssueDate() );

        return passportDto;
    }

    @Override
    public void updateEntity(Passport entity, PassportDto dto) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getPassportNumber() != null ) {
            entity.setPassportNumber( dto.getPassportNumber() );
        }
        if ( dto.getIssueDate() != null ) {
            entity.setIssueDate( dto.getIssueDate() );
        }
    }
}
