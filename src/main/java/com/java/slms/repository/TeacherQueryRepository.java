package com.java.slms.repository;

import com.java.slms.model.Admin;
import com.java.slms.model.Teacher;
import com.java.slms.model.TeacherQuery;
import com.java.slms.util.QueryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeacherQueryRepository extends JpaRepository<TeacherQuery, Long>
{
    List<TeacherQuery> findByTeacher(Teacher teacher);

    List<TeacherQuery> findByTeacherAndStatus(Teacher teacher, QueryStatus status);

    List<TeacherQuery> findByAdmin(Admin admin);

    List<TeacherQuery> findByAdminAndStatus(Admin admin, QueryStatus status);
}
