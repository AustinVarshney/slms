package com.java.slms.repository;

import com.java.slms.model.StudentEnrollments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

}
