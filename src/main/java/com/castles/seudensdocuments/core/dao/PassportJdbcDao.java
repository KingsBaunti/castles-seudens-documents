package com.castles.seudensdocuments.core.dao;

import com.castles.seudensdocuments.core.model.Passport;
import com.castles.seudensdocuments.core.model.Student;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PassportJdbcDao {

    private final JdbcTemplate jdbc;

    @Value("${jdbc_template.dao.batch_size}")
    private int batchSize;

    public List<Long> getPassportIdList(int numberOfId){
        return jdbc.query(
                "SELECT nextval('passport_sequence') FROM generate_series(1, ?)",
                (rs, rowNum) -> rs.getLong(1),
                numberOfId);
    }

    public void passportBatchCreate(List<Passport> passports){
        jdbc.batchUpdate("INSERT INTO passport " +
                "(id, passport_number, issue_date, student_id) " +
                "VALUES (?, ?, ?, ?)",
                passports,
                batchSize,
                (ps, p) -> {
                    ps.setLong(1, p.getId());
                    ps.setString(2, p.getPassportNumber());
                    ps.setObject(3, p.getIssueDate());
                    ps.setLong(4, p.getStudent().getId());
                });
    }
}
