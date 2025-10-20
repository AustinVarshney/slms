package com.java.slms.repository;

import com.java.slms.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, Long>
{
    @Query("SELECT s FROM Staff s " +
            "WHERE LOWER(s.email) = LOWER(:email) " +
            "AND s.school.id = :schoolId")
    Optional<Staff> findByEmailAndSchoolIdIgnoreCase(
            @Param("email") String email,
            @Param("schoolId") Long schoolId);

    @Query("SELECT s FROM Staff s WHERE s.id = :staffId AND s.school.id = :schoolId")
    Optional<Staff> findByIdAndSchoolId(
            @Param("staffId") Long staffId,
            @Param("schoolId") Long schoolId);

    @Query("SELECT s FROM Staff s " +
            "WHERE LOWER(s.email) = LOWER(:email) " +
            "AND s.school.id = :schoolId")
    Optional<Staff> findByEmailInCurrentSchool(@Param("email") String email,
                                               @Param("schoolId") Long schoolId);

}
