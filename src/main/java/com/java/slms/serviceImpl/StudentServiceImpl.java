package com.java.slms.serviceImpl;

import com.java.slms.dto.*;
import com.java.slms.exception.AlreadyExistException;
import com.java.slms.exception.ResourceNotFoundException;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private final SchoolRepository schoolRepository;
    private final StudentEnrollmentRepository studentEnrollmentRepository;

    @Override
    public StudentResponseDto createStudent(StudentRequestDto studentRequestDto, Long schoolId)
    {
        log.info("Attempting to create student with PAN: {}", studentRequestDto.getPanNumber());

        School school = schoolRepository.findById(schoolId).orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + schoolId));
        if (studentRepository.findByPanNumberIgnoreCase(studentRequestDto.getPanNumber()).isPresent())
        {
            throw new AlreadyExistException("A student with PAN number '" + studentRequestDto.getPanNumber() + "' already exists.");
        }
        User user = fetchUserByPan(studentRequestDto.getPanNumber());
        ClassEntity classEntity = classEntityRepository
                .findByIdAndSchoolIdAndSessionActive(studentRequestDto.getClassId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Class with ID " + studentRequestDto.getClassId() +
                        " not found in School with ID " + schoolId + " or the session is not active."));

        Session session = classEntity.getSession();

        // Check duplicate class for same name and session
        Optional<ClassEntity> duplicateClass = classEntityRepository.findByClassNameIgnoreCaseAndSessionIdAndSchoolId(classEntity.getClassName(), session.getId(), schoolId);

        if (duplicateClass.isPresent() && !duplicateClass.get().getId().equals(classEntity.getId()))
        {
            throw new AlreadyExistException("Class already exists with name: " + classEntity.getClassName() + " for the selected session.");
        }
        Student student = modelMapper.map(studentRequestDto, Student.class);
        student.setStatus(UserStatus.ACTIVE);
        student.setClassRollNumber(getNextRollNumber(studentRequestDto.getClassId(), schoolId));
        student.setCurrentClass(classEntity);
        student.setUser(user);
        student.setSchool(school);
        student.setSession(session);

        Student savedStudent = studentRepository.save(student);

        FeeStructure feeStructure = feeStructureRepository.findByClassEntity_IdAndSession_IdAndSchool_Id(studentRequestDto.getClassId(), session.getId(), schoolId).orElseThrow(() -> new ResourceNotFoundException("Fee structure not found for class and session"));

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
            fee.setStudent(student);
            fee.setSchool(school);

            feeEntries.add(fee);
            currentMonth = currentMonth.plusMonths(1);
        }

        feeRepository.saveAll(feeEntries);

        StudentEnrollments studentEnrollments = new StudentEnrollments();
        studentEnrollments.setStudent(savedStudent);
        studentEnrollments.setSchool(school);
        studentEnrollments.setClassEntity(classEntity);
        studentEnrollments.setSession(session);
        studentEnrollmentRepository.save(studentEnrollments);

        log.info("Student created successfully with PAN: {}", studentRequestDto.getPanNumber());

        StudentResponseDto responseDto = convertToDto(savedStudent);
        responseDto.setFeeCatalogStatus(FeeCatalogStatus.PENDING);
        responseDto.setFeeStatus(FeeStatus.PENDING);

        return responseDto;
    }

    @Transactional
    @Override
    public void markStudentsGraduateOrInActive(List<String> panNumbers, UserStatus status, Long schoolId)
    {
        List<Student> students = new ArrayList<>();
//        List<User> users = new ArrayList<>();

        for (String pan : panNumbers)
        {
            Student student = studentRepository.findByPanNumberIgnoreCaseAndSchool_IdAndStatusActive(pan, schoolId)
                    .orElseThrow(() -> new RuntimeException("Student not found with pan: " + pan));

//            User user = student.getUser();
//
//            if (user != null)
//            {
//                user.setEnabled(false);
//                users.add(user);
//            }

            student.setStatus(status);
            students.add(student);
        }

//        userRepository.saveAll(users);
        studentRepository.saveAll(students);
    }

    @Override
    public List<StudentResponseDto> getStudentsByClassId(Long classId, Long schoolId)
    {
        log.info("Fetching students by class ID: {}", classId);
        ClassEntity classEntity = classEntityRepository
                .findByIdAndSchoolIdAndSessionActive(classId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Class with ID " + classId +
                        " not found in School with ID " + schoolId + " or the session is not active."));

        List<Student> activeStudents = studentRepository.findByCurrentClassIdAndSchoolIdAndStatusActive(classId, schoolId);

        return activeStudents.stream().map(student ->
        {
            StudentResponseDto dto = convertToDto(student);
            setFeeStatuses(dto, student.getPanNumber(), schoolId);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<StudentResponseDto> getAllStudent(Long schoolId)
    {
        log.info("Fetching all students");
        return studentRepository.findAllBySchoolId(schoolId).stream().map(student ->
        {
            StudentResponseDto dto = convertToDto(student);
            setFeeStatuses(dto, student.getPanNumber(), schoolId);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<StudentResponseDto> getActiveStudents(Long schoolId)
    {
        log.info("Fetching all active students");
        List<Student> activeStudents = studentRepository.findAllActiveStudentsBySchoolId(schoolId);

        return activeStudents.stream().map(student ->
        {
            StudentResponseDto dto = modelMapper.map(student, StudentResponseDto.class);
            if (student.getSession() != null)
            {
                dto.setSessionId(student.getSession().getId());
                dto.setSessionName(student.getSession().getName());
            }
            setFeeStatuses(dto, student.getPanNumber(), schoolId);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public StudentResponseDto getStudentByPAN(String pan, Long schoolId)
    {
        log.info("Fetching student with PAN: {}", pan);
        Student fetchedStudent = EntityFetcher.fetchStudentByPan(studentRepository, pan);

        StudentResponseDto dto = convertToDto(fetchedStudent);
        setFeeStatuses(dto, pan, schoolId);

        log.info("Student fetched successfully with PAN: {}", pan);
        return dto;
    }

    @Override
    public Student getActiveStudentByPan(String pan, Long schoolId)
    {
        return studentRepository.findByPanNumberIgnoreCaseAndSchool_IdAndStatusActive(pan, schoolId)
                .orElseThrow(() -> new RuntimeException("Active Student not found with pan: " + pan));
    }


    @Override
    public List<Student> getStudentsBySchoolIdAndPanNumbers(Long schoolId, List<String> panNumbers)
    {
        List<String> lowerCasePanNumbers = panNumbers.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        return studentRepository.findBySchoolIdAndPanNumberInIgnoreCase(schoolId, lowerCasePanNumbers);
    }


    @Override
    public StudentResponseDto updateStudent(String pan, UpdateStudentInfo updateStudentInfo, Long schoolId)
    {
        log.info("Updating student with PAN: {}", pan);

        Student fetchedStudent = studentRepository.findByPanNumberIgnoreCaseAndSchool_IdAndStatusActive(pan, schoolId)
                .orElseThrow(() -> new WrongArgumentException("Only active students can be changed. Student with PAN " + pan + " is currently INACTIVE or GRADUATE"));

        if (!fetchedStudent.getSession().isActive())
        {
            throw new WrongArgumentException("Cannot update student because the session is inactive");
        }

        modelMapper.map(updateStudentInfo, fetchedStudent);
        fetchedStudent.setPanNumber(pan);

        Student updatedStudent = studentRepository.save(fetchedStudent);

        log.info("Student updated successfully with PAN: {}", pan);

        StudentResponseDto dto = convertToDto(updatedStudent);
        setFeeStatuses(dto, pan, schoolId);

        return dto;
    }

    @Override
    public CurrentDayAttendance getStudentsPresentToday(Optional<Long> classId, Long schoolId)
    {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        List<Student> presentStudents;
        CurrentDayAttendance result = new CurrentDayAttendance();

        if (classId.isPresent())
        {
            Long id = classId.get();
            ClassEntity classEntity = classEntityRepository.findByIdAndSchoolId(id, schoolId).orElseThrow(() -> new ResourceNotFoundException("Class not found with ID: " + id));

            if (!classEntity.getSession().isActive())
            {
                throw new ResourceNotFoundException("Session is not active for class ID: " + id);
            }

            presentStudents = studentRepository.findStudentsPresentTodayByClassId(id, schoolId, startOfDay, endOfDay);

            result.setId(classEntity.getId());
            result.setClassId(classEntity.getId());
            result.setClassName(classEntity.getClassName());
        }
        else
        {
            presentStudents = studentRepository.findStudentsPresentToday(schoolId, startOfDay, endOfDay);
            result.setId(null);
            result.setClassId(null);
            result.setClassName("All Classes");
        }

        List<StudentAttendance> studentAttendances = presentStudents.stream().map(student ->
        {
            StudentAttendance dto = new StudentAttendance();
            dto.setPanNumber(student.getPanNumber());
            dto.setPresent(true);
            return dto;
        }).collect(Collectors.toList());

        result.setDate(LocalDate.now());
        result.setStudentAttendances(studentAttendances);

        return result;
    }

    @Override
    public Student findByPanNumberIgnoreCaseAndSchool_IdAndStatusActive(String panNumber, Long schoolId)
    {
        return studentRepository.findByPanNumberIgnoreCaseAndSchool_IdAndStatusActive(panNumber, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found with PAN Number: " + panNumber +
                                " and ACTIVE status in school with ID: " + schoolId));
    }

    @Override
    public Student findByPanNumberIgnoreCaseAndSchool_IdAndStatusInactive(String panNumber, Long schoolId)
    {
        return studentRepository.findByPanNumberIgnoreCaseAndSchool_IdAndStatusInactive(panNumber, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found with PAN Number: " + panNumber +
                                " and ACTIVE status in school with ID: " + schoolId));
    }

    @Transactional
    @Override
    public void promoteStudentsToClass(List<String> panNumbers, Long classId, Long schoolId)
    {
        ClassEntity classEntity = classEntityRepository.findByIdAndSchoolIdAndSessionActive(classId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Class with ID " + classId +
                        " not found in School with ID " + schoolId + " or the session is not active."));

        Session session = classEntity.getSession();
        School school = classEntity.getSchool();

        List<String> skippedPans = new ArrayList<>();
        List<Student> studentsToUpdate = panNumbers.stream()
                .map(pan ->
                {
                    Optional<Student> optionalStudent = studentRepository.findByPanNumberIgnoreCaseAndSchool_IdAndStatusActive(pan, schoolId);
                    if (optionalStudent.isPresent())
                    {
                        return optionalStudent.get();
                    }
                    else
                    {
                        skippedPans.add(pan);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        log.info("Skipped PANs (inactive or not found): {}", skippedPans);

        List<StudentEnrollments> enrollments = new ArrayList<>();

        for (Student student : studentsToUpdate)
        {
            // Update student data
            student.setCurrentClass(classEntity);
            student.setSession(session);
            student.setClassRollNumber(getNextRollNumber(classId, schoolId));

            // Create new enrollment
            StudentEnrollments studentEnrollment = new StudentEnrollments();
            studentEnrollment.setStudent(student);
            studentEnrollment.setSchool(school);
            studentEnrollment.setClassEntity(classEntity);
            studentEnrollment.setSession(session);

            enrollments.add(studentEnrollment);
        }

        // Save all enrollments and updated students
        studentEnrollmentRepository.saveAll(enrollments);
        studentRepository.saveAll(studentsToUpdate);
    }


    // Private helper methods

    private User fetchUserByPan(String panNumber)
    {
        return userRepository.findByPanNumberIgnoreCase(panNumber).orElseThrow(() -> new ResourceNotFoundException("Student User not found with PAN: " + panNumber));
    }

    private ClassEntity fetchClassEntity(Long classId)
    {
        return EntityFetcher.fetchByIdOrThrow(classEntityRepository, classId, EntityNames.CLASS_ENTITY);
    }

    private Session fetchSession(Long sessionId)
    {
        return EntityFetcher.fetchByIdOrThrow(sessionRepository, sessionId, EntityNames.SESSION);
    }

    private void validateClassAndSession(ClassEntity classEntity, Session session)
    {
        // Check duplicate class for same name and session
        Optional<ClassEntity> duplicateClass = classEntityRepository.findByClassNameIgnoreCaseAndSessionId(classEntity.getClassName(), session.getId());

        if (duplicateClass.isPresent() && !duplicateClass.get().getId().equals(classEntity.getId()))
        {
            throw new AlreadyExistException("Class already exists with name: " + classEntity.getClassName() + " for the selected session.");
        }

        if (!session.isActive())
        {
            log.warn("Cannot add student to inactive session with ID {}", session.getId());
            throw new WrongArgumentException("Cannot add student to an inactive session");
        }
    }




    private Integer getNextRollNumber(Long classId, Long schoolId)
    {
        Integer maxRoll = studentRepository.findMaxClassRollNumberByCurrentClassIdAndSchoolId(classId, schoolId);
        return (maxRoll != null ? maxRoll : 0) + 1;
    }

    private void validateStudentIsActive(Student student)
    {
        if (!student.getStatus().equals(UserStatus.ACTIVE))
        {
            log.info("Update not allowed. Student with PAN {} has status: {}", student.getPanNumber(), student.getStatus());
            throw new WrongArgumentException("Only active students can be changed. Student with PAN " + student.getPanNumber() + " is currently " + student.getStatus().name().toLowerCase() + ".");
        }
    }

    private void validateSessionIsActive(Student student)
    {
        if (!student.getSession().isActive())
        {
            throw new WrongArgumentException("Cannot update student because the session is inactive");
        }
    }

    private StudentResponseDto convertToDto(Student student)
    {
        StudentResponseDto dto = modelMapper.map(student, StudentResponseDto.class);
        dto.setClassId(student.getCurrentClass().getId());
        dto.setSessionId(student.getSession().getId());
        dto.setSessionName(student.getSession().getName());
        return dto;
    }

    private void setFeeStatuses(StudentResponseDto dto, String pan, Long schoolId)
    {
        FeeCatalogDto feeCatalog = feeService.getFeeCatalogByStudentPanNumber(pan, schoolId);

        LocalDate today = LocalDate.now();
        Month currentMonth = today.getMonth();
        int currentYear = today.getYear();

        Month previousMonth = currentMonth.minus(1);
        int previousYear = currentMonth == Month.JANUARY ? currentYear - 1 : currentYear;

        Optional<MonthlyFeeDto> currentMonthFeeOpt = feeCatalog.getMonthlyFees().stream().filter(fee -> fee.getYear() == currentYear && fee.getMonth().equalsIgnoreCase(currentMonth.name())).findFirst();

        Optional<MonthlyFeeDto> previousMonthFeeOpt = feeCatalog.getMonthlyFees().stream().filter(fee -> fee.getYear() == previousYear && fee.getMonth().equalsIgnoreCase(previousMonth.name())).findFirst();

        if (currentMonthFeeOpt.isPresent() && "paid".equalsIgnoreCase(currentMonthFeeOpt.get().getStatus()))
        {
            dto.setFeeStatus(FeeStatus.PAID);
            dto.setFeeCatalogStatus(FeeCatalogStatus.UP_TO_DATE);
            return;
        }

        if (today.getDayOfMonth() <= 15)
        {
            if (previousMonthFeeOpt.isPresent() && "paid".equalsIgnoreCase(previousMonthFeeOpt.get().getStatus()))
            {
                dto.setFeeStatus(FeeStatus.PENDING);
                dto.setFeeCatalogStatus(FeeCatalogStatus.PENDING);
                return;
            }
            dto.setFeeStatus(FeeStatus.PENDING);
            dto.setFeeCatalogStatus(FeeCatalogStatus.PENDING);
            return;
        }
        else
        {
            if (currentMonthFeeOpt.isEmpty())
            {
                dto.setFeeStatus(FeeStatus.OVERDUE);
                dto.setFeeCatalogStatus(FeeCatalogStatus.OVERDUE);
                return;
            }

            if (previousMonthFeeOpt.isPresent() && "overdue".equalsIgnoreCase(previousMonthFeeOpt.get().getStatus()))
            {
                dto.setFeeStatus(FeeStatus.OVERDUE);
                dto.setFeeCatalogStatus(FeeCatalogStatus.OVERDUE);
                return;
            }

            if (currentMonthFeeOpt.isPresent() && !"paid".equalsIgnoreCase(currentMonthFeeOpt.get().getStatus()))
            {
                dto.setFeeStatus(FeeStatus.OVERDUE);
                dto.setFeeCatalogStatus(FeeCatalogStatus.OVERDUE);
                return;
            }
        }

        dto.setFeeStatus(FeeStatus.PENDING);
        dto.setFeeCatalogStatus(FeeCatalogStatus.PENDING);
    }
}
