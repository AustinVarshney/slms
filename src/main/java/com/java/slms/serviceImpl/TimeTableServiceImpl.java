package com.java.slms.serviceImpl;

import com.java.slms.dto.TimetableRequestDTO;
import com.java.slms.dto.TimetableResponseDTO;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.exception.WrongArgumentException;
import com.java.slms.model.*;
import com.java.slms.repository.*;
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
    private final SchoolRepository schoolRepository;

    @Override
    public TimetableResponseDTO createTimetable(TimetableRequestDTO dto, Long schoolId)
    {
        ClassEntity classEntity = classEntityRepository.findByIdAndSchoolIdAndSessionActive(dto.getClassId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found or not in active session: " + dto.getClassId()));

        Subject subject = subjectRepository.findSubjectByIdAndSchoolIdAndClassId(dto.getSubjectId(), schoolId, dto.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subject not found with ID: " + dto.getSubjectId() +
                                ", School ID: " + schoolId +
                                ", Class ID: " + dto.getClassId()));

        Teacher teacher = subject.getTeacher();

        List<TimeTable> existingSlots = timetableRepository.findByClassIdAndDayAndSchoolId(dto.getClassId(), dto.getDay(), schoolId);
        for (TimeTable slot : existingSlots)
        {
            if (isTimeOverlap(slot.getStartTime(), slot.getEndTime(), dto.getStartTime(), dto.getEndTime()))
            {
                throw new WrongArgumentException("Class already has a timetable slot that overlaps with this time.");
            }
        }

        School school = schoolRepository.findById(schoolId).orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + schoolId));

        preventTeacherDoubleBooking(dto, teacher.getId(), schoolId);

        TimeTable timetable = buildTimeTableEntity(dto, classEntity, subject, teacher, school);

        TimeTable saved = timetableRepository.save(timetable);
        return mapToResponseDTO(saved, classEntity, subject, teacher);
    }

    @Override
    public List<TimetableResponseDTO> getTimetableByTeacherIdInCurrentSession(Long teacherId, Long schoolId)
    {
        if (teacherRepository.findByTeacherIdAndSchoolId(teacherId, schoolId).isEmpty())
        {
            throw new ResourceNotFoundException("Teacher not found with ID: " + teacherId);
        }

        List<TimeTable> timetables = timetableRepository.findByTeacherIdAndActiveSessionAndSchoolId(teacherId, schoolId);

        if (timetables.isEmpty())
        {
            throw new ResourceNotFoundException("No timetable found for teacher ID: " + teacherId + " in school ID: " + schoolId);
        }

        return timetables.stream()
                .map(t -> modelMapper.map(t, TimetableResponseDTO.class))
                .collect(Collectors.toList());
    }


    @Override
    public List<TimetableResponseDTO> getTimetableByClassAndOptionalDay(Long classId, DayOfWeek day, Long schoolId)
    {
        ClassEntity classEntity = classEntityRepository.findByIdAndSchoolIdAndSessionActive(classId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found or not in active session: " + classId));

        List<TimeTable> timetables;

        if (day != null)
        {
            timetables = timetableRepository.findByClassIdAndDayAndSchoolId(classId, day, schoolId);
            if (timetables.isEmpty())
            {
                throw new ResourceNotFoundException("No timetable found for class ID " + classId + " on " + day + " in school " + schoolId);
            }
        }
        else
        {
            timetables = timetableRepository.findByClassEntity_IdAndSession_ActiveAndSchool_Id(classId, true, schoolId);
            if (timetables.isEmpty())
            {
                throw new ResourceNotFoundException("No timetable found for class ID " + classId + " in school " + schoolId);
            }
        }

        return timetables.stream()
                .map(t -> modelMapper.map(t, TimetableResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public TimetableResponseDTO updateTimetable(Long id, TimetableRequestDTO dto, Long schoolId)
    {
        TimeTable existing = timetableRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Timetable not found with ID: " + id));

        validateClassUnchanged(existing, dto.getClassId());

        ClassEntity classEntity = classEntityRepository.findByIdAndSchoolIdAndSessionActive(dto.getClassId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found or not in active session: " + dto.getClassId()));

        Subject subject = subjectRepository.findSubjectByIdAndSchoolIdAndClassId(dto.getSubjectId(), schoolId, dto.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subject not found with ID: " + dto.getSubjectId() +
                                ", School ID: " + schoolId +
                                ", Class ID: " + dto.getClassId()));

        Teacher teacher = subject.getTeacher();
        if (teacher == null)
        {
            throw new WrongArgumentException("No teacher assigned to this subject.");
        }

        validateNoOverlapExcludingCurrent(dto, id, schoolId);

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
    public void deleteTimetable(Long id, Long schoolId)
    {
        TimeTable timetable = timetableRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Timetable not found with ID: " + id + " in school ID: " + schoolId));
        timetableRepository.delete(timetable);
    }

    private void preventTeacherDoubleBooking(TimetableRequestDTO dto, Long teacherId, Long schoolId)
    {
        List<TimeTable> existingSlots = timetableRepository.findByTeacherIdAndDayAndSchoolId(teacherId, dto.getDay(), schoolId);
        for (TimeTable slot : existingSlots)
        {
            if (isTimeOverlap(slot.getStartTime(), slot.getEndTime(), dto.getStartTime(), dto.getEndTime()))
            {
                throw new WrongArgumentException("Teacher is already assigned to another class during this time.");
            }
        }
    }


    private TimeTable buildTimeTableEntity(TimetableRequestDTO dto, ClassEntity classEntity, Subject subject, Teacher teacher, School school)
    {
        TimeTable timetable = new TimeTable();
        timetable.setClassEntity(classEntity);
        timetable.setSession(classEntity.getSession());
        timetable.setSubject(subject);
        timetable.setTeacher(teacher);
        timetable.setDay(dto.getDay());
        timetable.setStartTime(dto.getStartTime());
        timetable.setEndTime(dto.getEndTime());
        timetable.setSchool(school);
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

    private void validateClassUnchanged(TimeTable existing, Long newClassId)
    {
        if (!existing.getClassEntity().getId().equals(newClassId))
        {
            throw new WrongArgumentException("Cannot change class for timetable entry.");
        }
    }

    private void validateNoOverlapExcludingCurrent(TimetableRequestDTO dto, Long currentTimetableId, Long schoolId)
    {
        boolean overlapExists = timetableRepository.existsOverlapExcludingCurrent(
                dto.getClassId(),
                dto.getDay(),
                dto.getEndTime(),
                dto.getStartTime(),
                currentTimetableId,
                schoolId
        );

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
