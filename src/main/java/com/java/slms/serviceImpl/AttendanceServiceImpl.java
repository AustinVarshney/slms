package com.java.slms.serviceImpl;

import com.java.slms.dto.AttendanceDto;
import com.java.slms.dto.StudentAttendance;
import com.java.slms.dto.StudentDto;
import com.java.slms.exception.AlreadyExistException;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.model.Attendance;
import com.java.slms.model.Student;
import com.java.slms.repository.AttendanceRepository;
import com.java.slms.service.AttendanceService;
import com.java.slms.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class AttendanceServiceImpl implements AttendanceService
{
    private final StudentService studentService;
    private final ModelMapper modelMapper;
    private final AttendanceRepository attendanceRepository;

    @Override
    public AttendanceDto markAttendance(AttendanceDto attendanceDto)
    {
        LocalDate todayStart = LocalDate.now();
        LocalDateTime dayStart = todayStart.atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);

        List<StudentAttendance> inputAttendances = attendanceDto.getStudentAttendances();
        if (inputAttendances == null || inputAttendances.isEmpty())
        {
            throw new IllegalArgumentException("No student attendances provided");
        }

        List<StudentAttendance> savedAttendanceList = new ArrayList<>();

        for (StudentAttendance sa : inputAttendances)
        {
            String panNumber = sa.getPanNumber();

            // Fetch student info
            StudentDto studentDto = studentService.getStudentByPAN(panNumber);
            if (studentDto == null)
            {
                log.error("Student with PAN '{}' not found", panNumber);
                throw new ResourceNotFoundException("Student with PAN '" + panNumber + "' not found");
            }

            // Map to entity
            Student student = modelMapper.map(studentDto, Student.class);

            // Check if attendance for today already exists for this student
            Optional<Attendance> existingAttendanceOpt = attendanceRepository.findByStudentAndDateBetween(student, dayStart, dayEnd);

            if (existingAttendanceOpt.isPresent())
            {
                log.error("Attendance already marked for today for PAN: {}", panNumber);
                throw new AlreadyExistException("Attendance already marked for today for PAN: " + panNumber);
            }

            // Create and save attendance
            Attendance attendance = new Attendance();
            attendance.setDate(LocalDateTime.now());
            attendance.setStudent(student);
            attendance.setPresent(sa.isPresent());

            Attendance saved = attendanceRepository.save(attendance);

            // Add to response list
            StudentAttendance savedSA = new StudentAttendance();
            savedSA.setPanNumber(panNumber);
            savedSA.setPresent(saved.isPresent());
            savedAttendanceList.add(savedSA);
        }

        // Prepare and return updated AttendanceDto
        AttendanceDto responseDto = new AttendanceDto();
        responseDto.setDate(LocalDateTime.now());
        responseDto.setStudentAttendances(savedAttendanceList);

        return responseDto;
    }

    @Override
    public AttendanceDto updateAttendanceForAdmin(AttendanceDto attendanceDto, LocalDate attendanceDate)
    {
        // Validate attendanceDate is within last 5 days including today
        LocalDate today = LocalDate.now();
        LocalDate earliestAllowedDate = today.minusDays(4); // 5 days window: today + previous 4 days

        if (attendanceDate.isBefore(earliestAllowedDate) || attendanceDate.isAfter(today))
        {
            throw new IllegalArgumentException("Attendance date must be within the last five days including today.");
        }

        List<StudentAttendance> inputAttendances = attendanceDto.getStudentAttendances();
        if (inputAttendances == null || inputAttendances.isEmpty())
        {
            throw new IllegalArgumentException("No student attendance records provided.");
        }

        LocalDateTime startDateTime = attendanceDate.atStartOfDay();
        LocalDateTime endDateTime = startDateTime.plusDays(1);

        List<StudentAttendance> updatedAttendances = new ArrayList<>();

        for (StudentAttendance sa : inputAttendances)
        {
            String panNumber = sa.getPanNumber();

            // Fetch student info
            StudentDto studentDto = studentService.getStudentByPAN(panNumber);
            if (studentDto == null)
            {
                log.error("Student with PAN '{}' not found", panNumber);
                throw new ResourceNotFoundException("Student with PAN '" + panNumber + "' not found");
            }
            Student student = modelMapper.map(studentDto, Student.class);

            // Find attendance for this student at the specific date
            Optional<Attendance> existingAttendanceOpt = attendanceRepository.findByStudentAndDateBetween(
                    student, startDateTime, endDateTime);

            if (existingAttendanceOpt.isPresent())
            {
                Attendance attendance = existingAttendanceOpt.get();
                // Update attendance present status
                attendance.setPresent(sa.isPresent());
                attendanceRepository.save(attendance);
            }
            else
            {
                // Optionally: create if absent or throw exception
                // Here, create a new attendance record if none exists
                Attendance attendance = new Attendance();
                attendance.setStudent(student);
                attendance.setDate(attendanceDate.atStartOfDay()); // or LocalDateTime.now(), or datetime precision you want
                attendance.setPresent(sa.isPresent());
                attendanceRepository.save(attendance);
            }

            updatedAttendances.add(sa);
        }

        // Return DTO summarizing the updated data
        AttendanceDto responseDto = new AttendanceDto();
        responseDto.setDate(attendanceDate.atStartOfDay());
        responseDto.setStudentAttendances(updatedAttendances);

        return responseDto;
    }


}
