package com.java.slms.repository;

import com.java.slms.model.StudentPromotion;
import com.java.slms.util.PromotionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentPromotionRepository extends JpaRepository<StudentPromotion, Long> {

    @Query("SELECT sp FROM StudentPromotion sp WHERE sp.fromClass.id = :classId " +
            "AND sp.fromSession.id = :sessionId AND sp.schoolId = :schoolId")
    List<StudentPromotion> findByClassAndSession(
            @Param("classId") Long classId,
            @Param("sessionId") Long sessionId,
            @Param("schoolId") Long schoolId
    );

    @Query("SELECT sp FROM StudentPromotion sp WHERE sp.studentPan = :studentPan " +
            "AND sp.fromSession.id = :sessionId AND sp.schoolId = :schoolId")
    Optional<StudentPromotion> findByStudentAndSession(
            @Param("studentPan") String studentPan,
            @Param("sessionId") Long sessionId,
            @Param("schoolId") Long schoolId
    );

    @Query("SELECT sp FROM StudentPromotion sp WHERE sp.fromSession.id = :sessionId " +
            "AND sp.schoolId = :schoolId AND sp.status = :status")
    List<StudentPromotion> findBySessionAndStatus(
            @Param("sessionId") Long sessionId,
            @Param("schoolId") Long schoolId,
            @Param("status") PromotionStatus status
    );

    @Query("SELECT sp FROM StudentPromotion sp WHERE sp.schoolId = :schoolId " +
            "AND sp.fromSession.id = :sessionId")
    List<StudentPromotion> findBySchoolAndSession(
            @Param("schoolId") Long schoolId,
            @Param("sessionId") Long sessionId
    );
}
