package com.java.slms.model;

import com.java.slms.util.FeeStatus;
import com.java.slms.util.UserStatus;
import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDate;

@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(
        name = "non_teaching_staff",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id"})
        }
)
public class NonTeachingStaff extends BaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    private String name;
    private String email;
    private String designation;
    private String contactNumber;
    private String qualification;
    private LocalDate joiningDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private UserStatus status;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id")
    private School school;

}
