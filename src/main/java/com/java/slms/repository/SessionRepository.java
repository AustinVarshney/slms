package com.java.slms.repository;

import com.java.slms.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long>
{
    Optional<Session> findByName(String name);

    Optional<Session> findByActiveTrue();

    boolean existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate endDate, LocalDate startDate);

    @Query("SELECT s FROM Session s WHERE s.school.id = :schoolId AND s.active = true")
    Optional<Session> findBySchoolIdAndActiveTrue(Long schoolId);

    boolean existsBySchoolIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long schoolId, LocalDate endDate, LocalDate startDate);

    @Query("SELECT s FROM Session s WHERE s.school.id = :schoolId")
    List<Session> findAllBySchoolId(@Param("schoolId") Long schoolId);

    @Query("SELECT s FROM Session s WHERE s.school.id = :schoolId AND s.id = :sessionId")
    Optional<Session> findBySessionIdAndSchoolId(@Param("sessionId") Long sessionId, @Param("schoolId") Long schoolId);

    @Query("SELECT s FROM Session s WHERE s.id = :id AND s.school.id = :schoolId AND s.active = true")
    Optional<Session> findActiveSessionByIdAndSchoolId(@Param("id") Long id, @Param("schoolId") Long schoolId);

}