package com.java.slms.model;

import com.java.slms.util.FeeCatalogStatus;
import com.java.slms.util.FeeStatus;
import com.java.slms.util.Gender;
import com.java.slms.util.UserStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "student")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student extends BaseEntity
{

    @Id
    private String panNumber;

    @Column(name = "class_roll_number")
    private Integer classRollNumber;
    private String name;
    private String photo;

    @Enumerated(EnumType.STRING)
    @Column(name = "fee_status")
    private FeeStatus feeStatus;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "fee_catalog_status")
    private FeeCatalogStatus feeCatalogStatus;

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

    @ManyToOne
    @JoinColumn(name = "session_id")
    private Session session;


    @ManyToOne
    @JoinColumn(name = "class_id")
    private ClassEntity currentClass;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Attendance> attendanceRecords;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Score> scores;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Fee> fees;
}
