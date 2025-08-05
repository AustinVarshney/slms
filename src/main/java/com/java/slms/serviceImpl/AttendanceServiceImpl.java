package com.java.slms.serviceImpl;

import com.java.slms.dto.StudentDto;
import com.java.slms.exception.AlreadyExistException;
import com.java.slms.model.Attendance;
import com.java.slms.model.Student;
import com.java.slms.repository.AttendanceRepository;
import com.java.slms.service.AttendanceService;
import com.java.slms.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class AttendanceServiceImpl implements AttendanceService
{
    private final StudentService studentService;
    private final ModelMapper modelMapper;
    private final AttendanceRepository attendanceRepository;

    @Override
    public void markAttendance(String studentPanNumber, boolean isPresent)
    {
        StudentDto studentDto = studentService.getStudentByPAN(studentPanNumber);
        // Check if attendance for today already exists for this student
        LocalDateTime today = LocalDateTime.now().toLocalDate().atStartOfDay();  // Start of the current day (midnight)

        Student student = new Student();
        student.setPanNumber(studentDto.getPanNumber());

        // Find an existing attendance for this student today
        Attendance existingAttendance = attendanceRepository.findByStudentAndDateBetween(
                modelMapper.map(studentDto, Student.class),
                today,
                today.plusDays(1));

        if (existingAttendance != null)
        {
            log.error("Attendance already marked for today for PAN: {}", studentPanNumber);
            throw new AlreadyExistException("Attendance already marked for today for PAN: " + studentPanNumber);
        }

        Attendance attendance = new Attendance();
        attendance.setDate(LocalDateTime.now());
        attendance.setPresent(isPresent);
        attendance.setStudent(modelMapper.map(studentDto, Student.class));
        attendanceRepository.save(attendance);
    }
}
