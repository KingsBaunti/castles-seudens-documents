package com.castles.seudensdocuments.core.mapper;

import com.castles.seudensdocuments.core.dto.TranscriptDto;
import com.castles.seudensdocuments.core.model.Transcript;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-20T20:38:04+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.12 (Microsoft)"
)
@Component
public class TranscriptMapperImpl implements TranscriptMapper {

    @Override
    public Transcript toEntity(TranscriptDto dto) {
        if ( dto == null ) {
            return null;
        }

        Transcript transcript = new Transcript();

        transcript.setSubject( dto.getSubject() );
        transcript.setGrade( dto.getGrade() );

        return transcript;
    }

    @Override
    public TranscriptDto toDto(Transcript entity) {
        if ( entity == null ) {
            return null;
        }

        TranscriptDto transcriptDto = new TranscriptDto();

        transcriptDto.setId( entity.getId() );
        transcriptDto.setSubject( entity.getSubject() );
        transcriptDto.setGrade( entity.getGrade() );

        return transcriptDto;
    }

    @Override
    public void updateEntity(Transcript entity, TranscriptDto dto) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getSubject() != null ) {
            entity.setSubject( dto.getSubject() );
        }
        entity.setGrade( dto.getGrade() );
    }
}
