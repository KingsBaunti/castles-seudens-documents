CREATE SEQUENCE passport_sequence
    START 1
    INCREMENT 50;


CREATE TABLE passport(
       id BIGINT PRIMARY KEY DEFAULT nextval('passport_sequence'),
       passport_number VARCHAR(25) UNIQUE,
       issue_date DATE
);
