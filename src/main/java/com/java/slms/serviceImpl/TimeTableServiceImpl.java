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
import java.util.stream.Collectors;

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
        ClassEntity classEntity = fetchActiveClass(dto.getClassId());
        Subject subject = fetchSubject(dto.getSubjectId());
        validateSubjectBelongsToClass(subject, dto.getClassId());

        Teacher teacher = subject.getTeacher();

        preventOverlappingForClass(dto, classEntity.getId());
        preventTeacherDoubleBooking(dto, teacher.getId());

        TimeTable timetable = buildTimeTableEntity(dto, classEntity, subject, teacher);

        TimeTable saved = timetableRepository.save(timetable);
        return mapToResponseDTO(saved, classEntity, subject, teacher);
    }

    @Override
    public List<TimetableResponseDTO> getTimetableByTeacherIdInCurrentSession(Long teacherId)
    {
        ensureTeacherExists(teacherId);

        List<TimeTable> timetables = timetableRepository.findByTeacher_IdAndSession_Active(teacherId, true);

        if (timetables.isEmpty())
        {
            throw new ResourceNotFoundException("No timetable found for teacher ID: " + teacherId);
        }

        return timetables.stream()
                .map(t -> modelMapper.map(t, TimetableResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<TimetableResponseDTO> getTimetableByClassAndOptionalDay(Long classId, DayOfWeek day)
    {
        ClassEntity classEntity = fetchActiveClass(classId);

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
                .collect(Collectors.toList());
    }

    @Override
    public TimetableResponseDTO updateTimetable(Long id, TimetableRequestDTO dto)
    {
        TimeTable existing = timetableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Timetable not found with ID: " + id));

        validateClassUnchanged(existing, dto.getClassId());
        ClassEntity classEntity = fetchActiveClass(dto.getClassId());

        Subject subject = fetchSubject(dto.getSubjectId());
        validateSubjectBelongsToClass(subject, dto.getClassId());

        Teacher teacher = subject.getTeacher();
        if (teacher == null)
        {
            throw new WrongArgumentException("No teacher assigned to this subject.");
        }

        validateNoOverlapExcludingCurrent(dto, id);

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

    private ClassEntity fetchActiveClass(Long classId)
    {
        return classEntityRepository.findByIdAndSession_Active(classId, true)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found or not in active session: " + classId));
    }

    private Subject fetchSubject(Long subjectId)
    {
        return subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with ID: " + subjectId));
    }

    private void validateSubjectBelongsToClass(Subject subject, Long classId)
    {
        if (!subject.getClassEntity().getId().equals(classId))
        {
            throw new WrongArgumentException("Subject does not belong to the selected class.");
        }
        if (!subject.getClassEntity().getSession().isActive())
        {
            throw new WrongArgumentException("Subject does not belong to the active session.");
        }
    }

    private void preventOverlappingForClass(TimetableRequestDTO dto, Long classId)
    {
        List<TimeTable> existingSlots = timetableRepository.findByClassEntity_IdAndDay(classId, dto.getDay());
        for (TimeTable slot : existingSlots)
        {
            if (isTimeOverlap(slot.getStartTime(), slot.getEndTime(), dto.getStartTime(), dto.getEndTime()))
            {
                throw new WrongArgumentException("Class already has a timetable slot that overlaps with this time.");
            }
        }
    }

    private void preventTeacherDoubleBooking(TimetableRequestDTO dto, Long teacherId)
    {
        List<TimeTable> existingSlots = timetableRepository.findByTeacher_IdAndDay(teacherId, dto.getDay());
        for (TimeTable slot : existingSlots)
        {
            if (isTimeOverlap(slot.getStartTime(), slot.getEndTime(), dto.getStartTime(), dto.getEndTime()))
            {
                throw new WrongArgumentException("Teacher is already assigned to another class during this time.");
            }
        }
    }

    private TimeTable buildTimeTableEntity(TimetableRequestDTO dto, ClassEntity classEntity, Subject subject, Teacher teacher)
    {
        TimeTable timetable = new TimeTable();
        timetable.setClassEntity(classEntity);
        timetable.setSession(classEntity.getSession());
        timetable.setSubject(subject);
        timetable.setTeacher(teacher);
        timetable.setDay(dto.getDay());
        timetable.setStartTime(dto.getStartTime());
        timetable.setEndTime(dto.getEndTime());
        return timetable;
    }

    private TimetableResponseDTO mapToResponseDTO(TimeTable timetable, ClassEntity classEntity, Subject subject, Teacher teacher)
    {
        TimetableResponseDTO dto = modelMapper.map(timetable, TimetableResponseDTO.class);
        dto.setClassId(classEntity.getId());
        dto.setClassName(classEntity.getClassName());
        dto.setSubjectId(subject.getId());
        dto.setTeacherId(teacher.getId());
        return dto;
    }

    private void ensureTeacherExists(Long teacherId)
    {
        if (!teacherRepository.existsById(teacherId))
        {
            throw new ResourceNotFoundException("Teacher not found with ID: " + teacherId);
        }
    }

    private void validateClassUnchanged(TimeTable existing, Long newClassId)
    {
        if (!existing.getClassEntity().getId().equals(newClassId))
        {
            throw new WrongArgumentException("Cannot change class for timetable entry.");
        }
    }

    private void validateNoOverlapExcludingCurrent(TimetableRequestDTO dto, Long currentTimetableId)
    {
        boolean overlapExists = timetableRepository.existsByClassEntity_IdAndDayAndStartTimeLessThanEqualAndEndTimeGreaterThanEqualAndIdNot(
                dto.getClassId(), dto.getDay(), dto.getEndTime(), dto.getStartTime(), currentTimetableId);
        if (overlapExists)
        {
            throw new WrongArgumentException("Time slot overlaps with an existing timetable entry for this class.");
        }
    }

    private boolean isTimeOverlap(LocalTime existingStart, LocalTime existingEnd,
                                  LocalTime newStart, LocalTime newEnd)
    {
        // Overlap exists if new start is before existing end and new end is after existing start
        return newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart);
    }
}
