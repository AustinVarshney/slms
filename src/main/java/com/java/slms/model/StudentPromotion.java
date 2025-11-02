package com.java.slms.model;

import com.java.slms.util.PromotionStatus;
import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = false)
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "student_promotion",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"student_pan", "from_session_id", "school_id"})
        },
        indexes = {
                @Index(name = "idx_promotion_school", columnList = "school_id"),
                @Index(name = "idx_promotion_session", columnList = "from_session_id"),
                @Index(name = "idx_promotion_status", columnList = "status")
        }
)
public class StudentPromotion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_pan", nullable = false)
    private String studentPan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_class_id", nullable = false)
    private ClassEntity fromClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_class_id")
    private ClassEntity toClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_session_id", nullable = false)
    private Session fromSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_session_id")
    private Session toSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_teacher_id")
    private Teacher assignedByTeacher;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PromotionStatus status;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @Column(name = "is_graduated")
    private Boolean isGraduated = false;
}
