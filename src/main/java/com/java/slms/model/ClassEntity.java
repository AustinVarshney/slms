package com.java.slms.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "class_entity"
)

public class ClassEntity extends BaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    private String className;

    @OneToMany(mappedBy = "currentClass", fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference
    private List<Student> students;

    @OneToMany(mappedBy = "classEntity", fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference
    private List<Subject> subjects;

    @OneToOne(mappedBy = "classEntity", fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference
    private FeeStructure feeStructures;

    @OneToMany(mappedBy = "classEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<TimeTable> timetables;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference
    @JoinColumn(name = "session_id")
    private Session session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_teacher_id")
    private Teacher classTeacher;

    @OneToMany(mappedBy = "classEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ClassExam> classExams = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id")
    private School school;

}
