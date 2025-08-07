package com.java.slms.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassEntity extends BaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String className;

    @OneToMany(mappedBy = "currentClass", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Student> students;

    @OneToMany(mappedBy = "classEntity", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Subject> subjects;

    @OneToMany(mappedBy = "classEntity", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Exam> exams;

//    @OneToMany(mappedBy = "classEntity")
//    private List<FeeStructure> feeStructures;
}
