package com.java.slms.model;

import com.java.slms.util.StudentStatus;
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
    private String name;
    private String photo;
    @Enumerated(EnumType.STRING)
    private StudentStatus status;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Attendance> attendanceRecords;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Score> scores;

    @ManyToOne
    @JoinColumn(name = "class_id")
    private ClassEntity currentClass;

}
