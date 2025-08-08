package com.java.slms.serviceImpl;

import com.java.slms.dto.TimetableRequestDTO;
import com.java.slms.dto.TimetableResponseDTO;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.exception.WrongArgumentException;
import com.java.slms.model.ClassEntity;
import com.java.slms.model.Subject;
import com.java.slms.model.Teacher;
import com.java.slms.model.TimeTable;
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
        // Do NOT set timetable.setId() – JPA will generate this!
        timetable.setClassEntity(classEntity);
        timetable.setSubject(subject);
        timetable.setTeacher(teacher);
        timetable.setDay(dto.getDay());
        timetable.setStartTime(dto.getStartTime());
        timetable.setEndTime(dto.getEndTime());

        TimeTable saved = timetableRepository.save(timetable);
        return modelMapper.map(saved, TimetableResponseDTO.class);
    }

    @Override
    public List<TimetableResponseDTO> getTimetableByClassId(Long classId)
    {
        if (!classEntityRepository.existsById(classId))
        {
            throw new ResourceNotFoundException("Class not found with ID: " + classId);
        }

        log.info("Fetching timetable for classId={}", classId);
        return timetableRepository.findByClassEntity_Id(classId)
                .stream()
                .map(t -> modelMapper.map(t, TimetableResponseDTO.class))
                .toList();
    }

    @Override
    public List<TimetableResponseDTO> getTimetableByTeacherId(Long teacherId)
    {
        // Ensure teacher exists
        if (!teacherRepository.existsById(teacherId))
        {
            throw new ResourceNotFoundException("Teacher not found with ID: " + teacherId);
        }

        List<TimeTable> timetables = timetableRepository.findByTeacher_Id(teacherId);
        if (timetables.isEmpty())
        {
            throw new ResourceNotFoundException("No timetable found for teacher ID: " + teacherId);
        }

        return timetables.stream()
                .map(t -> modelMapper.map(t, TimetableResponseDTO.class))
                .toList();
    }

    @Override
    public List<TimetableResponseDTO> getTimetableByClassAndDay(Long classId, DayOfWeek day)
    {
        // Ensure class exists
        if (!classEntityRepository.existsById(classId))
        {
            throw new ResourceNotFoundException("Class not found with ID: " + classId);
        }

        List<TimeTable> timetables = timetableRepository.findByClassEntity_IdAndDay(classId, day);
        if (timetables.isEmpty())
        {
            throw new ResourceNotFoundException("No timetable found for class ID " + classId + " on " + day);
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

        // Validate subject belongs to class
        Subject subject = subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with ID: " + dto.getSubjectId()));

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
