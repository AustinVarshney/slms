package com.java.slms.model;

import com.java.slms.util.Gender;
import com.java.slms.util.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "student",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"class_roll_number", "class_id", "session_id"})
        },
        indexes = {
                @Index(name = "idx_panNumber", columnList = "panNumber"),
                @Index(name = "idx_school_id", columnList = "school_id"),
                @Index(name = "idx_status", columnList = "status")
        }
)
public class Student extends BaseEntity
{

    @Id
    @EqualsAndHashCode.Include
    private String panNumber;

    @Column(name = "class_roll_number", unique = true)
    private Integer classRollNumber;
    private String name;
    private String photo;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Column(name = "parent_name")
    private String parentName;

    @Column(name = "mobile_number")
    private String mobileNumber;

    @Temporal(TemporalType.DATE)
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "address")
    private String address;

    @Column(name = "emergency_contact")
    private String emergencyContact;

    @Column(name = "blood_group")
    private String bloodGroup;

    @Temporal(TemporalType.DATE)
    @Column(name = "admission_date")
    private LocalDate admissionDate;

    @Column(name = "previous_school")
    private String previousSchool;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private Session session;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private ClassEntity currentClass;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Attendance> attendanceRecords;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Fee> fees;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id")
    private School school;

}
