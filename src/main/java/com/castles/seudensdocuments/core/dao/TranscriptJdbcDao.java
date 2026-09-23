package com.castles.seudensdocuments.core.dao;

import com.castles.seudensdocuments.core.model.Transcript;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TranscriptJdbcDao {

    private final JdbcTemplate jdbc;

    public TranscriptJdbcDao(JdbcTemplate jdbc){
        this.jdbc = jdbc;
    }

    public List<Long> getTranscriptIdList(int numberOfId){
        return jdbc.query("SELECT nextval('transcript_sequence') FROM generate_series(1, ?)",
                (rs, rowNum) -> rs.getLong(1),
                numberOfId);
    }

    public void createTranscripts(List<Transcript> transcripts){
        jdbc.batchUpdate(
                "INSERT INTO transcript (id, subject, grade, student_id) VALUES(?, ?, ?, ?)",
                transcripts,
                1000,
                (ps, t) -> {
                    ps.setLong(1, t.getId());
                    ps.setString(2, t.getSubject());
                    ps.setInt(3, t.getGrade());
                    ps.setLong(4, t.getStudent().getId());
                });
    }
}
