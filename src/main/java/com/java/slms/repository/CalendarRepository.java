package com.java.slms.repository;

import com.java.slms.model.CalendarEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CalendarRepository extends JpaRepository<CalendarEntity, Long>
{
}
