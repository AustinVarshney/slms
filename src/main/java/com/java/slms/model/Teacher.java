package com.java.slms.model;

import com.java.slms.util.StudentStatuses;
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
public class Teacher extends BaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String qualification;

    @Enumerated(EnumType.STRING)
    private StudentStatuses status;

//    @ManyToMany(mappedBy = "teachers", fetch = FetchType.LAZY)
//    private List<ClassEntity> classes;

    @OneToMany(mappedBy = "teacher", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Subject> subjects;

}