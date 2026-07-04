package com.castles.seudensdocuments.core.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Transcript {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transcript_generator")
    @SequenceGenerator(sequenceName = "transcript_sequence", name = "transcript_generator", allocationSize = 50)
    Long id;

    String subject;

    int grade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;
}
