package com.java.slms.repository;

import com.java.slms.model.Teacher;
import com.java.slms.util.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Long>
{
    Optional<Teacher> findByEmailIgnoreCase(String email);

    List<Teacher> findByEmailIn(List<String> emails);

    List<Teacher> findByStatus(UserStatus userStatus);

    Optional<Teacher> findByEmailIgnoreCaseAndStatus(String email, UserStatus status);

    boolean existsByEmail(String email);

    void deleteByEmail(String email);

    @Query("SELECT t FROM Teacher t " +
            "WHERE LOWER(t.email) = LOWER(:email) " +
            "AND t.school.id = :schoolId " +
            "AND t.status = 'ACTIVE'")
    Optional<Teacher> findByEmailIgnoreCaseAndSchoolIdAndStatusActive(
            @Param("email") String email,
            @Param("schoolId") Long schoolId);

    @Query("SELECT t FROM Teacher t " +
            "WHERE LOWER(t.email) = LOWER(:email) " +
            "AND t.school.id = :schoolId ")
    Optional<Teacher> findByEmailIgnoreCaseAndSchoolId(
            @Param("email") String email,
            @Param("schoolId") Long schoolId);

    @Query("SELECT t FROM Teacher t " +
            "WHERE t.id = :teacherId " +
            "AND t.school.id = :schoolId " +
            "AND t.status = 'ACTIVE'")
    Optional<Teacher> findByTeacherIdAndSchoolIdAndStatusActive(
            @Param("teacherId") Long teacherId,
            @Param("schoolId") Long schoolId);

    @Query("SELECT t FROM Teacher t " +
            "WHERE t.id = :teacherId " +
            "AND t.school.id = :schoolId ")
    Optional<Teacher> findByTeacherIdAndSchoolId(
            @Param("teacherId") Long teacherId,
            @Param("schoolId") Long schoolId);

    @Query("SELECT t FROM Teacher t WHERE t.school.id = :schoolId")
    List<Teacher> findAllBySchoolId(@Param("schoolId") Long schoolId);

    @Query("SELECT t FROM Teacher t WHERE t.school.id = :schoolId AND t.status = 'ACTIVE'")
    List<Teacher> findAllBySchoolIdAndActive(@Param("schoolId") Long schoolId);

}
