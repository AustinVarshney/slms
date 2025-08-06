package com.java.slms.repository;

import com.java.slms.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, String>
{
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
            "FROM Student s " +
            "JOIN s.currentClass c " +
            "WHERE LOWER(c.className) = LOWER(:className) " +
            "AND LOWER(s.panNumber) = LOWER(:panNumber)")
    boolean existsByClassNameAndPanNumberIgnoreCase(@Param("className") String className,
                                                    @Param("panNumber") String panNumber);


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
