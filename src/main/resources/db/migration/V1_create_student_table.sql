CREATE SEQUENCE student_sequence
    START 1
    INCREMENT 50;


CREATE TABLE student(
    id BIGINT PRIMARY KEY,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    email VARCHAR(255) UNIQUE,
    enrollment_date DATE
);