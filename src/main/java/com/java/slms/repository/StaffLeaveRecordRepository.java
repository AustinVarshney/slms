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
            "WHERE s.teacher.id = :teacherId AND s.session.id = :sessionId AND s.status = 'APPROVED'")
    int countApprovedLeaves(@Param("teacherId") Long teacherId, @Param("sessionId") Long sessionId);

    @Query("SELECT l FROM StaffLeaveRecord l " +
            "WHERE l.teacher.id = :teacherId AND l.session.active = true " +
            "AND (:status IS NULL OR l.status = :status)")
    List<StaffLeaveRecord> findByTeacherInActiveSessionAndStatus(
            @Param("teacherId") Long teacherId,
            @Param("status") LeaveStatus status);

    @Query("SELECT l FROM StaffLeaveRecord l " +
            "WHERE (:status IS NULL OR l.status = :status) " +
            "AND (:sessionId IS NULL OR l.session.id = :sessionId) " +
            "AND (:teacherId IS NULL OR l.teacher.id = :teacherId)")
    List<StaffLeaveRecord> findByAdminFilters(@Param("status") LeaveStatus status,
                                              @Param("sessionId") Long sessionId,
                                              @Param("teacherId") Long teacherId);


}