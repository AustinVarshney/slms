package com.java.slms.model;

import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "student_term_marks",
        uniqueConstraints = @UniqueConstraint(columnNames = {"enrollment_id", "class_exam_id", "subject_id"})
)

public class StudentTermMarks extends BaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private StudentEnrollments enrollment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_exam_id", nullable = false)
    private ClassExam classExam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(nullable = false)
    private Integer marksObtained;

    @Column(nullable = false)
    private Integer maxMarks;

    @Column(nullable = false)
    private Integer passingMarks;

}
