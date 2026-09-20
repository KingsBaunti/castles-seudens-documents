CREATE INDEX idx_student_last_name
ON students_documents_bd.public.student (last_name);

CREATE INDEX idx_student_enrollment_date
ON students_documents_bd.public.student (enrollment_date);