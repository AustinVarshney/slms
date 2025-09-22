package com.java.slms.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "class_entity",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"session_id", "className"})
        }
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

    @OneToMany(mappedBy = "classEntity", fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference
    private List<Exam> exams;

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

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "class_teacher_id", unique = true)
    @JsonManagedReference
    private Teacher classTeacher;

}
