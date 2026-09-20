ALTER TABLE passport
ADD COLUMN student_id BIGINT UNIQUE NOT NULL;
ALTER TABLE passport ADD CONSTRAINT fk_passport_student FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE;


ALTER TABLE transcript
ADD COLUMN student_id BIGINT NOT NULL;
ALTER TABLE transcript ADD CONSTRAINT fk_transcript_student FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE;