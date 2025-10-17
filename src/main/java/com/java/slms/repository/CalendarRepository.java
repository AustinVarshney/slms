package com.java.slms.repository;

import com.java.slms.model.CalendarEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CalendarRepository extends JpaRepository<CalendarEntity, Long>
{

    @Query("SELECT c FROM CalendarEntity c WHERE c.school.id = :schoolId")
    List<CalendarEntity> findAllBySchoolId(@Param("schoolId") Long schoolId);

    @Query("SELECT c FROM CalendarEntity c WHERE c.id = :id AND c.school.id = :schoolId")
    Optional<CalendarEntity> findByIdAndSchoolId(@Param("id") Long id, @Param("schoolId") Long schoolId);

    @Query("SELECT c FROM CalendarEntity c WHERE c.session.id = :sessionId AND c.school.id = :schoolId")
    List<CalendarEntity> findBySessionIdAndSchoolId(@Param("sessionId") Long sessionId, @Param("schoolId") Long schoolId);
}
