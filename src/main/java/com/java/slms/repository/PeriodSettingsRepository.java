package com.java.slms.repository;

import com.java.slms.model.PeriodSettings;
import com.java.slms.model.School;
import com.java.slms.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PeriodSettingsRepository extends JpaRepository<PeriodSettings, Long> {
    
    Optional<PeriodSettings> findBySchoolAndSession(School school, Session session);
    
    Optional<PeriodSettings> findBySchoolIdAndSessionId(Long schoolId, Long sessionId);
    
    Optional<PeriodSettings> findFirstBySchoolIdOrderByCreatedAtDesc(Long schoolId);
}
