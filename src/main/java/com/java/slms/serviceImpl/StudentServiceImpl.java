package com.java.slms.serviceImpl;

import com.java.slms.dto.*;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.exception.AlreadyExistException;
import com.java.slms.exception.WrongArgumentException;
import com.java.slms.model.*;
import com.java.slms.repository.*;
import com.java.slms.service.FeeService;
import com.java.slms.service.StudentService;
import com.java.slms.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class StudentServiceImpl implements StudentService
{

    private final ModelMapper modelMapper;
    private final StudentRepository studentRepository;
    private final ClassEntityRepository classEntityRepository;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final FeeStructureRepository feeStructureRepository;
    private final FeeRepository feeRepository;
    private final FeeService feeService;

    @Override
    public StudentResponseDto createStudent(StudentRequestDto studentRequestDto)
    {
        log.info("Attempting to create student with PAN: {}", studentRequestDto.getPanNumber());

        if (studentRepository.existsById(studentRequestDto.getPanNumber()))
        {
            throw new AlreadyExistException("Student already exists with PAN: " + studentRequestDto.getPanNumber());
        }

        User user = userRepository.findByPanNumberIgnoreCase(studentRequestDto.getPanNumber())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student User not found with PAN: " + studentRequestDto.getPanNumber()));

        ClassEntity classEntity = EntityFetcher.fetchByIdOrThrow(
                classEntityRepository,
                studentRequestDto.getClassId(),
                EntityNames.CLASS_ENTITY);

        Session session = EntityFetcher.fetchByIdOrThrow(
                sessionRepository,
                studentRequestDto.getSessionId(),
                EntityNames.SESSION);


        // Check if another class with same name and session already exists
        Optional<ClassEntity> duplicateClass = classEntityRepository
                .findByClassNameIgnoreCaseAndSessionId(classEntity.getClassName(), studentRequestDto.getSessionId());

        if (duplicateClass.isPresent() && !duplicateClass.get().getId().equals(classEntity.getId()))
        {
            throw new AlreadyExistException("Class already exists with name: " + classEntity.getClassName() +
                    " for the selected session.");
        }

        if (!session.isActive())
        {
            log.warn("Cannot add student to inactive session with ID {}", session.getId());
            throw new WrongArgumentException("Cannot add student to an inactive session");
        }

        Student student = modelMapper.map(studentRequestDto, Student.class);
        student.setStatus(UserStatus.ACTIVE);

        Integer maxRoll = studentRepository.findMaxClassRollNumberByCurrentClassId(studentRequestDto.getClassId());
        student.setClassRollNumber((maxRoll != null ? maxRoll : 0) + 1);

        student.setCurrentClass(classEntity);
        student.setUser(user);
        student.setSession(session);

        Student savedStudent = studentRepository.save(student);

        FeeStructure feeStructure = feeStructureRepository.findByClassEntity_IdAndSession_Id(studentRequestDto.getClassId(), studentRequestDto.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Fee structure not found for class and session"));

        List<Fee> feeEntries = new ArrayList<>();
        LocalDate currentMonth = session.getStartDate().withDayOfMonth(1);

        for (int i = 0; i < 12; i++)
        {
            Fee fee = new Fee();
            fee.setMonth(FeeMonth.valueOf(currentMonth.getMonth().toString()));
            fee.setYear(currentMonth.getYear());
            fee.setStatus(FeeStatus.PENDING);
            fee.setAmount(feeStructure.getFeesAmount());
            fee.setFeeStructure(feeStructure);
            fee.setDueDate(currentMonth.withDayOfMonth(15));
            fee.setStudent(savedStudent);
            feeEntries.add(fee);
            currentMonth = currentMonth.plusMonths(1);
        }
        feeRepository.saveAll(feeEntries);

        log.info("Student created successfully with PAN: {}", studentRequestDto.getPanNumber());
        StudentResponseDto studentResponseDto = convertToDto(savedStudent);
        studentResponseDto.setFeeCatalogStatus(FeeCatalogStatus.PENDING);
        studentResponseDto.setFeeStatus(FeeStatus.PENDING);
        return studentResponseDto;
    }

    @Transactional
    @Override
    public void markStudentsGraduateOrInActive(List<String> panNumbers, UserStatus status)
    {
        List<Student> students = new ArrayList<>();
        List<User> users = new ArrayList<>();

        for (String panNumber : panNumbers)
        {
            Student student = EntityFetcher.fetchByIdOrThrow(studentRepository, panNumber, EntityNames.STUDENT);

            User user = student.getUser();
            if (user != null)
            {
                user.setEnabled(false);
                users.add(user);
            }

            student.setStatus(status);
            students.add(student);
        }

        userRepository.saveAll(users);
        studentRepository.saveAll(students);
    }

    @Override
    public List<StudentResponseDto> getStudentsByClassId(Long classId)
    {
        log.info("Fetching students by class ID: {}", classId);

        ClassEntity classEntity = classEntityRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with ID: " + classId));

        if (!classEntity.getSession().isActive())
        {
            throw new WrongArgumentException("Current Class is already Inactive");
        }

        List<Student> activeStudents = studentRepository.findByStatusAndCurrentClass_Id(UserStatus.ACTIVE, classId);

        return activeStudents.stream()
                .map(student ->
                {
                    StudentResponseDto dto = convertToDto(student);
                    setFeeStatuses(dto, student.getPanNumber());
                    return dto;
                })
                .toList();
    }

    @Override
    public List<StudentResponseDto> getAllStudent()
    {
        log.info("Fetching all students");
        return studentRepository.findAll().stream()
                .map(student ->
                {
                    StudentResponseDto dto = convertToDto(student);
                    setFeeStatuses(dto, student.getPanNumber());
                    return dto;
                })
                .toList();
    }

    @Override
    public List<StudentResponseDto> getActiveStudents()
    {
        log.info("Fetching all active students");
        List<Student> activeStudents = studentRepository.findByStatus(UserStatus.ACTIVE);

        return activeStudents.stream()
                .map(student ->
                {
                    StudentResponseDto dto = modelMapper.map(student, StudentResponseDto.class);
                    if (student.getSession() != null)
                    {
                        dto.setSessionId(student.getSession().getId());
                        dto.setSessionName(student.getSession().getName());
                    }
                    setFeeStatuses(dto, student.getPanNumber());
                    return dto;
                })
                .toList();
    }

    @Override
    public StudentResponseDto getStudentByPAN(String pan)
    {
        log.info("Fetching student with PAN: {}", pan);
        Student fetchedStudent = EntityFetcher.fetchStudentByPan(studentRepository, pan);

        StudentResponseDto studentResponseDto = convertToDto(fetchedStudent);
        setFeeStatuses(studentResponseDto, pan);
        log.info("Student fetched successfully with PAN: {}", pan);

        return studentResponseDto;
    }

    @Override
    public StudentResponseDto updateStudent(String pan, UpdateStudentInfo updateStudentInfo) //Only Active students can be change
    {
        log.info("Updating student with PAN: {}", pan);

        Student fetchedStudent = EntityFetcher.fetchStudentByPan(studentRepository, pan);
        if (!fetchedStudent.getStatus().equals(UserStatus.ACTIVE))
        {
            log.info("Update not allowed. Student with PAN {} has status: {}",
                    fetchedStudent.getPanNumber(), fetchedStudent.getStatus());
            throw new WrongArgumentException(
                    "Only active students can be changed. Student with PAN " +
                            fetchedStudent.getPanNumber() + " is currently " +
                            fetchedStudent.getStatus().name().toLowerCase() + ".");
        }

        if (!fetchedStudent.getSession().isActive())
        {
            throw new WrongArgumentException("Cannot update student because the session is inactive");
        }

        modelMapper.map(updateStudentInfo, fetchedStudent);
        fetchedStudent.setPanNumber(pan);

        Student updatedStudent = studentRepository.save(fetchedStudent);
        log.info("Student updated successfully with PAN: {}", pan);

        StudentResponseDto studentResponseDto = convertToDto(updatedStudent);
        setFeeStatuses(studentResponseDto, pan);
        return studentResponseDto;
    }

    @Override
    public List<StudentAttendance> getStudentsPresentToday()
    {
        log.info("Fetching today's present students");
        return studentRepository.findStudentsPresentToday().stream()
                .map(student -> modelMapper.map(student, StudentAttendance.class))
                .toList();
    }

    @Override
    public List<StudentAttendance> getStudentsPresentTodayByClass(Long classId)
    {
        log.info("Fetching today's present students for class ID: {}", classId);
        EntityFetcher.fetchClassEntityByClassId(classEntityRepository, classId);

        return studentRepository.findStudentsPresentTodayByClassName(classId).stream()
                .map(student -> modelMapper.map(student, StudentAttendance.class))
                .toList();
    }

    private StudentResponseDto convertToDto(Student student)
    {
        StudentResponseDto dto = modelMapper.map(student, StudentResponseDto.class);
        dto.setClassId(student.getCurrentClass().getId());
        dto.setSessionId(student.getSession().getId());
        dto.setSessionName(student.getSession().getName());
        return dto;
    }

    private void setFeeStatuses(StudentResponseDto dto, String pan)
    {
        FeeCatalogDto feeCatalog = feeService.getFeeCatalogByStudentPanNumber(pan);

        LocalDate today = LocalDate.now();
        Month currentMonth = today.getMonth();
        int currentYear = today.getYear();

        Month previousMonth = currentMonth.minus(1);
        int previousYear = currentMonth == Month.JANUARY ? currentYear - 1 : currentYear;

        Optional<MonthlyFeeDto> currentMonthFeeOpt = feeCatalog.getMonthlyFees().stream()
                .filter(fee -> fee.getYear() == currentYear && fee.getMonth().equalsIgnoreCase(currentMonth.name()))
                .findFirst();

        Optional<MonthlyFeeDto> previousMonthFeeOpt = feeCatalog.getMonthlyFees().stream()
                .filter(fee -> fee.getYear() == previousYear && fee.getMonth().equalsIgnoreCase(previousMonth.name()))
                .findFirst();

        if (currentMonthFeeOpt.isPresent() && "paid".equalsIgnoreCase(currentMonthFeeOpt.get().getStatus()))
        {
            // Current month paid
            dto.setFeeStatus(FeeStatus.PAID);
            dto.setFeeCatalogStatus(FeeCatalogStatus.UP_TO_DATE);
            return;
        }

        if (today.getDayOfMonth() <= 15)
        {
            // On or before 15th and current month fee not paid
            if (previousMonthFeeOpt.isPresent()
                    && "paid".equalsIgnoreCase(previousMonthFeeOpt.get().getStatus()))
            {
                dto.setFeeStatus(FeeStatus.PENDING);
                dto.setFeeCatalogStatus(FeeCatalogStatus.PENDING);
                return;
            }
            // If no previous month or not paid, treat as pending as well
            dto.setFeeStatus(FeeStatus.PENDING);
            dto.setFeeCatalogStatus(FeeCatalogStatus.PENDING);
            return;
        }
        else
        {
            // After 15th
            if (currentMonthFeeOpt.isEmpty())
            {
                // No current month fee submitted → OVERDUE
                dto.setFeeStatus(FeeStatus.OVERDUE);
                dto.setFeeCatalogStatus(FeeCatalogStatus.OVERDUE);
                return;
            }

            // Check if previous month is overdue
            if (previousMonthFeeOpt.isPresent()
                    && "overdue".equalsIgnoreCase(previousMonthFeeOpt.get().getStatus()))
            {
                dto.setFeeStatus(FeeStatus.OVERDUE);
                dto.setFeeCatalogStatus(FeeCatalogStatus.OVERDUE);
                return;
            }

            // Otherwise default to pending if current month fee exists but not paid
            if (currentMonthFeeOpt.isPresent() &&
                    !"paid".equalsIgnoreCase(currentMonthFeeOpt.get().getStatus()))
            {
                dto.setFeeStatus(FeeStatus.PENDING);
                dto.setFeeCatalogStatus(FeeCatalogStatus.PENDING);
                return;
            }
        }

        // Default fallback
        dto.setFeeStatus(FeeStatus.PENDING);
        dto.setFeeCatalogStatus(FeeCatalogStatus.PENDING);
    }


}
