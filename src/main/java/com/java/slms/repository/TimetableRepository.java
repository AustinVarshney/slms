package com.java.slms.repository;

import com.java.slms.model.TimeTable;
import com.java.slms.util.DayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalTime;
import java.util.List;

public interface TimetableRepository extends JpaRepository<TimeTable, Long>
{

    List<TimeTable> findByTeacher_IdAndDay(Long teacherId, DayOfWeek day);

    List<TimeTable> findByTeacher_Id(Long teacherId); // ✅ added for getTimetableByTeacherId

    List<TimeTable> findByClassEntity_Id(Long classId);

    List<TimeTable> findByClassEntity_IdAndDay(Long classId, DayOfWeek day);

    boolean existsByClassEntity_IdAndDayAndStartTimeLessThanEqualAndEndTimeGreaterThanEqualAndIdNot(
            Long classId, DayOfWeek day, LocalTime startTime, LocalTime endTime, Long excludeId
    );


}
