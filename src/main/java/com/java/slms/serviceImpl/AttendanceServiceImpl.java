package com.java.slms.serviceImpl;

import com.java.slms.dto.*;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.exception.WrongArgumentException;
import com.java.slms.model.Attendance;
import com.java.slms.model.ClassEntity;
import com.java.slms.model.Session;
import com.java.slms.model.Student;
import com.java.slms.model.School;
import com.java.slms.repository.AttendanceRepository;
import com.java.slms.repository.ClassEntityRepository;
import com.java.slms.repository.SessionRepository;
import com.java.slms.repository.StudentRepository;
import com.java.slms.repository.SchoolRepository;
import com.java.slms.service.AttendanceService;
import com.java.slms.service.StudentService;
import com.java.slms.util.FeeMonth;
import com.java.slms.util.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class AttendanceServiceImpl implements AttendanceService
{
    private final StudentService studentService;
    private final ModelMapper modelMapper;
    private final AttendanceRepository attendanceRepository;
    private final SessionRepository sessionRepository;
    private final ClassEntityRepository classEntityRepository;
    private final StudentRepository studentRepository;
    private final SchoolRepository schoolRepository;

    @Override
    @Transactional
    public void markTodaysAttendance(AttendanceDto attendanceDto, Long schoolId)
    {
        LocalDate today = LocalDate.now();
        LocalDateTime dayStart = today.atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);

        List<StudentAttendance> inputAttendances = attendanceDto.getStudentAttendances();
        if (inputAttendances == null || inputAttendances.isEmpty())
        {
            throw new WrongArgumentException("No student attendances provided");
        }

        Long classId = attendanceDto.getClassId();
        if (classId == null)
        {
            throw new WrongArgumentException("Class ID must be provided");
        }

        Optional<ClassEntity> classBelongsToSession = classEntityRepository.findByIdAndSchoolIdAndSessionActive(classId, schoolId);
        if (classBelongsToSession.isEmpty())
        {
            throw new WrongArgumentException("Class ID " + classId + " does not belong to the active session");
        }

        ClassEntity classEntity = classBelongsToSession.get();

        // Fetch all relevant students once to avoid multiple DB calls
        Set<String> panNumbers = inputAttendances.stream()
                .map(StudentAttendance::getPanNumber)
                .collect(Collectors.toSet());

        List<Student> students = studentService.getStudentsBySchoolIdAndPanNumbers(schoolId, new ArrayList<>(panNumbers));
        Session activeSession = sessionRepository.findBySchoolIdAndActiveTrue(schoolId).orElseThrow(() -> new ResourceNotFoundException("No active session found"));
        School school = schoolRepository.findById(schoolId).orElseThrow(() -> new ResourceNotFoundException("School not found"));

        Map<String, Student> studentMap = students.stream()
                .collect(Collectors.toMap(Student::getPanNumber, s -> s));

        for (StudentAttendance sa : inputAttendances)
        {
            String panNumber = sa.getPanNumber();
            Student student = studentMap.get(panNumber);

            if (student == null)
            {
                log.error("Student with PAN '{}' not found", panNumber);
                throw new ResourceNotFoundException("Student with PAN '" + panNumber + "' not found");
            }

            if (student.getStatus() == UserStatus.INACTIVE || student.getStatus() == UserStatus.GRADUATED)
            {
                log.error("Student with PAN '{}' has status: {}", panNumber, student.getStatus());
                throw new WrongArgumentException("Student with PAN '" + panNumber + "' has status '" + student.getStatus() + "' and cannot be marked for attendance");
            }

            if (!student.getSession().getId().equals(activeSession.getId()))
            {
                log.error("Student with PAN '{}' does not belong to the active session", panNumber);
                throw new WrongArgumentException("Student with PAN '" + panNumber + "' does not belong to the active session");
            }

            if (!student.getCurrentClass().getId().equals(classId))
            {
                log.error("Student with PAN '{}' does not belong to class ID {}", panNumber, classId);
                throw new WrongArgumentException("Student with PAN '" + panNumber + "' does not belong to class ID " + classId);
            }

            Optional<Attendance> existingAttendanceOpt = attendanceRepository.findByStudentAndDateBetweenAndSchoolId(student, dayStart, dayEnd, schoolId);

            if (existingAttendanceOpt.isPresent())
            {
                log.error("Attendance already marked for today for PAN: {}", panNumber);
                continue;  // Or collect to return info about skipped entries
            }

            Attendance attendance = new Attendance();
            attendance.setDate(LocalDateTime.now());
            attendance.setStudent(student);
            attendance.setPresent(sa.isPresent());
            attendance.setSession(activeSession);
            attendance.setSchool(school);
            attendance.setClassEntity(classEntity);

            attendanceRepository.save(attendance);
            log.info("Marked attendance for student PAN '{}' as present={}", panNumber, sa.isPresent());
        }
    }

    @Override
    @Transactional
    public AttendanceUpdateResult updateAttendanceForAdmin(AttendanceDto attendanceDto, LocalDate attendanceDate, Long schoolId)
    {
        AttendanceUpdateResult result = new AttendanceUpdateResult();

        Session activeSession = sessionRepository.findBySchoolIdAndActiveTrue(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("No active session found"));

        if (attendanceDate.isBefore(activeSession.getStartDate()) || attendanceDate.isAfter(activeSession.getEndDate()))
        {
            throw new WrongArgumentException("Attendance date " + attendanceDate + " is outside the active session period.");
        }

        if (attendanceDto.getStudentAttendances() == null || attendanceDto.getStudentAttendances().isEmpty())
        {
            throw new WrongArgumentException("Student attendances must be provided");
        }

        for (StudentAttendance sa : attendanceDto.getStudentAttendances())
        {
            String panNumber = sa.getPanNumber();

            Optional<Student> fetchedStudent = studentRepository.findByPanNumberIgnoreCaseAndSchool_IdAndStatusActive(panNumber, schoolId);

            if (fetchedStudent.isEmpty())
            {
                log.warn("Active Student with PAN '{}' not found. Skipping attendance update.", panNumber);
                result.getInvalidPanNumbers().add(panNumber);
                continue;
            }

            Student student = fetchedStudent.get();

            LocalDateTime dayStart = attendanceDate.atStartOfDay();
            LocalDateTime dayEnd = attendanceDate.plusDays(1).atStartOfDay();

            Optional<Attendance> attendanceOpt = attendanceRepository.findByStudentAndDateBetweenAndSchoolId(student, dayStart, dayEnd, schoolId);

            if (attendanceOpt.isEmpty())
            {
                log.warn("Attendance record not found for student PAN '{}' on date {}. Skipping.", panNumber, attendanceDate);
                result.getInvalidPanNumbers().add(panNumber);
                continue;
            }

            Attendance attendance = attendanceOpt.get();

            if (attendance.isPresent() == sa.isPresent())
            {
                log.info("Attendance for student PAN '{}' on {} already marked as present={}, skipping update.",
                        panNumber, attendanceDate, attendance.isPresent());
                result.getUnchangedPanNumbers().add(panNumber);
                continue;
            }

            attendance.setPresent(sa.isPresent());
            attendance.setUpdatedAt(new Date());
            attendanceRepository.save(attendance);

            result.getUpdatedPanNumbers().add(panNumber);
            log.info("Updated attendance for student PAN '{}' on {} to present={}.", panNumber, attendanceDate, sa.isPresent());
        }

        return result;
    }

    @Override
    public List<AttendanceInfoDto> getAllAttendanceByPanAndSessionId(String panNumber, Long sessionId, FeeMonth month, Long schoolId)
    {
        // Fetch session for date range filtering
        Session session = sessionRepository.findBySessionIdAndSchoolId(sessionId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + sessionId));

        LocalDateTime sessionStart = session.getStartDate().atStartOfDay();
        LocalDateTime sessionEnd = session.getEndDate().atTime(LocalTime.MAX);

        List<Attendance> attendances;

        Integer monthNumber = (month != null) ? month.ordinal() + 1 : null;

        if (monthNumber != null)
        {
            // Repository method with date range and month filter (you'll need to implement this)
            attendances = attendanceRepository.findByPanNumberAndSessionIdAndMonthWithinSessionAndSchoolId(
                    panNumber, sessionId, sessionStart, sessionEnd, monthNumber, schoolId);
        }
        else
        {
            // Repository method with date range only
            attendances = attendanceRepository.findByPanNumberAndSessionIdWithinSessionAndSchoolId(
                    panNumber, sessionId, sessionStart, sessionEnd, schoolId);
        }

        if (attendances.isEmpty())
        {
            return Collections.emptyList();
        }

        Map<Long, List<Attendance>> attendancesByClass = attendances.stream()
                .filter(att -> att.getStudent().getCurrentClass() != null) // Defensive null check
                .collect(Collectors.groupingBy(att -> att.getStudent().getCurrentClass().getId()));

        List<AttendanceInfoDto> result = new ArrayList<>();

        for (Map.Entry<Long, List<Attendance>> entry : attendancesByClass.entrySet())
        {
            Long classId = entry.getKey();
            List<Attendance> classAttendances = entry.getValue();

            AttendanceInfoDto dto = new AttendanceInfoDto();

            Student student = classAttendances.get(0).getStudent();
            Session sess = classAttendances.get(0).getSession();

            dto.setPanNumber(student.getPanNumber());
            dto.setStudentName(student.getName());
            dto.setSessionId(sess.getId());
            dto.setSessionName(sess.getName());
            dto.setClassId(classId);
            dto.setClassName(student.getCurrentClass().getClassName());

            if (month != null)
            {
                dto.setMonth(month);
            }
            else
            {
                int monthValue = classAttendances.get(0).getDate().getMonthValue();
                dto.setMonth(FeeMonth.values()[monthValue - 1]);
            }


            List<AttendenceResponse> attendanceResponses = classAttendances.stream()
                    .map(att ->
                    {
                        AttendenceResponse response = modelMapper.map(att, AttendenceResponse.class);
                        response.setCreatedAt(att.getCreatedAt());
                        response.setUpdatedAt(att.getUpdatedAt());
                        return response;
                    })
                    .toList();

            dto.setAttendances(attendanceResponses);

            result.add(dto);
        }

        return result;
    }

    @Override
    public List<AttendanceByClassDto> getAttendanceByClassAndSession(Long classId, Long sessionId, FeeMonth month, Long schoolId)
    {
        Session session = sessionRepository.findBySessionIdAndSchoolId(sessionId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + sessionId));

        LocalDateTime sessionStart = session.getStartDate().atStartOfDay();
        LocalDateTime sessionEnd = session.getEndDate().atTime(LocalTime.MAX);

        Integer monthNumber = (month != null) ? month.ordinal() + 1 : null;

        List<Attendance> attendances = (monthNumber != null) ?
                attendanceRepository.findByClassIdAndSessionIdAndMonthWithinSessionAndSchoolId(
                        classId, sessionId, sessionStart, sessionEnd, monthNumber, schoolId) :
                attendanceRepository.findByClassIdAndSessionIdWithinSessionAndSchoolId(
                        classId, sessionId, sessionStart, sessionEnd, schoolId);

        if (attendances.isEmpty())
        {
            return Collections.emptyList();
        }

        Map<Integer, List<Attendance>> attendancesByMonth = attendances.stream()
                .collect(Collectors.groupingBy(att -> att.getDate().getMonthValue()));

        List<AttendanceByClassDto> result = new ArrayList<>();

        for (Map.Entry<Integer, List<Attendance>> entry : attendancesByMonth.entrySet())
        {
            Integer monthVal = entry.getKey();
            List<Attendance> monthAttendances = entry.getValue();

            AttendanceByClassDto dto = new AttendanceByClassDto();

            Attendance firstAttendance = monthAttendances.get(0);

            dto.setId(firstAttendance.getId());
            dto.setClassId(classId);
            dto.setClassName(firstAttendance.getStudent().getCurrentClass().getClassName());
            dto.setSessionId(sessionId);
            dto.setSessionName(session.getName());
            dto.setMonth(FeeMonth.values()[monthVal - 1]);

            List<StudentAttendance> studentAttendances = monthAttendances.stream()
                    .map(att ->
                    {
                        StudentAttendance sa = new StudentAttendance();
                        sa.setPanNumber(att.getStudent().getPanNumber());
                        sa.setPresent(att.isPresent());
                        return sa;
                    })
                    .toList();

            dto.setStudentAttendances(studentAttendances);

            result.add(dto);
        }

        return result;
    }


}
