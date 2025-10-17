package com.java.slms.repository;

import com.java.slms.model.StaffLeaveRecord;
import com.java.slms.util.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StaffLeaveRecordRepository extends JpaRepository<StaffLeaveRecord, Long>
{
    @Query("SELECT COALESCE(SUM(s.daysRequested), 0) FROM StaffLeaveRecord s " +
            "WHERE s.staff.id = :staffId " +
            "AND s.session.id = :sessionId " +
            "AND s.school.id = :schoolId " +
            "AND s.status = 'APPROVED'")
    int countApprovedLeaves(
            @Param("staffId") Long staffId,
            @Param("sessionId") Long sessionId,
            @Param("schoolId") Long schoolId);

    @Query("SELECT l FROM StaffLeaveRecord l " +
            "WHERE l.staff.id = :staffId AND l.session.active = true " +
            "AND (:status IS NULL OR l.status = :status)")
    List<StaffLeaveRecord> findByTeacherInActiveSessionAndStatus(
            @Param("staffId") Long staffId,
            @Param("status") LeaveStatus status);

    @Query("SELECT l FROM StaffLeaveRecord l " +
            "WHERE l.staff.id = :staffId " +
            "AND l.session.active = true " +
            "AND l.school.id = :schoolId " +
            "AND (:status IS NULL OR l.status = :status)")
    List<StaffLeaveRecord> findByStaffInActiveSessionAndStatus(
            @Param("staffId") Long staffId,
            @Param("status") LeaveStatus status,
            @Param("schoolId") Long schoolId);


    @Query("SELECT l FROM StaffLeaveRecord l " +
            "WHERE (:status IS NULL OR l.status = :status) " +
            "AND (:sessionId IS NULL OR l.session.id = :sessionId) " +
            "AND (:staffId IS NULL OR l.staff.id = :staffId) " +
            "AND (:schoolId IS NULL OR l.school.id = :schoolId)")
    List<StaffLeaveRecord> findByAdminFilters(
            @Param("status") LeaveStatus status,
            @Param("sessionId") Long sessionId,
            @Param("staffId") Long staffId,
            @Param("schoolId") Long schoolId);


}