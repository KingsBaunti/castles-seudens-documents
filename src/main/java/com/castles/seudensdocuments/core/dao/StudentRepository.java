package com.castles.seudensdocuments.core.dao;

import com.castles.seudensdocuments.core.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    @Query("SELECT s FROM Student as s " +
            "LEFT JOIN FETCH s.passport " +
            "LEFT JOIN FETCH s.transcripts " +
            "WHERE s.id = :id")
    Optional<Student> findStudentById(Long id);



}
