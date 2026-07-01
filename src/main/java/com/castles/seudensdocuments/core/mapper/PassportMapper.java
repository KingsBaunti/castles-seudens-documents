package com.castles.seudensdocuments.core.mapper;


import com.castles.seudensdocuments.core.dto.PassportDto;
import com.castles.seudensdocuments.core.model.Passport;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PassportMapper {

    //Из DTO в Entity
    @Mapping(target = "id", ignore = true)
    Passport toEntity(PassportDto dto);

    //Из Entity в DTO
    PassportDto toDto(Passport entity);

    @Mapping(target = "id", ignore = true)
    void updateEntity(@MappingTarget Passport entity, PassportDto dto);


}
