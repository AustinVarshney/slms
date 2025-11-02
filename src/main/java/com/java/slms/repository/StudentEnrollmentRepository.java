package com.java.slms.repository;

import com.java.slms.model.StudentEnrollments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentEnrollmentRepository extends JpaRepository<StudentEnrollments, Long>
{
    @Query("SELECT se FROM StudentEnrollments se " +
            "WHERE se.classEntity.id = :classId " +
            "AND se.school.id = :schoolId " +
            "AND se.session.id = :sessionId " +
            "AND se.student.panNumber = :pan")
    Optional<StudentEnrollments> findByClassIdAndSchoolIdAndSessionIdAndPan(@Param("classId") Long classId,
                                                                            @Param("schoolId") Long schoolId,
                                                                            @Param("sessionId") Long sessionId,
                                                                            @Param("pan") String pan);

    @Query("SELECT se FROM StudentEnrollments se " +
            "WHERE LOWER(se.student.panNumber) = LOWER(:panNumber) " +
            "AND se.school.id = :schoolId " +
            "ORDER BY se.session.startDate DESC")
    List<StudentEnrollments> findByStudent_PanNumberIgnoreCaseAndStudent_School_IdOrderBySession_StartYearDesc(
            @Param("panNumber") String panNumber,
            @Param("schoolId") Long schoolId);

    @Query("SELECT CASE WHEN COUNT(se) > 0 THEN true ELSE false END FROM StudentEnrollments se " +
            "WHERE se.student.panNumber = :panNumber " +
            "AND se.session.id = :sessionId " +
            "AND se.classEntity.id = :classId")
    boolean existsByStudent_PanNumberAndSession_IdAndClassEntity_Id(
            @Param("panNumber") String panNumber,
            @Param("sessionId") Long sessionId,
            @Param("classId") Long classId);

}
