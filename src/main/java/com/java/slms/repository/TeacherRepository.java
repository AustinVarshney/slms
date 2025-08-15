package com.java.slms.repository;

import com.java.slms.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Long>
{
    Optional<Teacher> findByEmailIgnoreCase(String email);

    List<Teacher> findByEmailIn(List<String> emails);

    boolean existsByEmail(String email);
    void deleteByEmail(String email);
}
