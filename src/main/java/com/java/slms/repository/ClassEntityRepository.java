package com.java.slms.repository;

import com.java.slms.model.ClassEntity;
import com.java.slms.model.TimeTable;
import com.java.slms.util.DayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClassEntityRepository extends JpaRepository<ClassEntity, Long>
{
    Optional<ClassEntity> findByClassNameIgnoreCaseAndSessionId(String className, Long sessionId);

    @Query("SELECT c FROM ClassEntity c " +
            "WHERE c.id = :classId " +
            "AND c.session.id = :sessionId " +
            "AND c.school.id = :schoolId")
    Optional<ClassEntity> findByIdAndSessionIdAndSchoolId(
            @Param("classId") Long classId,
            @Param("sessionId") Long sessionId,
            @Param("schoolId") Long schoolId);

    @Query("SELECT c FROM ClassEntity c " +
            "WHERE c.id = :classId " +
            "AND c.session.active = true " +
            "AND c.school.id = :schoolId")
    Optional<ClassEntity> findByIdAndSchoolIdAndSessionActive(
            @Param("classId") Long classId,
            @Param("schoolId") Long schoolId);

    @Query("SELECT c FROM ClassEntity c WHERE LOWER(c.className) = LOWER(:className) AND c.session.id = :sessionId AND c.school.id = :schoolId")
    Optional<ClassEntity> findByClassNameIgnoreCaseAndSessionIdAndSchoolId(
            @Param("className") String className,
            @Param("sessionId") Long sessionId,
            @Param("schoolId") Long schoolId
    );

    @Query("SELECT c FROM ClassEntity c " +
            "WHERE c.id = :classId " +
            "AND c.school.id = :schoolId")
    Optional<ClassEntity> findByIdAndSchoolId(
            @Param("classId") Long classId,
            @Param("schoolId") Long schoolId);

    @Query("SELECT c FROM ClassEntity c WHERE c.session.id = :sessionId AND c.school.id = :schoolId")
    List<ClassEntity> findBySession_IdAndSchool_Id(@Param("sessionId") Long sessionId, @Param("schoolId") Long schoolId);


    @Query("SELECT t FROM TimeTable t WHERE t.classEntity.id = :classId AND t.day = :day AND t.school.id = :schoolId")
    List<TimeTable> findByClassEntity_IdAndDayAndSchool_Id(
            @Param("classId") Long classId,
            @Param("day") DayOfWeek day,
            @Param("schoolId") Long schoolId);

    @Query("SELECT t FROM TimeTable t WHERE t.classEntity.id = :classId AND t.session.active = :active AND t.school.id = :schoolId")
    List<TimeTable> findByClassEntity_IdAndSession_ActiveAndSchool_Id(
            @Param("classId") Long classId,
            @Param("active") boolean active,
            @Param("schoolId") Long schoolId);

    @Query("SELECT c FROM ClassEntity c WHERE c.classTeacher.id = :teacherId")
    List<ClassEntity> findAllByClassTeacher_Id(@Param("teacherId") Long teacherId);

}
