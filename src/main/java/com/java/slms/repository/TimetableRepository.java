package com.java.slms.repository;

import com.java.slms.model.TimeTable;
import com.java.slms.util.DayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface TimetableRepository extends JpaRepository<TimeTable, Long>
{
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM TimeTable t " +
            "WHERE t.classEntity.id = :classId " +
            "AND t.day = :day " +
            "AND t.startTime <= :endTime " +
            "AND t.endTime >= :startTime " +
            "AND t.id <> :excludeId " +
            "AND t.school.id = :schoolId")
    boolean existsOverlapExcludingCurrent(
            @Param("classId") Long classId,
            @Param("day") DayOfWeek day,
            @Param("endTime") LocalTime endTime,
            @Param("startTime") LocalTime startTime,
            @Param("excludeId") Long excludeId,
            @Param("schoolId") Long schoolId);


    @Query("SELECT t FROM TimeTable t WHERE t.classEntity.id = :classId AND t.day = :day AND t.school.id = :schoolId")
    List<TimeTable> findByClassIdAndDayAndSchoolId(
            @Param("classId") Long classId,
            @Param("day") DayOfWeek day,
            @Param("schoolId") Long schoolId);

    @Query("SELECT t FROM TimeTable t WHERE t.teacher.id = :teacherId AND t.day = :day AND t.school.id = :schoolId")
    List<TimeTable> findByTeacherIdAndDayAndSchoolId(
            @Param("teacherId") Long teacherId,
            @Param("day") DayOfWeek day,
            @Param("schoolId") Long schoolId);

    @Query("SELECT t FROM TimeTable t WHERE t.classEntity.id = :classId AND t.session.active = :active AND t.school.id = :schoolId")
    List<TimeTable> findByClassEntity_IdAndSession_ActiveAndSchool_Id(
            @Param("classId") Long classId,
            @Param("active") boolean active,
            @Param("schoolId") Long schoolId);

    @Query("SELECT t FROM TimeTable t " +
            "WHERE t.teacher.id = :teacherId " +
            "AND t.session.active = true " +
            "AND t.school.id = :schoolId")
    List<TimeTable> findByTeacherIdAndActiveSessionAndSchoolId(
            @Param("teacherId") Long teacherId,
            @Param("schoolId") Long schoolId);


    Optional<TimeTable> findByIdAndSchoolId(Long id, Long schoolId);

}
