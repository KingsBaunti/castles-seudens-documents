CREATE SEQUENCE transcript_sequence
    START 1
    INCREMENT 50;

CREATE TABLE transcript
(
    id     BIGINT PRIMARY KEY,
    subject VARCHAR(50),
    grade  INTEGER
);