package com.java.slms.repository;

import com.java.slms.model.School;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SchoolRepository extends JpaRepository<School, Long>
{
    Optional<School> findBySchoolName(String schoolName);
}
