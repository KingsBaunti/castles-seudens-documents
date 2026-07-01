package com.castles.seudensdocuments.core.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Passport {
    //TODO старый паспорт не удаляется при его замене апдейтом у пользователя
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "passport_generator")
    @SequenceGenerator(sequenceName = "passport_sequence", name = "passport_generator", allocationSize = 50)
    Long id;

    String passportNumber;

    LocalDate issueDate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    Student student;


}
