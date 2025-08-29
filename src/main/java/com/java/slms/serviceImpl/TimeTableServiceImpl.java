package com.java.slms.serviceImpl;

import com.java.slms.dto.TimetableRequestDTO;
import com.java.slms.dto.TimetableResponseDTO;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.exception.WrongArgumentException;
import com.java.slms.model.*;
import com.java.slms.repository.ClassEntityRepository;
import com.java.slms.repository.SubjectRepository;
import com.java.slms.repository.TeacherRepository;
import com.java.slms.repository.TimetableRepository;
import com.java.slms.service.TimeTableService;
import com.java.slms.util.DayOfWeek;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class TimeTableServiceImpl implements TimeTableService
{
    private final ClassEntityRepository classEntityRepository;
    private final SubjectRepository subjectRepository;
    private final TimetableRepository timetableRepository;
    private final TeacherRepository teacherRepository;
    private final ModelMapper modelMapper;

    @Override
    public TimetableResponseDTO createTimetable(TimetableRequestDTO dto)
    {
        ClassEntity classEntity = classEntityRepository.findById(dto.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));

        Subject subject = subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));

        Teacher teacher = subject.getTeacher();

        // Check that subject belongs to the given class
        if (!subject.getClassEntity().getId().equals(dto.getClassId()))
        {
            throw new WrongArgumentException("Subject does not belong to the selected class.");
        }

        if (!classEntity.getSession().isActive())
        {
            throw new WrongArgumentException("Class does not belong to active session");
        }

        // Prevent overlapping time for the same class
        List<TimeTable> existingClassSlots = timetableRepository.findByClassEntity_IdAndDay(dto.getClassId(), dto.getDay());
        for (TimeTable slot : existingClassSlots)
        {
            if (isTimeOverlap(slot.getStartTime(), slot.getEndTime(), dto.getStartTime(), dto.getEndTime()))
            {
                throw new WrongArgumentException("Class already has a timetable slot that overlaps with this time.");
            }
        }

        // Prevent teacher double-booking
        List<TimeTable> existingTeacherSlots = timetableRepository.findByTeacher_IdAndDay(teacher.getId(), dto.getDay());
        for (TimeTable slot : existingTeacherSlots)
        {
            if (isTimeOverlap(slot.getStartTime(), slot.getEndTime(), dto.getStartTime(), dto.getEndTime()))
            {
                throw new WrongArgumentException("Teacher is already assigned to another class during this time.");
            }
        }

        // Manually map DTO fields
        TimeTable timetable = new TimeTable();
        timetable.setClassEntity(classEntity);
        timetable.setSession(classEntity.getSession());
        timetable.setSubject(subject);
        timetable.setTeacher(teacher);
        timetable.setDay(dto.getDay());
        timetable.setStartTime(dto.getStartTime());
        timetable.setEndTime(dto.getEndTime());

        TimeTable saved = timetableRepository.save(timetable);
        TimetableResponseDTO timetableResponseDTO = modelMapper.map(saved, TimetableResponseDTO.class);
        timetableResponseDTO.setClassId(classEntity.getId());
        timetableResponseDTO.setClassName(classEntity.getClassName());
        timetableResponseDTO.setSubjectId(subject.getId());
        timetableResponseDTO.setTeacherId(teacher.getId());
        return timetableResponseDTO;
    }

    @Override
    public List<TimetableResponseDTO> getTimetableByTeacherIdInCurrentSession(Long teacherId)
    {
        // Ensure teacher exists
        if (!teacherRepository.existsById(teacherId))
        {
            throw new ResourceNotFoundException("Teacher not found with ID: " + teacherId);
        }

        List<TimeTable> timetables = timetableRepository.findByTeacher_IdAndSession_Active(teacherId, true);
        if (timetables.isEmpty())
        {
            throw new ResourceNotFoundException("No timetable found for teacher ID: " + teacherId);
        }

        return timetables.stream()
                .map(t -> modelMapper.map(t, TimetableResponseDTO.class))
                .toList();
    }

    @Override
    public List<TimetableResponseDTO> getTimetableByClassAndOptionalDay(Long classId, DayOfWeek day)
    {
        // Ensure class exists and belongs to active session
        ClassEntity classEntity = classEntityRepository.findByIdAndSession_Active(classId, true)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found or not in active session: " + classId));

        List<TimeTable> timetables;

        if (day != null)
        {
            timetables = timetableRepository.findByClassEntity_IdAndDay(classId, day);
            if (timetables.isEmpty())
            {
                throw new ResourceNotFoundException("No timetable found for class ID " + classId + " on " + day);
            }
        }
        else
        {
            timetables = timetableRepository.findByClassEntity_IdAndSession_Active(classId, true);
            if (timetables.isEmpty())
            {
                throw new ResourceNotFoundException("No timetable found for class ID " + classId);
            }
        }

        return timetables.stream()
                .map(t -> modelMapper.map(t, TimetableResponseDTO.class))
                .toList();
    }

    @Override
    public TimetableResponseDTO updateTimetable(Long id, TimetableRequestDTO dto)
    {
        TimeTable existing = timetableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Timetable not found with ID: " + id));

        // Validate class
        if (!existing.getClassEntity().getId().equals(dto.getClassId()))
        {
            throw new WrongArgumentException("Cannot change class for timetable entry.");
        }

        ClassEntity classEntity = classEntityRepository.findByIdAndSession_Active(dto.getClassId(), true)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found or not in active session: " + dto.getClassId()));


        // Validate subject belongs to class
        Subject subject = subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with ID: " + dto.getSubjectId()));

        if (!subject.getClassEntity().getSession().isActive())
        {
            throw new WrongArgumentException("Subject does not below to the active Session " + subject.getId());
        }

        if (!subject.getClassEntity().getId().equals(dto.getClassId()))
        {
            throw new WrongArgumentException("Subject does not belong to the given class.");
        }

        // Validate teacher
        Teacher teacher = subject.getTeacher();
        if (teacher == null)
        {
            throw new WrongArgumentException("No teacher assigned to this subject.");
        }

        // Check time overlap excluding current timetable entry
        boolean overlapExists = timetableRepository.existsByClassEntity_IdAndDayAndStartTimeLessThanEqualAndEndTimeGreaterThanEqualAndIdNot(
                dto.getClassId(), dto.getDay(), dto.getEndTime(), dto.getStartTime(), id);
        if (overlapExists)
        {
            throw new WrongArgumentException("Time slot overlaps with an existing timetable entry for this class.");
        }

        existing.setSubject(subject);
        existing.setTeacher(teacher);
        existing.setDay(dto.getDay());
        existing.setStartTime(dto.getStartTime());
        existing.setEndTime(dto.getEndTime());
        existing.setSession(classEntity.getSession());

        TimeTable updated = timetableRepository.save(existing);
        return modelMapper.map(updated, TimetableResponseDTO.class);
    }

    @Override
    public void deleteTimetable(Long id)
    {
        TimeTable timetable = timetableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Timetable not found with ID: " + id));
        timetableRepository.delete(timetable);
    }

    private boolean isTimeOverlap(LocalTime existingStart, LocalTime existingEnd,
                                  LocalTime newStart, LocalTime newEnd)
    {
        return (newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart));
    }

}
