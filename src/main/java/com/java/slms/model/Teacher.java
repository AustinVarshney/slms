package com.java.slms.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.java.slms.util.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Teacher extends BaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    private String name;
    private String email;
    private String designation;
    private String salaryGrade;
    private String contactNumber;
    private String qualification;
    private LocalDate joiningDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private UserStatus status;


//    @ManyToMany(mappedBy = "teachers", fetch = FetchType.LAZY)
//    private List<ClassEntity> classes;

    @OneToMany(mappedBy = "teacher", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Subject> subjects;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

}