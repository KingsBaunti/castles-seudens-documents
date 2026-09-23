package com.castles.seudensdocuments.core.service;

import com.castles.seudensdocuments.core.dao.*;
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
//import lombok.Value;
import net.datafaker.Faker;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
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
    @Value("${spring.jpa.properties.hibernate.jdbc.batch-size:100}")
    private int batchSize;

    private final StudentJdbcDao studentJdbcDao;
    private final PassportJdbcDao passportJdbcDao;
    private final TranscriptJdbcDao transcriptJdbcDao;

    @Transactional
    public ResponseEntity<StudentResponseDto> createStudent(StudentRequestDto requestDto){

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
        return ResponseEntity.status(HttpStatus.CREATED).body(studentMapper.toResponseDto(savedStudent));
    }


    public ResponseEntity<StudentResponseDto> getStudent(Long id){
        Student student = studentRepository.findStudentById(id)
                .orElseThrow(() -> new EntityNotFoundException("Student not found with id:" + id));
        return ResponseEntity.ok(studentMapper.toResponseDto(student));
    }

    @Transactional
    public ResponseEntity<StudentResponseDto> updateStudent(Long id, StudentRequestDto requestDto){

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
        return ResponseEntity.ok(studentMapper.toResponseDto(updated));
    }


    @Transactional
    public ResponseEntity<Map<String, String>> deleteStudent(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new EntityNotFoundException("Student not found with id: " + id);
        }
        studentRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("status", "Пользователь с id " + id + " успешно удалён"));
    }


    @Transactional
    public ResponseEntity<Map<String, String>> uploadAvatar(Long id, MultipartFile file){

        try {
            if (file.isEmpty()){
                return ResponseEntity.badRequest().body(Map.of("error", "Файл аватара пустой"));

            }

            if (file.getSize() > MAX_AVATAR_SIZE){
                return ResponseEntity.badRequest().body(Map.of("error", "Размер аватара не должен превышать 5 МБ"));
            }

            Student student = studentRepository.findStudentById(id)
                    .orElseThrow(() ->
                            new EntityNotFoundException("Студент с id " + id + " не найден"));

            student.setAvatar(file.getBytes());

            studentRepository.save(student);
            return ResponseEntity.ok(Map.of("status", "Аватарка успешно загружена для студента с id " + id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @Transactional
    public ResponseEntity<byte[]> getAvatar(Long id){
        Student student = studentRepository.findStudentById(id)
                .orElseThrow(() -> new EntityNotFoundException("Студент с id " + id + " не найден"));
        if (student.getAvatar() == null){
           throw new EntityNotFoundException("Аватар отсутствует у студента с id " + id);
        }
        return ResponseEntity.ok().
                contentType(MediaType.IMAGE_JPEG).
                header(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "arrachment; filename=\"avatar_" + id + ".jpg\""
                )
                .body(student.getAvatar());

    }



    @Transactional
    public ResponseEntity<Map<String, Object>> generateStudents(int numberOfStudents){
        try {
            int numberOfCreatedStudents = 0;
            if(numberOfStudents < 0){
                return ResponseEntity.badRequest().body(Map.of("error", "В запросе на генерацию отрицательное число"));
            }
            if(numberOfStudents == 0){
                return ResponseEntity.badRequest().body(Map.of("error", "В запросе на генерацию нуль"));
            }
            Faker faker = new Faker(new Locale("ru"));
            Random random = new Random();
            List<Student> studentsBatch = new ArrayList<>(batchSize);
            long time = System.currentTimeMillis();

            for(int i = 0; i < numberOfStudents; i++){

                studentsBatch.add(createRandomStudent(faker, random));

                if(studentsBatch.size() == batchSize){
                    studentRepository.saveAll(studentsBatch);
                    studentRepository.flush();
                    numberOfCreatedStudents += studentsBatch.size();
                    studentsBatch.clear();
                }
            }
            if(!studentsBatch.isEmpty()){
                studentRepository.saveAll(studentsBatch);
                studentRepository.flush();
                numberOfCreatedStudents += studentsBatch.size();
            }


            time = System.currentTimeMillis() - time;
            return ResponseEntity.ok(Map.of("status", "Generated " + numberOfCreatedStudents + " students",
                    "executionTimeMs", time));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<Student> createRandomStudentList(int numberOfStudents){
        List<Student> studentList = new ArrayList<>();
        Faker faker = new Faker(new Locale("ru"));
        Random random = new Random();
        for(int i = 0; i < numberOfStudents; i++){
            studentList.add(createRandomStudent(faker, random));
        }
        return studentList;
    }
    private Student createRandomStudent(Faker faker, Random random){

        Student student = new Student();

        student.setFirstName(faker.name().firstName());
        student.setLastName(faker.name().lastName());
        student.setEmail(UUID.randomUUID() + faker.internet().emailAddress());
        student.setEnrollmentDate(faker.timeAndDate().birthday());

        Passport passport = createRandomPassport(faker);
        student.setPassport(passport);
        passport.setStudent(student);

        //От трёх до восьми оценок
        int transcriptsCount = faker.random().nextInt(3, 8);
        List<Transcript> transcripts = new ArrayList<>();
        for(int i = 0; i < transcriptsCount; i++){
            Transcript transcript = createRandomTranscript(faker);
            transcripts.add(transcript);
            transcript.setStudent(student);
        }
        student.setTranscripts(transcripts);

        //Случайное добавление аватара
        if(random.nextBoolean()){

            try {
                ClassPathResource resource = new ClassPathResource("schakal.jpg");
                byte[] avatar = resource.getInputStream().readAllBytes();
                student.setAvatar(avatar);
            } catch (IOException e) {
                throw new RuntimeException("Ошибка при присвоении аватара случайному студенту", e);
            }

        }


        return student;
    }
    private Passport createRandomPassport(Faker faker){

        Passport passport = new Passport();

        passport.setPassportNumber(faker.bothify("#### ######"));
        passport.setIssueDate(faker.timeAndDate().birthday());

        return passport;
    }
    private Transcript createRandomTranscript(Faker faker){

        Transcript transcript = new Transcript();

        transcript.setSubject(faker.educator().course());
        transcript.setGrade(faker.random().nextInt(1, 5));

        return transcript;
    }

    @Transactional
    public ResponseEntity<Map<String, Object>> generateStudentsWithJdbcBatchUpdate(int numberOfStudents){
        try {
            if(numberOfStudents <= 0){
                return ResponseEntity.badRequest().body(Map.of("error", "В запросе число студентов <= 0"));
            }

            Long time = System.currentTimeMillis();

            List<Student> students = createRandomStudentList(numberOfStudents);
            List<Long> studentIds = studentJdbcDao.getStudentIdList(numberOfStudents);
            List<Long> passportIds = passportJdbcDao.getPassportIdList(numberOfStudents);
            List<Passport> passports = new ArrayList<>(numberOfStudents);
            List<Long> transcriptIds = new ArrayList<>();
            List<Transcript> transcripts = new ArrayList<>();


            for(int i = 0; i < students.size(); i++){

                students.get(i).setId(studentIds.get(i));

                passports.add(students.get(i).getPassport());
                passports.get(i).setId(passportIds.get(i));

                transcripts.addAll(students.get(i).getTranscripts());
            }

            transcriptIds.addAll(transcriptJdbcDao.getTranscriptIdList(transcripts.size()));
            for(int i = 0; i < transcriptIds.size(); i++){
                transcripts.get(i).setId(transcriptIds.get(i));
            }

            studentJdbcDao.studentBatchCreate(students);
            passportJdbcDao.passportBatchCreate(passports);
            transcriptJdbcDao.createTranscripts(transcripts);

            time = System.currentTimeMillis() - time;
            return ResponseEntity.ok(Map.of("status", "Generated " + students.size() + " students",
                    "executionTimeMs", time));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

}
