package com.java.slms.repository;

import com.java.slms.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>
{
    List<Event> findBySession_Id(Long sessionId);

    @Query("SELECT e FROM Event e WHERE e.id = :eventId AND e.school.id = :schoolId")
    Optional<Event> findByIdAndSchoolId(@Param("eventId") Long eventId, @Param("schoolId") Long schoolId);

    @Query("SELECT e FROM Event e WHERE e.id = :eventId AND e.school.id = :schoolId AND e.session.active = true")
    Optional<Event> findByIdAndSchoolIdAndSessionActive(@Param("eventId") Long eventId, @Param("schoolId") Long schoolId);

    @Query("SELECT e FROM Event e WHERE e.session.id = :sessionId AND e.school.id = :schoolId")
    List<Event> findBySessionIdAndSchoolId(@Param("sessionId") Long sessionId, @Param("schoolId") Long schoolId);

    @Query("SELECT e FROM Event e WHERE e.school.id = :schoolId AND e.session.active = true")
    List<Event> findBySchoolIdAndSessionActive(@Param("schoolId") Long schoolId);
}
