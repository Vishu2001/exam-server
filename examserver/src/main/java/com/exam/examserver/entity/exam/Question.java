package com.exam.examserver.entity.exam;

import jakarta.persistence.*;

@Entity
@Table(name = "question")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long questionId;

    @Column(length = 5000)
    private String content;
    private String image;

    @Column(length = 5000)
    private String option1;
    @Column(length = 5000)
    private String option2;
    @Column(length = 5000)
    private String option3;
    @Column(length = 5000)
    private String option4;

    private String answer;

    @ManyToOne(fetch = FetchType.EAGER)
    private Quiz quiz;

}
