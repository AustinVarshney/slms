package com.java.slms.repository;

import com.java.slms.model.Session;
import com.java.slms.model.Staff;
import com.java.slms.model.StaffLeaveAllowance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StaffLeaveAllowanceRepository extends JpaRepository<StaffLeaveAllowance, Long>
{
    @Query("SELECT s FROM StaffLeaveAllowance s " +
            "WHERE s.staff = :staff " +
            "AND s.session = :session " +
            "AND s.staff.school.id = :schoolId")
    Optional<StaffLeaveAllowance> findByStaffAndSessionAndSchoolId(
            @Param("staff") Staff staff,
            @Param("session") Session session,
            @Param("schoolId") Long schoolId);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
            "FROM StaffLeaveAllowance s " +
            "WHERE s.staff = :staff " +
            "AND s.session = :session " +
            "AND s.school.id = :schoolId")
    boolean existsByStaffAndSessionAndSchoolId(
            @Param("staff") Staff staff,
            @Param("session") Session session,
            @Param("schoolId") Long schoolId);

    @Query("SELECT s FROM StaffLeaveAllowance s " +
            "WHERE s.session = :session AND s.school.id = :schoolId")
    List<StaffLeaveAllowance> findBySessionAndSchoolId(
            @Param("session") Session session,
            @Param("schoolId") Long schoolId);

    @Query("SELECT s FROM StaffLeaveAllowance s " +
            "WHERE s.session.id = :sessionId AND s.school.id = :schoolId")
    List<StaffLeaveAllowance> findAllBySessionIdAndSchoolId(
            @Param("sessionId") Long sessionId,
            @Param("schoolId") Long schoolId);

    @Query("SELECT s FROM StaffLeaveAllowance s " +
            "WHERE s.id = :allowanceId AND s.session.active = true AND s.school.id = :schoolId")
    Optional<StaffLeaveAllowance> findByIdAndActiveSessionAndSchoolId(
            @Param("allowanceId") Long allowanceId,
            @Param("schoolId") Long schoolId);



}
