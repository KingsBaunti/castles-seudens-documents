package com.castles.seudensdocuments.core.dao;

import com.castles.seudensdocuments.core.model.Student;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StudentJdbcDao {
    private final JdbcTemplate jdbc;

    public StudentJdbcDao(JdbcTemplate jdbc){
        this.jdbc = jdbc;
    }

    public List<Long> getStudentIdList(int numberOfId){
        return jdbc.query(
                "SELECT nextval('student_sequence') FROM generate_series(1,?)",
                (rs, rowNum) -> rs.getLong(1),
                numberOfId);
    }

    public void studentBatchCreate(List<Student> students){
        jdbc.batchUpdate("INSERT INTO student" +
                "(id, first_name, last_name, email, enrollment_date, avatar)" +
                "VALUES (?, ?, ?, ?, ?, ?)",
                students,
                1000,
                (ps, student) -> {
                    ps.setLong(1, student.getId());
                    ps.setString(2, student.getFirstName());
                    ps.setString(3, student.getLastName());
                    ps.setString(4, student.getEmail());
                    ps.setObject(5, student.getEnrollmentDate());
                    ps.setObject(6, student.getAvatar());
                }
                );

    }

}
