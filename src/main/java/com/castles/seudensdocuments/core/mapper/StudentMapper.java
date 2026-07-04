package com.castles.seudensdocuments.core.mapper;


import com.castles.seudensdocuments.core.dto.StudentRequestDto;
import com.castles.seudensdocuments.core.dto.StudentResponseDto;
import com.castles.seudensdocuments.core.model.Student;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring", //Делаем маппер спринг бином
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE //Игнорируем Null при обновлении
)
public interface StudentMapper {

    //Преобразование из StudentRequestDto в Entity
    @Mapping(target = "id", ignore = true) //Игнорируем ID
    @Mapping(target = "transcripts", ignore = true) //Связь с оценками обрабатываем вручную
    @Mapping(target = "passport", ignore = true)
    Student toEntity(StudentRequestDto requestDto);

    //Преобразование из Entity в DTO
    @Mapping(source = "enrollmentDate", target = "enrollmentDate")//Выглядит странно но работает только так
    StudentResponseDto toResponseDto(Student entity);

    //Преобразование из StudentRequestDto в Entity
    @Mapping(target = "id", ignore = true) //Игнорируем ID
    @Mapping(target = "passport", ignore = true)//Связь с паспортом обрабатываем вручную
    @Mapping(target = "transcripts", ignore = true) //Связь с оценками обрабатываем вручную
    void updateEntity(@MappingTarget Student entity, StudentRequestDto requestDto);

    //



}
