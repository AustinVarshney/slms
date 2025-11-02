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
import java.util.ArrayList;
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
        
        // Validate that subject has an assigned teacher
        if (teacher == null) {
            throw new WrongArgumentException("Cannot create timetable: Subject '" + subject.getSubjectName() + 
                    "' does not have a teacher assigned. Please assign a teacher to this subject first.");
        }

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

        // Return empty list instead of throwing exception when no timetable exists
        // This is normal for new sessions where timetables haven't been created yet
        if (timetables.isEmpty())
        {
            log.info("No timetable found for teacher ID: {} in active session for school ID: {}. Returning empty list.", teacherId, schoolId);
            return new ArrayList<>();
        }

        return timetables.stream()
                .map(t -> {
                    TimetableResponseDTO dto = modelMapper.map(t, TimetableResponseDTO.class);
                    // Manually set IDs and names that modelMapper might not map correctly
                    if (t.getClassEntity() != null) {
                        dto.setClassId(t.getClassEntity().getId());
                        dto.setClassName(t.getClassEntity().getClassName());
                        
                        // Parse className (e.g., "4-A") into className and section
                        String fullClassName = t.getClassEntity().getClassName();
                        if (fullClassName != null && fullClassName.contains("-")) {
                            String[] parts = fullClassName.split("-");
                            dto.setClassName(parts[0]); // e.g., "4", "10"
                            dto.setSection(parts.length > 1 ? parts[1] : "A");
                        }
                    }
                    if (t.getSubject() != null) {
                        dto.setSubjectId(t.getSubject().getId());
                        dto.setSubjectName(t.getSubject().getSubjectName());
                    }
                    if (t.getTeacher() != null) {
                        dto.setTeacherId(t.getTeacher().getId());
                        dto.setTeacherName(t.getTeacher().getName());
                        dto.setTeacherContactNumber(t.getTeacher().getContactNumber());
                    }
                    if (t.getDay() != null) {
                        dto.setDayOfWeek(t.getDay().name());
                    }
                    
                    return dto;
                })
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
            // Return empty list instead of throwing exception - normal for new sessions
            if (timetables.isEmpty())
            {
                log.info("No timetable found for class ID {} on {} in school {}. Returning empty list.", classId, day, schoolId);
                return new ArrayList<>();
            }
        }
        else
        {
            timetables = timetableRepository.findByClassEntity_IdAndSession_ActiveAndSchool_Id(classId, true, schoolId);
            // Return empty list instead of throwing exception - normal for new sessions
            if (timetables.isEmpty())
            {
                log.info("No timetable found for class ID {} in active session for school {}. Returning empty list.", classId, schoolId);
                return new ArrayList<>();
            }
        }

        return timetables.stream()
                .map(t -> {
                    TimetableResponseDTO dto = modelMapper.map(t, TimetableResponseDTO.class);
                    // Manually set fields for better data completeness
                    if (t.getClassEntity() != null) {
                        dto.setClassId(t.getClassEntity().getId());
                        dto.setClassName(t.getClassEntity().getClassName());
                    }
                    if (t.getSubject() != null) {
                        dto.setSubjectId(t.getSubject().getId());
                        dto.setSubjectName(t.getSubject().getSubjectName());
                    }
                    if (t.getTeacher() != null) {
                        dto.setTeacherId(t.getTeacher().getId());
                        dto.setTeacherName(t.getTeacher().getName());
                        dto.setTeacherContactNumber(t.getTeacher().getContactNumber());
                    }
                    if (t.getDay() != null) {
                        dto.setDayOfWeek(t.getDay().name());
                    }
                    return dto;
                })
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

        // Get teacher from request - teacher is now optional in subjects
        if (dto.getTeacherId() == null) {
            throw new WrongArgumentException("Teacher ID is required for timetable assignment.");
        }
        Teacher teacher = teacherRepository.findById(dto.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + dto.getTeacherId()));

        validateNoOverlapExcludingCurrent(dto, id, schoolId);

        existing.setSubject(subject);
        existing.setTeacher(teacher);
        existing.setDay(dto.getDay());
        existing.setPeriod(dto.getPeriod() != null ? dto.getPeriod() : existing.getPeriod());
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
        // Only check for conflicts in the active session, not across all sessions
        List<TimeTable> existingSlots = timetableRepository.findByTeacherIdAndDayAndSchoolIdAndActiveSession(teacherId, dto.getDay(), schoolId);
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
        timetable.setPeriod(dto.getPeriod() != null ? dto.getPeriod() : 1); // Default to period 1 if not provided
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
        dto.setDayOfWeek(timetable.getDay() != null ? timetable.getDay().name() : "MONDAY"); // Set string representation
        
        // Parse className (e.g., "1-A") into className ("1") and section ("A")
        String fullClassName = classEntity.getClassName();
        if (fullClassName != null && fullClassName.contains("-")) {
            String[] parts = fullClassName.split("-");
            dto.setClassName(parts[0]); // e.g., "1", "10"
            dto.setSection(parts.length > 1 ? parts[1] : "A"); // e.g., "A", "B"
        } else {
            // If no section in className, use fullClassName as is
            dto.setClassName(fullClassName != null ? fullClassName : "N/A");
            dto.setSection("A"); // Default section
        }
        
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
