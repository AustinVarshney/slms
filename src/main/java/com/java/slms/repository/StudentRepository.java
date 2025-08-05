package com.java.slms.repository;

import com.java.slms.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, String>
{
    // Find students who are marked present today
    @Query("SELECT a.student FROM Attendance a WHERE a.present = true AND DATE(a.date) = CURRENT_DATE")
    List<Student> findStudentsPresentToday();

    // Find students who are present today AND belong to specific class
    @Query("""
            SELECT a.student FROM Attendance a 
            WHERE a.present = true 
            AND DATE(a.date) = CURRENT_DATE 
            AND LOWER(a.student.currentClass.className) = LOWER(:className)
            """)
    List<Student> findStudentsPresentTodayByClassName(String className);
}
