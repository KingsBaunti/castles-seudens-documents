package com.castles.seudensdocuments.core.mapper;


import com.castles.seudensdocuments.core.dto.StudentRequestDto;
import com.castles.seudensdocuments.core.dto.StudentResponseDto;
import com.castles.seudensdocuments.core.model.Student;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import java.util.Base64;

@Mapper(
        componentModel = "spring", //Делаем маппер спринг бином
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE //Игнорируем Null при обновлении
)
public interface StudentMapper {

    //Преобразование из StudentRequestDto в Entity
    @Mapping(target = "id", ignore = true) //Игнорируем ID
    @Mapping(target = "transcripts", ignore = true) //Связь с оценками обрабатываем вручную
    @Mapping(target = "passport", ignore = true)
    @Mapping(target = "avatar", ignore = true) //Игнорируем avatar, тк создаётся отдельно
    Student toEntity(StudentRequestDto requestDto);

    //Преобразование из Entity в DTO
    @Mapping(source = "enrollmentDate", target = "enrollmentDate")//Выглядит странно но работает только так
    @Mapping(target = "avatar",
            expression = "java(entity.getAvatar() != null ? java.util.Base64.getEncoder().encodeToString(entity.getAvatar()) : null)") //Преобразование из Byte[] в Base64
    StudentResponseDto toResponseDto(Student entity);


    //Преобразование из StudentRequestDto в Entity
    @Mapping(target = "id", ignore = true) //Игнорируем ID
    @Mapping(target = "passport", ignore = true)//Связь с паспортом обрабатываем вручную
    @Mapping(target = "transcripts", ignore = true) //Связь с оценками обрабатываем вручную
    @Mapping(target = "avatar", ignore = true)
    void updateEntity(@MappingTarget Student entity, StudentRequestDto requestDto);

    //Преобразование из RequestDTO в ResponseDTO
    StudentResponseDto toResponseDto(StudentRequestDto requestDto);
    //Преобразование из RequestDTO в ResponseDTO
    StudentRequestDto toRequestDto(StudentResponseDto responseDto);





}
