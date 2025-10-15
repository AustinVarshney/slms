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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
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

    @Override
    public StudentResponseDto createStudent(StudentRequestDto studentRequestDto)
    {
        log.info("Attempting to create student with PAN: {}", studentRequestDto.getPanNumber());

        validateStudentDoesNotExist(studentRequestDto.getPanNumber());

        User user = fetchUserByPan(studentRequestDto.getPanNumber());
        ClassEntity classEntity = fetchClassEntity(studentRequestDto.getClassId());
        Session session = fetchSession(studentRequestDto.getSessionId());

        validateClassAndSession(classEntity, session);

        Student student = modelMapper.map(studentRequestDto, Student.class);
        student.setStatus(UserStatus.ACTIVE);
        
        // Get next available roll number that doesn't conflict with unique constraint
        Integer rollNumber = getNextAvailableRollNumber(studentRequestDto.getClassId(), session.getId());
        student.setClassRollNumber(rollNumber);
        
        student.setCurrentClass(classEntity);
        student.setUser(user);
        student.setSession(session);

        Student savedStudent = studentRepository.save(student);

        createAndSaveFeesForStudent(savedStudent, studentRequestDto.getClassId(), studentRequestDto.getSessionId(), session);

        log.info("Student created successfully with PAN: {}", studentRequestDto.getPanNumber());

        StudentResponseDto responseDto = convertToDto(savedStudent);
        responseDto.setFeeCatalogStatus(FeeCatalogStatus.PENDING);
        responseDto.setFeeStatus(FeeStatus.PENDING);

        return responseDto;
    }

    @Override
    public boolean existsByPanNumber(String panNumber)
    {
        return studentRepository.existsById(panNumber);
    }

    @Transactional
    @Override
    public void markStudentsGraduateOrInActive(List<String> panNumbers, UserStatus status)
    {
        List<Student> students = new ArrayList<>();
        List<User> users = new ArrayList<>();

        for (String pan : panNumbers)
        {
            Student student = EntityFetcher.fetchByIdOrThrow(studentRepository, pan, EntityNames.STUDENT);
            User user = student.getUser();

            if (user != null)
            {
                // Enable user only if status is ACTIVE, disable for INACTIVE/GRADUATED
                user.setEnabled(status == UserStatus.ACTIVE);
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
        
        // Verify class exists
        if (!classEntityRepository.existsById(classId))
        {
            throw new ResourceNotFoundException("Class not found with ID: " + classId);
        }

        // Allow fetching students regardless of session status
        // Removed session active check to allow viewing historical data
        
        List<Student> activeStudents = studentRepository.findByStatusAndCurrentClass_Id(UserStatus.ACTIVE, classId);

        return activeStudents.stream()
                .map(student ->
                {
                    StudentResponseDto dto = convertToDto(student);
                    setFeeStatuses(dto, student.getPanNumber());
                    return dto;
                })
                .collect(Collectors.toList());
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
                .collect(Collectors.toList());
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
                .collect(Collectors.toList());
    }

    @Override
    public StudentResponseDto getStudentByPAN(String pan)
    {
        log.info("Fetching student with PAN: {}", pan);
        Student fetchedStudent = EntityFetcher.fetchStudentByPan(studentRepository, pan);

        StudentResponseDto dto = convertToDto(fetchedStudent);
        setFeeStatuses(dto, pan);

        log.info("Student fetched successfully with PAN: {}", pan);
        return dto;
    }

    @Override
    public StudentResponseDto updateStudent(String pan, UpdateStudentInfo updateStudentInfo)
    {
        log.info("Updating student with PAN: {}", pan);

        Student fetchedStudent = EntityFetcher.fetchStudentByPan(studentRepository, pan);

        validateStudentIsActive(fetchedStudent);
        validateSessionIsActive(fetchedStudent);

        // Handle class change if className or classId is provided
        if (updateStudentInfo.getClassId() != null || updateStudentInfo.getClassName() != null)
        {
            ClassEntity newClass = null;
            
            if (updateStudentInfo.getClassId() != null)
            {
                // Direct class ID provided
                newClass = EntityFetcher.fetchByIdOrThrow(classEntityRepository, updateStudentInfo.getClassId(), EntityNames.CLASS_ENTITY);
            }
            else if (updateStudentInfo.getClassName() != null)
            {
                // Class name provided (format: "1-A", "10-B")
                newClass = classEntityRepository.findByClassNameAndSession_Active(updateStudentInfo.getClassName(), true)
                        .orElseThrow(() -> new ResourceNotFoundException("Active class not found with name: " + updateStudentInfo.getClassName()));
            }
            
            if (newClass != null)
            {
                // Verify the new class belongs to the same active session
                if (!newClass.getSession().isActive())
                {
                    throw new IllegalStateException("Cannot move student to inactive session");
                }
                
                // Check if class actually changed
                boolean classChanged = !fetchedStudent.getCurrentClass().getId().equals(newClass.getId());
                
                // Update class and get new roll number
                fetchedStudent.setCurrentClass(newClass);
                fetchedStudent.setClassRollNumber(getNextRollNumber(newClass.getId()));
                
                log.info("Student class updated from {} to {} with new roll number: {}", 
                         fetchedStudent.getCurrentClass().getClassName(), 
                         newClass.getClassName(), 
                         fetchedStudent.getClassRollNumber());
                
                // Save student first to persist class change
                // Then regenerate fees for new class if class actually changed
                if (classChanged)
                {
                    log.info("Class changed for student {}. Will regenerate fee structure after saving...", pan);
                    // Mark that we need to regenerate fees after saving
                    // This will be handled after the student is saved
                }
            }
        }

        // Map other fields (excluding className and classId as they're already handled)
        modelMapper.map(updateStudentInfo, fetchedStudent);
        fetchedStudent.setPanNumber(pan);

        Student updatedStudent = studentRepository.save(fetchedStudent);
        
        // Regenerate fees AFTER saving the student with new class
        if (updateStudentInfo.getClassId() != null || updateStudentInfo.getClassName() != null)
        {
            ClassEntity currentClass = updatedStudent.getCurrentClass();
            if (currentClass != null)
            {
                // Check if we need to regenerate fees by checking if student was previously in different class
                // We'll regenerate fees to ensure consistency
                log.info("Regenerating fee structure for student {} in class {}...", pan, currentClass.getClassName());
                try {
                    // Delete old fees
                    feeRepository.deleteByStudent_PanNumber(pan);
                    feeRepository.flush(); // Ensure delete is committed
                    log.info("Deleted old fees for student: {}", pan);
                    
                    // Generate new fees based on new class
                    feeService.generateFeesForStudent(pan);
                    log.info("Generated new fees for student: {} based on class: {}", pan, currentClass.getClassName());
                } catch (Exception e) {
                    log.error("Failed to regenerate fees for student: {}", pan, e);
                    // Don't fail the entire update if fee regeneration fails
                }
            }
        }

        log.info("Student updated successfully with PAN: {}", pan);

        StudentResponseDto dto = convertToDto(updatedStudent);
        setFeeStatuses(dto, pan);

        return dto;
    }

    @Override
    public CurrentDayAttendance getStudentsPresentToday(Optional<Long> classId)
    {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        List<Student> presentStudents;
        CurrentDayAttendance result = new CurrentDayAttendance();

        if (classId.isPresent())
        {
            Long id = classId.get();
            ClassEntity classEntity = classEntityRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Class not found with ID: " + id));

            if (!classEntity.getSession().isActive())
            {
                throw new ResourceNotFoundException("Session is not active for class ID: " + id);
            }

            presentStudents = studentRepository.findStudentsPresentTodayByClassId(id, startOfDay, endOfDay);

            result.setId(classEntity.getId());
            result.setClassId(classEntity.getId());
            result.setClassName(classEntity.getClassName());
        }
        else
        {
            presentStudents = studentRepository.findStudentsPresentToday(startOfDay, endOfDay);
            result.setId(null);
            result.setClassId(null);
            result.setClassName("All Classes");
        }

        List<StudentAttendance> studentAttendances = presentStudents.stream()
                .map(student ->
                {
                    StudentAttendance dto = new StudentAttendance();
                    dto.setPanNumber(student.getPanNumber());
                    dto.setPresent(true);
                    return dto;
                })
                .collect(Collectors.toList());

        result.setDate(LocalDate.now());
        result.setStudentAttendances(studentAttendances);

        return result;
    }

    // Overloaded methods for convenience
    public List<StudentAttendance> getStudentsPresentToday()
    {
        log.info("Fetching today's present students");
        return studentRepository.findStudentsPresentToday().stream()
                .map(student -> modelMapper.map(student, StudentAttendance.class))
                .collect(Collectors.toList());
    }

    public CurrentDayAttendance getStudentsPresentTodayByClass(Long classId)
    {
        log.info("Fetching today's present students for class ID: {}", classId);

        ClassEntity classEntity = EntityFetcher.fetchClassEntityByClassId(classEntityRepository, classId);

        if (!classEntity.getSession().isActive())
        {
            throw new ResourceNotFoundException("Session is not active for the class ID: " + classId);
        }

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        List<Student> presentStudents = studentRepository.findStudentsPresentTodayByClassId(classId, startOfDay, endOfDay);

        List<StudentAttendance> studentAttendances = presentStudents.stream()
                .map(student ->
                {
                    StudentAttendance dto = new StudentAttendance();
                    dto.setPanNumber(student.getPanNumber());
                    dto.setPresent(true);
                    return dto;
                })
                .collect(Collectors.toList());

        CurrentDayAttendance result = new CurrentDayAttendance();
        result.setId(classEntity.getId());
        result.setClassId(classEntity.getId());
        result.setClassName(classEntity.getClassName());
        result.setDate(LocalDate.now());
        result.setStudentAttendances(studentAttendances);

        return result;
    }

    // Private helper methods

    private void validateStudentDoesNotExist(String panNumber)
    {
        if (studentRepository.existsById(panNumber))
        {
            throw new AlreadyExistException("Student already exists with PAN: " + panNumber);
        }
    }

    private User fetchUserByPan(String panNumber)
    {
        return userRepository.findByPanNumberIgnoreCase(panNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Student User not found with PAN: " + panNumber));
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
        Optional<ClassEntity> duplicateClass = classEntityRepository
                .findByClassNameIgnoreCaseAndSessionId(classEntity.getClassName(), session.getId());

        if (duplicateClass.isPresent() && !duplicateClass.get().getId().equals(classEntity.getId()))
        {
            throw new AlreadyExistException("Class already exists with name: " + classEntity.getClassName()
                    + " for the selected session.");
        }

        if (!session.isActive())
        {
            log.warn("Cannot add student to inactive session with ID {}", session.getId());
            throw new WrongArgumentException("Cannot add student to an inactive session");
        }
    }

    private Integer getNextRollNumber(Long classId)
    {
        Integer maxRoll = studentRepository.findMaxClassRollNumberByCurrentClassId(classId);
        return (maxRoll != null ? maxRoll : 0) + 1;
    }

    /**
     * Get next available roll number considering the unique constraint on (class_roll_number, class_id, session_id)
     * This method checks for existing students with the same class and session to avoid conflicts
     */
    private Integer getNextAvailableRollNumber(Long classId, Long sessionId)
    {
        // Find all students in this class and session
        List<Student> existingStudents = studentRepository.findByCurrentClass_Id(classId).stream()
                .filter(s -> s.getSession() != null && s.getSession().getId().equals(sessionId))
                .toList();
        
        if (existingStudents.isEmpty())
        {
            return 1;
        }
        
        // Find the maximum roll number
        Integer maxRoll = existingStudents.stream()
                .map(Student::getClassRollNumber)
                .filter(roll -> roll != null)
                .max(Integer::compareTo)
                .orElse(0);
        
        return maxRoll + 1;
    }

    private void createAndSaveFeesForStudent(Student student, Long classId, Long sessionId, Session session)
    {
        FeeStructure feeStructure = feeStructureRepository
                .findByClassEntity_IdAndSession_Id(classId, sessionId)
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
            fee.setStudent(student);

            feeEntries.add(fee);
            currentMonth = currentMonth.plusMonths(1);
        }

        feeRepository.saveAll(feeEntries);
    }

    private void validateStudentIsActive(Student student)
    {
        if (!student.getStatus().equals(UserStatus.ACTIVE))
        {
            log.info("Update not allowed. Student with PAN {} has status: {}", student.getPanNumber(), student.getStatus());
            throw new WrongArgumentException("Only active students can be changed. Student with PAN "
                    + student.getPanNumber() + " is currently " + student.getStatus().name().toLowerCase() + ".");
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
        
        // Parse className (e.g., "1-A") into currentClass ("1") and section ("A")
        String className = student.getCurrentClass().getClassName();
        if (className != null && className.contains("-")) {
            String[] parts = className.split("-");
            dto.setCurrentClass(parts[0]); // e.g., "1", "10"
            dto.setSection(parts.length > 1 ? parts[1] : "A"); // e.g., "A", "B"
        } else {
            // If no section in className, use className as currentClass
            dto.setCurrentClass(className != null ? className : "N/A");
            dto.setSection("A"); // Default section
        }
        
        return dto;
    }

    private void setFeeStatuses(StudentResponseDto dto, String pan)
    {
        FeeCatalogDto feeCatalog = feeService.getFeeCatalogByStudentPanNumber(pan);

        List<MonthlyFeeDto> allFees = feeCatalog.getMonthlyFees();
        
        // If no fees exist, set as PENDING
        if (allFees == null || allFees.isEmpty())
        {
            dto.setFeeStatus(FeeStatus.PENDING);
            dto.setFeeCatalogStatus(FeeCatalogStatus.PENDING);
            return;
        }

        // Check if ALL fees are paid
        boolean allFeesPaid = allFees.stream()
                .allMatch(fee -> "paid".equalsIgnoreCase(fee.getStatus()));
        
        // Check if ANY fee is overdue
        boolean anyFeeOverdue = allFees.stream()
                .anyMatch(fee -> "overdue".equalsIgnoreCase(fee.getStatus()));

        // If all fees are paid, mark as PAID
        if (allFeesPaid)
        {
            dto.setFeeStatus(FeeStatus.PAID);
            dto.setFeeCatalogStatus(FeeCatalogStatus.UP_TO_DATE);
            return;
        }

        // If any fee is overdue, mark as OVERDUE
        if (anyFeeOverdue)
        {
            dto.setFeeStatus(FeeStatus.OVERDUE);
            dto.setFeeCatalogStatus(FeeCatalogStatus.OVERDUE);
            return;
        }

        // Otherwise, mark as PENDING
        dto.setFeeStatus(FeeStatus.PENDING);
        dto.setFeeCatalogStatus(FeeCatalogStatus.PENDING);
    }
}
