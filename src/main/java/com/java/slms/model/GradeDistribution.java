package com.java.slms.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * GradeDistribution entity defines the grading system for a school
 * Schools can customize their grade boundaries
 */
@Entity
@Table(name = "grade_distribution", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"school_id", "grade"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeDistribution extends BaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Column(nullable = false, length = 5)
    private String grade; // A+, A, B+, B, C, D, E, F

    @Column(name = "min_percentage", nullable = false)
    private Double minPercentage; // Minimum percentage for this grade

    @Column(name = "max_percentage", nullable = false)
    private Double maxPercentage; // Maximum percentage for this grade

    @Column(length = 100)
    private String description; // Optional description
}
