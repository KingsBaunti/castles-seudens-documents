package com.castles.seudensdocuments.core.dao;

import com.castles.seudensdocuments.core.model.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {

    @Query("SELECT s FROM Student as s " +
            "LEFT JOIN FETCH s.passport " +
            "LEFT JOIN FETCH s.transcripts " +
            "WHERE s.id = :id")
    Optional<Student> findStudentById(Long id);

    @Query("SELECT s FROM Student s " +
            "WHERE (:name IS NULL OR LOWER(s.firstName) LIKE :name " +
            "OR LOWER(s.lastName) LIKE :name) " +
            "AND (:email is NULL OR LOWER(s.email) = :email) " +
            "AND (CAST(:from AS localdate) IS NULL OR s.enrollmentDate >= :from) " +
            "AND (CAST(:to AS LOCALDATE) IS NULL OR s.enrollmentDate <= :to)")
    Page<Student> search(
            @Param("name") String name,
            @Param("email") String email,
            @Param("from") LocalDate enrollmentDateFrom,
            @Param("to") LocalDate enrollmentDateTo,
            Pageable pageable
    );


}
