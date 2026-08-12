package com.castles.seudensdocuments.core.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

//import static sun.security.ec.ECOperations.Secp256R1GeneratorMontgomeryMultiplier.generator; ???????

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "student_generator")
    @SequenceGenerator(sequenceName = "student_sequence", name = "student_generator", allocationSize = 50)
    Long id;

    String firstName;

    String lastName;

    String email;

    LocalDate enrollmentDate;

    @OneToOne(mappedBy = "student",
            orphanRemoval = true,
            cascade = CascadeType.ALL)
    Passport passport;

    @OneToMany(mappedBy = "student",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true)
    List<Transcript> transcripts;


    @Column(columnDefinition = "BYTEA")
    byte[] avatar;


}
