package com.java.slms.repository;

import com.java.slms.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, String>
{
}
