package com.java.slms.repository;

import com.java.slms.model.Student;
import com.java.slms.model.StudentQuery;
import com.java.slms.model.Teacher;
import com.java.slms.util.QueryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentQueryRepository extends JpaRepository<StudentQuery, Long>
{
    List<StudentQuery> findByTeacherId(Long teacherId);

    List<StudentQuery> findByStudent_panNumber(String pan);

    List<StudentQuery> findByStudent(Student student);

    List<StudentQuery> findByStudentAndStatus(Student student, QueryStatus status);

    List<StudentQuery> findByTeacher(Teacher teacher);

    List<StudentQuery> findByTeacherAndStatus(Teacher teacher, QueryStatus status);


}
