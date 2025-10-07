package com.java.slms.repository;

import com.java.slms.model.CalendarEntity;
import com.java.slms.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CalendarRepository extends JpaRepository<CalendarEntity, Long>
{
    List<CalendarEntity> findBySession_Id(Long sessionId);

}
