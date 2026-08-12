package com.castles.seudensdocuments.core.service;

import com.castles.seudensdocuments.core.dao.PassportRepository;
import com.castles.seudensdocuments.core.dao.StudentRepository;
import com.castles.seudensdocuments.core.dao.TranscriptRepository;
import com.castles.seudensdocuments.core.dto.StudentRequestDto;
import com.castles.seudensdocuments.core.dto.StudentResponseDto;
import com.castles.seudensdocuments.core.mapper.PassportMapper;
import com.castles.seudensdocuments.core.mapper.StudentMapper;
import com.castles.seudensdocuments.core.mapper.TranscriptMapper;
import com.castles.seudensdocuments.core.model.Passport;
import com.castles.seudensdocuments.core.model.Student;
import com.castles.seudensdocuments.core.model.Transcript;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final PassportRepository passportRepository;
    private final TranscriptRepository transcriptRepository;
    private final StudentMapper studentMapper;
    private final PassportMapper passportMapper;
    private final TranscriptMapper transcriptMapper;
    private static final long MAX_AVATAR_SIZE = 5*1024*1024; //1024 байт = 1 КБ, 1024 КБ = 1 МБ

    @Transactional
    public StudentResponseDto createStudent(StudentRequestDto requestDto){

        //Создаём пасспорт через маппер и синхронизируем с БД методом passportRepository.save()
        Passport passport = passportMapper.toEntity(requestDto.getPassport());

        //Создаём student и привязываем паспорт
        Student student = studentMapper.toEntity(requestDto);
        student.setPassport(passport);
        passport.setStudent(student);

        //Создаём транскрипты и связываем их со студентом
        List<Transcript> transcripts = requestDto.getTranscripts().stream().map(dto ->
        {
            Transcript t = transcriptMapper.toEntity(dto);
            t.setStudent(student);
            return t;
        })
                .collect(Collectors.toList());
        student.setTranscripts(transcripts);

        Student savedStudent = studentRepository.save(student);
        return studentMapper.toResponseDto(savedStudent);
    }


    public StudentResponseDto getStudent(Long id){
        Student student = studentRepository.findStudentById(id)
                .orElseThrow(() -> new EntityNotFoundException("Student not found with id:" + id));
        return studentMapper.toResponseDto(student);
    }

    @Transactional
    public StudentResponseDto updateStudent(Long id, StudentRequestDto requestDto){

        Student existingStudent = studentRepository.findStudentById(id)
                .orElseThrow(() -> new EntityNotFoundException("Student not found with id: " + id));

        //Обновляем passport
        Passport passport = existingStudent.getPassport();
        if (passport == null) {
            passport = new Passport();
        } else{
            passportMapper.updateEntity(passport, requestDto.getPassport());
        }
        existingStudent.setPassport(passport);

        //Обновляем основные поля студента из DTO с помощью MapStruct
        studentMapper.updateEntity(existingStudent, requestDto);

        //Обновляем transcripts
        existingStudent.getTranscripts().clear();
        List<Transcript> newTranscripts = requestDto.getTranscripts().stream()
                .map(dto -> {
                    Transcript t = new Transcript();
                    t.setSubject(dto.getSubject());
                    t.setGrade(dto.getGrade());
                    t.setStudent(existingStudent);
                    return t;
                })
                .collect(Collectors.toList());
        existingStudent.getTranscripts().addAll(newTranscripts);

        Student updated = studentRepository.save(existingStudent);
        return studentMapper.toResponseDto(updated);
    }


    @Transactional
    public String deleteStudent(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new EntityNotFoundException("Student not found with id: " + id);
        }
        studentRepository.deleteById(id);
        return "Пользователь с id " + id + " успешно удалён";
    }


    @Transactional
    public void uploadAvatar(Long id, MultipartFile file) throws IOException {

        if (file.isEmpty()){
            throw new IllegalArgumentException("Файл аватара пустой");
        }

        if (file.getSize() > MAX_AVATAR_SIZE){
            throw new IllegalArgumentException("Размер аватара не должен превышать 5 МБ");
        }

        Student student = studentRepository.findStudentById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Студент с id " + id + "не найден"));

        student.setAvatar(file.getBytes());

        studentRepository.save(student);
    }

    @Transactional
    public byte[] getAvatar(Long id){
        Student student = studentRepository.findStudentById(id)
                .orElseThrow(() -> new EntityNotFoundException("Студент с id " + id + " не найден"));
        if (student.getAvatar() == null){
           throw new EntityNotFoundException("Аватар отсутствует у студента с id " + id);
        }
        return student.getAvatar();
    }
}
