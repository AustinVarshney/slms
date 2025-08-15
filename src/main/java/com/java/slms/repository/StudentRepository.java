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
            AND a.student.currentClass.id = :classId
            """)
    List<Student> findStudentsPresentTodayByClassName(@Param("classId") Long classId);

    List<Student> findByCurrentClass_Id(Long classId);

    List<Student> findByPanNumberIn(List<String> panNumbers);

}
