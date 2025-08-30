package com.java.slms.repository;

import com.java.slms.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long>
{
    List<Event> findBySession_Id(Long sessionId);
}
