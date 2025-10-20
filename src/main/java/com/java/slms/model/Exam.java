package com.java.slms.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Exam represents a specific exam instance for a subject within a ClassExam.
 * For example: "Mathematics Midterm for Class 10-A"
 */
@Entity
@Table(name = "exam", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"class_exam_id", "subject_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exam extends BaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_exam_id", nullable = false)
    private ClassExam classExam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private ClassEntity classEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id")
    private School school;

    @Column(name = "exam_date")
    private LocalDate examDate;

    @Column(name = "maximum_marks")
    private Double maximumMarks;

    @Column(name = "pass_marks")
    private Double passMarks;

    private String description;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Score> scores = new HashSet<>();
}
