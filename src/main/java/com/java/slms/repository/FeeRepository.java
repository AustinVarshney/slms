package com.java.slms.repository;

import com.java.slms.model.Fee;
import com.java.slms.util.FeeMonth;
import com.java.slms.util.FeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface FeeRepository extends JpaRepository<Fee, Long>
{
    @Query("SELECT f FROM Fee f WHERE LOWER(f.student.panNumber) = LOWER(:panNumber) AND f.student.school.id = :schoolId AND f.month = :month AND f.session.id = :sessionId")
    List<Fee> findFeesByPanNumberAndSchoolIdAndMonth(@Param("panNumber") String panNumber,
                                                     @Param("schoolId") Long schoolId,
                                                     @Param("month") FeeMonth month,
                                                     @Param("sessionId") Long sessionId);

    @Query("SELECT f FROM Fee f " +
            "WHERE f.student.panNumber = :panNumber " +
            "AND f.school.id = :schoolId " +
            "AND f.session.id = :sessionId " +
            "ORDER BY f.year ASC, f.month ASC")
    List<Fee> findByStudentPanNumberAndSchoolIdOrderByYearAscMonthAsc(@Param("panNumber") String panNumber,
                                                                      @Param("schoolId") Long schoolId,
                                                                      @Param("sessionId") Long sessionId);

    @Query("SELECT f FROM Fee f " +
            "WHERE f.status = :status " +
            "AND f.dueDate <= :dueDate " +
            "AND f.student.id = :studentId " +
            "AND f.feeStructure.session.active = true")
    List<Fee> findOverdueFeesByStudentAndActiveSession(
            @Param("status") FeeStatus status,
            @Param("dueDate") LocalDate dueDate,
            @Param("studentId") Long studentId
    );

    @Modifying
    @Query("DELETE FROM Fee f WHERE f.student.panNumber = :panNumber")
    void deleteByStudent_PanNumber(@Param("panNumber") String panNumber);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END " +
            "FROM Fee f " +
            "WHERE f.student.panNumber = :panNumber " +
            "AND f.month = :month " +
            "AND f.feeStructure.session = :session")
    boolean existsByStudentPanNumberAndMonthAndSession(
            @Param("panNumber") String panNumber,
            @Param("month") com.java.slms.util.FeeMonth month,
            @Param("session") com.java.slms.model.Session session);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END " +
            "FROM Fee f " +
            "WHERE f.student.panNumber = :panNumber " +
            "AND f.month = :month " +
            "AND f.year = :year " +
            "AND f.session = :session")
    boolean existsByStudentPanNumberAndMonthAndYearAndSession(
            @Param("panNumber") String panNumber,
            @Param("month") com.java.slms.util.FeeMonth month,
            @Param("year") int year,
            @Param("session") com.java.slms.model.Session session);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END " +
            "FROM Fee f " +
            "WHERE f.receiptNumber = :receiptNumber")
    boolean existsByReceiptNumber(@Param("receiptNumber") String receiptNumber);

}
