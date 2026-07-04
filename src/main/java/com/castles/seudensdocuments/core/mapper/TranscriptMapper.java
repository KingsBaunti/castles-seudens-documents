package com.castles.seudensdocuments.core.mapper;


import com.castles.seudensdocuments.core.dto.TranscriptDto;
import com.castles.seudensdocuments.core.model.Transcript;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface TranscriptMapper {

    //Преобразование из DTO в сущность
    @Mapping(target = "id", ignore = true)
    Transcript toEntity(TranscriptDto dto);

    //Из сущности в ДТО
    TranscriptDto toDto(Transcript entity);

    //Обновление сущности из ДТО без участия студента
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "student", ignore = true)
    void updateEntity(@MappingTarget Transcript entity, TranscriptDto dto);

}
