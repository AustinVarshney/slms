package com.java.slms.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "class_exam", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"class_id", "exam_type_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassExam
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private ClassEntity classEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_type_id", nullable = false)
    private ExamType examType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id")
    private School school;

    private LocalDate examDate;
    
    // Added fields for max marks and passing marks
    @Column(name = "max_marks")
    private Integer maxMarks;
    
    @Column(name = "passing_marks")
    private Integer passingMarks;
}

