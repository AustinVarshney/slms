package com.java.slms.repository;

import com.java.slms.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long>
{
    Optional<Admin> findByEmailIgnoreCase(String email);

    @Query("SELECT a FROM Admin a " +
            "WHERE LOWER(a.email) = LOWER(:email) " +
            "AND a.school.id = :schoolId " +
            "AND a.status = 'ACTIVE'")
    Optional<Admin> findByEmailIgnoreCaseAndSchoolIdAndStatusActive(
            @Param("email") String email,
            @Param("schoolId") Long schoolId);

    @Query("SELECT a FROM Admin a WHERE a.school.id = :schoolId")
    Optional<Admin> findBySchoolId(@Param("schoolId") Long schoolId);

}
