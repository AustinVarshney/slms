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
import java.util.*;
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
    private final ScoreRepository scoreRepository;
    private final AttendanceRepository attendanceRepository;

    @Override
    public StudentResponseDto createStudent(StudentRequestDto studentRequestDto, Long schoolId)
    {
        log.info("Attempting to create student with PAN: {}", studentRequestDto.getPanNumber());

        School school = schoolRepository.findById(schoolId).orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + schoolId));
        
        User user = fetchUserByPan(studentRequestDto.getPanNumber());
        ClassEntity classEntity = classEntityRepository
                .findByIdAndSchoolIdAndSessionActive(studentRequestDto.getClassId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Class with ID " + studentRequestDto.getClassId() +
                        " not found in School with ID " + schoolId + " or the session is not active."));

        Session session = classEntity.getSession();
        
        // Check if student already exists in THIS session and school (not globally)
        Optional<Student> existingStudent = studentRepository.findByPanNumberIgnoreCaseAndSchoolIdAndSessionId(
            studentRequestDto.getPanNumber(), 
            schoolId, 
            session.getId()
        );
        
        if (existingStudent.isPresent())
        {
            throw new AlreadyExistException("A student with PAN number '" + studentRequestDto.getPanNumber() + 
                    "' already exists in this school for the current session.");
        }

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
        
        // Explicitly set admission date if provided
        if (studentRequestDto.getAdmissionDate() != null) {
            student.setAdmissionDate(studentRequestDto.getAdmissionDate());
        } else {
            // Set to current date if not provided
            student.setAdmissionDate(LocalDate.now());
        }

        Student savedStudent = studentRepository.save(student);

        log.info("Generating 12-month fee structure for student: {} in class: {} for session: {}", 
                savedStudent.getPanNumber(), classEntity.getClassName(), session.getName());

        FeeStructure feeStructure = feeStructureRepository
                .findByClassEntity_IdAndSession_IdAndSchool_Id(
                        studentRequestDto.getClassId(), session.getId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Fee structure not found for class " + classEntity.getClassName() + 
                        " and session " + session.getName() + " in school ID: " + schoolId));

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
            fee.setClassEntity(classEntity);
            fee.setDueDate(currentMonth.withDayOfMonth(10)); // Due on 10th of each month
            fee.setStudent(savedStudent);
            fee.setSchool(school);
            fee.setSession(session); // Link fee to session

            feeEntries.add(fee);
            currentMonth = currentMonth.plusMonths(1);
        }

        feeRepository.saveAll(feeEntries);
        log.info("Successfully generated {} fee records for student: {}", feeEntries.size(), savedStudent.getPanNumber());

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

    @Override
    public boolean existsByPanNumber(String panNumber)
    {
        return studentRepository.existsById(panNumber);
    }

    @Transactional
    @Override
    public void markStudentsGraduateOrInActive(List<String> panNumbers, UserStatus status, Long schoolId)
    {
        List<Student> students = new ArrayList<>();
//        List<User> users = new ArrayList<>();

        for (String pan : panNumbers)
        {
            // FIXED: Use findByPanNumberIgnoreCaseAndSchool_Id instead of ...AndStatusActive
            // This allows changing status of INACTIVE/GRADUATED students back to ACTIVE
            Student student = studentRepository.findByPanNumberIgnoreCaseAndSchool_Id(pan, schoolId)
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

        // Store original class ID for comparison
        Long originalClassId = fetchedStudent.getCurrentClass() != null ? fetchedStudent.getCurrentClass().getId() : null;

        // Map basic fields
        modelMapper.map(updateStudentInfo, fetchedStudent);
        fetchedStudent.setPanNumber(pan);

        // Handle class update - prioritize classId over className
        boolean classWillChange = false;
        Long newClassId = null;
        
        if (updateStudentInfo.getClassId() != null)
        {
            ClassEntity classEntity = classEntityRepository.findByIdAndSchoolIdAndSessionActive(updateStudentInfo.getClassId(), schoolId)
                    .orElseThrow(() -> new ResourceNotFoundException("Class with ID " + updateStudentInfo.getClassId() + 
                            " not found in School with ID " + schoolId + " or the session is not active."));
            
            newClassId = classEntity.getId();
            classWillChange = !newClassId.equals(originalClassId);
            
            fetchedStudent.setCurrentClass(classEntity);
            log.info("Updated student class to: {}", classEntity.getClassName());
            
            // Auto-assign new roll number if class changed
            if (classWillChange)
            {
                Integer newRollNumber = getNextRollNumber(newClassId, schoolId);
                fetchedStudent.setClassRollNumber(newRollNumber);
                log.info("Class changed - assigned new roll number {} for class ID {}", newRollNumber, newClassId);
                
                // Reassign roll numbers in the old class after this student is moved
                if (originalClassId != null) {
                    log.info("Reassigning roll numbers in old class ID: {}", originalClassId);
                }
            }
        }
        else if (updateStudentInfo.getClassName() != null && !updateStudentInfo.getClassName().isEmpty())
        {
            // Parse className format "1-A" to get class and section
            String className = updateStudentInfo.getClassName();
            Session activeSession = sessionRepository.findBySchoolIdAndActiveTrue(schoolId)
                    .orElseThrow(() -> new ResourceNotFoundException("No active session found for school ID: " + schoolId));
            
            // Use simple class name without session concatenation
            String simpleClassName = className.trim();
            log.info("Searching for class with name: {}", simpleClassName);
            
            ClassEntity classEntity = classEntityRepository.findByClassNameIgnoreCaseAndSessionIdAndSchoolId(simpleClassName, activeSession.getId(), schoolId)
                    .orElseThrow(() -> new ResourceNotFoundException("Class " + simpleClassName + " not found in active session for school ID: " + schoolId));
            
            newClassId = classEntity.getId();
            classWillChange = !newClassId.equals(originalClassId);
            
            fetchedStudent.setCurrentClass(classEntity);
            log.info("Updated student class to: {} (ID: {})", classEntity.getClassName(), classEntity.getId());
            
            // Auto-assign new roll number if class changed
            if (classWillChange)
            {
                Integer newRollNumber = getNextRollNumber(newClassId, schoolId);
                fetchedStudent.setClassRollNumber(newRollNumber);
                log.info("Class changed - assigned new roll number {} for class {}", newRollNumber, simpleClassName);
                
                // Reassign roll numbers in the old class after this student is moved
                if (originalClassId != null) {
                    log.info("Reassigning roll numbers in old class ID: {}", originalClassId);
                }
            }
        }

        Student updatedStudent = studentRepository.save(fetchedStudent);
        
        // Update class_id in all related tables when class changes
        if (classWillChange && newClassId != null && originalClassId != null) {
            log.info("Updating class_id in all related tables for student: {} from class {} to class {}", 
                    pan, originalClassId, newClassId);
            
            // Update Attendance records
            List<Attendance> attendanceRecords = attendanceRepository.findByStudentPanNumberAndSchoolId(pan, schoolId);
            if (!attendanceRecords.isEmpty()) {
                for (Attendance attendance : attendanceRecords) {
                    attendance.setClassEntity(updatedStudent.getCurrentClass());
                }
                attendanceRepository.saveAll(attendanceRecords);
                log.info("Updated {} attendance records for student: {}", attendanceRecords.size(), pan);
            }
            
            // Update Score records
            List<Score> scoreRecords = scoreRepository.findByStudentPanNumberAndSchoolId(pan, schoolId);
            if (!scoreRecords.isEmpty()) {
                for (Score score : scoreRecords) {
                    score.setClassEntity(updatedStudent.getCurrentClass());
                }
                scoreRepository.saveAll(scoreRecords);
                log.info("Updated {} score records for student: {}", scoreRecords.size(), pan);
            }
            
            // Get active session for filtering
            Session activeSession = fetchedStudent.getSession();
            
            // Update Fee records (after regeneration)
            List<Fee> feeRecords = feeRepository.findByStudentPanNumberAndSchoolIdOrderByYearAscMonthAsc(
                    pan, schoolId, activeSession.getId());
            if (!feeRecords.isEmpty()) {
                for (Fee fee : feeRecords) {
                    fee.setClassEntity(updatedStudent.getCurrentClass());
                }
                feeRepository.saveAll(feeRecords);
                log.info("Updated {} fee records for student: {}", feeRecords.size(), pan);
            }
        }
        
        // Reassign roll numbers in old class if class changed
        if (classWillChange && originalClassId != null) {
            reassignRollNumbers(originalClassId, schoolId);
        }
        
        // Regenerate fees if class changed
        if (classWillChange && newClassId != null)
        {
            ClassEntity currentClass = updatedStudent.getCurrentClass();
            Session activeSession = updatedStudent.getSession();
            
            log.info("Class changed from {} to {}. Regenerating fee structure for student {}...", 
                    originalClassId, newClassId, pan);
            
            // Use repository to find fee structure
            Optional<FeeStructure> newFeeStructureOpt = feeStructureRepository
                    .findByClassEntity_IdAndSession_IdAndSchool_Id(
                            currentClass.getId(), 
                            activeSession.getId(), 
                            schoolId);
            
            if (newFeeStructureOpt.isEmpty()) {
                log.warn("No fee structure found for class: {} in session: {}. Cannot regenerate fees.", 
                        currentClass.getClassName(), activeSession.getName());
            } else {
                FeeStructure newFeeStructure = newFeeStructureOpt.get();
                try {
                    // Delete old fees for this student in this session
                    List<Fee> existingFees = feeRepository.findByStudentPanNumberAndSchoolIdOrderByYearAscMonthAsc(
                            pan, schoolId, activeSession.getId());
                    
                    // Delete all existing fees to regenerate from scratch
                    if (!existingFees.isEmpty()) {
                        feeRepository.deleteAll(existingFees);
                        feeRepository.flush();
                        log.info("Deleted {} existing fees for student: {}", existingFees.size(), pan);
                    }
                    
                    // Generate new fees based on new class fee structure
                    List<Fee> newFeeEntries = new ArrayList<>();
                    LocalDate currentMonth = activeSession.getStartDate().withDayOfMonth(1);
                    
                    for (int i = 0; i < 12; i++)
                    {
                        Fee fee = new Fee();
                        fee.setMonth(FeeMonth.valueOf(currentMonth.getMonth().toString()));
                        fee.setYear(currentMonth.getYear());
                        fee.setStatus(FeeStatus.PENDING);
                        fee.setAmount(newFeeStructure.getFeesAmount());
                        fee.setFeeStructure(newFeeStructure);
                        fee.setClassEntity(currentClass);
                        fee.setDueDate(currentMonth.withDayOfMonth(10)); // Due on 10th
                        fee.setStudent(updatedStudent);
                        fee.setSchool(updatedStudent.getSchool());
                        fee.setSession(activeSession); // Link fee to session

                        newFeeEntries.add(fee);
                        currentMonth = currentMonth.plusMonths(1);
                    }
                    
                    feeRepository.saveAll(newFeeEntries);
                    feeRepository.flush();
                    log.info("Generated {} new fees for student: {} based on class: {} with fee amount: {}", 
                            newFeeEntries.size(), pan, currentClass.getClassName(), newFeeStructure.getFeesAmount());
                } catch (Exception e) {
                    log.error("Failed to regenerate fees for student: {}", pan, e);
                    // Don't fail the entire update if fee regeneration fails
                }
            }
        }

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
        
        // Set school information
        School school = student.getSchool();
        if (school != null) {
            dto.setSchoolName(school.getSchoolName());
            dto.setSchoolLogo(school.getSchoolLogo());
            dto.setSchoolTagline(school.getSchoolTagline());
        }
        
        // Set class teacher information
        ClassEntity currentClass = student.getCurrentClass();
        if (currentClass != null && currentClass.getClassTeacher() != null) {
            dto.setClassTeacherId(currentClass.getClassTeacher().getId());
            dto.setClassTeacherName(currentClass.getClassTeacher().getName());
        }
        
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

    private void setFeeStatuses(StudentResponseDto dto, String pan, Long schoolId)
    {
        FeeCatalogDto feeCatalog = feeService.getFeeCatalogByStudentPanNumber(pan, schoolId);

        LocalDate today = LocalDate.now();

        // Check if any fee is overdue (unpaid and past due date)
        boolean anyFeeOverdue = feeCatalog.getMonthlyFees().stream()
                .anyMatch(fee -> {
                    if ("pending".equalsIgnoreCase(fee.getStatus()) || "unpaid".equalsIgnoreCase(fee.getStatus()))
                    {
                        LocalDate dueDate = fee.getDueDate();
                        return dueDate != null && dueDate.isBefore(today);
                    }
                    return "overdue".equalsIgnoreCase(fee.getStatus());
                });

        // If any fee is overdue, mark student status as OVERDUE
        if (anyFeeOverdue)
        {
            dto.setFeeStatus(FeeStatus.OVERDUE);
            dto.setFeeCatalogStatus(FeeCatalogStatus.OVERDUE);
            
            // Update student's status in database to reflect overdue status
            Student student = studentRepository.findByPanNumberIgnoreCaseAndSchool_Id(pan, schoolId).orElse(null);
            if (student != null && student.getStatus() == UserStatus.ACTIVE)
            {
                // Note: We'll keep the UserStatus as ACTIVE but show OVERDUE in fee status
                // This allows student to still login but shows they have overdue fees
                log.info("Student {} has overdue fees", pan);
            }
            return;
        }

        // Check if ALL fees for the year are paid (all 12 months)
        long totalMonthsFees = feeCatalog.getMonthlyFees().size();
        long paidMonthsFees = feeCatalog.getMonthlyFees().stream()
                .filter(fee -> "paid".equalsIgnoreCase(fee.getStatus()))
                .count();

        // PAID status: Only when ALL fees for the complete year are paid
        if (totalMonthsFees == 12 && paidMonthsFees == 12)
        {
            dto.setFeeStatus(FeeStatus.PAID);
            dto.setFeeCatalogStatus(FeeCatalogStatus.UP_TO_DATE);
            return;
        }

        // Otherwise, mark as PENDING (even if overdues are cleared, but not all months paid)
        dto.setFeeStatus(FeeStatus.PENDING);
        dto.setFeeCatalogStatus(FeeCatalogStatus.PENDING);
    }

    @Override
    public List<PreviousSchoolingRecordDto> getPreviousSchoolingRecords(String panNumber, Long schoolId)
    {
        log.info("Fetching previous schooling records for PAN: {}", panNumber);
        
        // Get all enrollments for this student (across all sessions)
        List<StudentEnrollments> enrollments = studentEnrollmentRepository
                .findByStudent_PanNumberIgnoreCaseAndStudent_School_IdOrderBySession_StartYearDesc(panNumber, schoolId);
        
        if (enrollments.isEmpty())
        {
            log.warn("No enrollment records found for PAN: {}", panNumber);
            return new ArrayList<>();
        }
        
        List<PreviousSchoolingRecordDto> records = new ArrayList<>();
        
        for (StudentEnrollments enrollment : enrollments)
        {
            // Skip current active session - we only want history
            if (enrollment.getSession().isActive())
            {
                continue;
            }
            
            PreviousSchoolingRecordDto record = buildSchoolingRecord(enrollment, schoolId);
            records.add(record);
        }
        
        log.info("Found {} previous schooling records for PAN: {}", records.size(), panNumber);
        return records;
    }
    
    private PreviousSchoolingRecordDto buildSchoolingRecord(StudentEnrollments enrollment, Long schoolId)
    {
        Student student = enrollment.getStudent();
        Session session = enrollment.getSession();
        ClassEntity classEntity = enrollment.getClassEntity();
        
        // Parse className for class and section
        String className = classEntity.getClassName();
        String classLabel = className;
        String section = "A";
        if (className != null && className.contains("-"))
        {
            String[] parts = className.split("-");
            classLabel = parts[0];
            section = parts.length > 1 ? parts[1] : "A";
        }
        
        // Calculate attendance
        List<Attendance> attendanceRecords = attendanceRepository
                .findByStudentPanNumberAndSessionId(student.getPanNumber(), session.getId());
        
        int totalPresent = 0;
        int totalAbsent = 0;
        for (Attendance attendance : attendanceRecords)
        {
            if (attendance.isPresent())
            {
                totalPresent++;
            }
            else
            {
                totalAbsent++;
            }
        }
        
        int totalDays = totalPresent + totalAbsent;
        double attendancePercentage = totalDays > 0 ? (totalPresent * 100.0) / totalDays : 0.0;
        
        // Get all scores for this student
        List<Score> allScores = scoreRepository.findByStudentPanNumber(student.getPanNumber());
        
        // Filter scores for THIS SPECIFIC SESSION ONLY (not current session)
        // We need to check if the exam's classExam's classEntity's session matches this enrollment's session
        List<Score> sessionScores = allScores.stream()
                .filter(score -> score.getExam() != null 
                        && score.getExam().getClassExam() != null
                        && score.getExam().getClassExam().getClassEntity() != null
                        && score.getExam().getClassExam().getClassEntity().getSession() != null
                        && score.getExam().getClassExam().getClassEntity().getSession().getId().equals(session.getId()))
                .collect(Collectors.toList());
        
        // Filter scores for this session (simple approach - get all scores and calculate)
        List<PreviousSchoolingRecordDto.ExamResultSummary> examResults = new ArrayList<>();
        double totalPercentage = 0.0;
        int examCount = 0;
        
        // Group scores by exam
        Map<Long, List<Score>> scoresByExam = sessionScores.stream()
                .filter(score -> score.getExam() != null)
                .collect(Collectors.groupingBy(score -> score.getExam().getId()));
        
        for (Map.Entry<Long, List<Score>> entry : scoresByExam.entrySet())
        {
            List<Score> examScores = entry.getValue();
            if (examScores.isEmpty()) continue;
            
            Exam exam = examScores.get(0).getExam();
            
            // Calculate total marks for this exam
            int obtainedMarks = 0;
            int totalMarks = 0;
            int subjectCount = 0;
            
            // Get the maximum marks from ClassExam
            Double maxMarksPerSubject = exam.getClassExam() != null && exam.getClassExam().getMaxMarks() != null 
                ? exam.getClassExam().getMaxMarks().doubleValue()
                : (exam.getMaximumMarks() != null ? exam.getMaximumMarks() : 100.0);
            
            for (Score score : examScores)
            {
                if (score.getMarks() != null)
                {
                    obtainedMarks += score.getMarks().intValue();
                    subjectCount++;
                }
            }
            
            // Calculate total marks based on actual max marks per subject
            totalMarks = (int) (subjectCount * maxMarksPerSubject);
            
            if (totalMarks > 0)
            {
                double percentage = (obtainedMarks * 100.0) / totalMarks;
                String grade = calculateGradeFromPercentage(percentage);
                
                PreviousSchoolingRecordDto.ExamResultSummary examSummary = PreviousSchoolingRecordDto.ExamResultSummary.builder()
                        .examName(exam.getName())
                        .examDate(null) // Exam doesn't have date in this model
                        .percentage(percentage)
                        .grade(grade)
                        .obtainedMarks(obtainedMarks)
                        .totalMarks(totalMarks)
                        .build();
                
                examResults.add(examSummary);
                totalPercentage += percentage;
                examCount++;
            }
        }
        
        // Calculate overall percentage (average of all exams)
        double overallPercentage = examCount > 0 ? totalPercentage / examCount : 0.0;
        String overallGrade = calculateGradeFromPercentage(overallPercentage);
        
        // Determine status
        String status = "COMPLETED";
        if (student.getStatus() == UserStatus.GRADUATED)
        {
            status = "COMPLETED";
        }
        else if (student.getStatus() == UserStatus.INACTIVE)
        {
            status = "TRANSFERRED";
        }
        
        return PreviousSchoolingRecordDto.builder()
                .sessionId(session.getId())
                .sessionName(session.getName())
                .className(classLabel)
                .section(section)
                .passingYear(session.getEndDate() != null ? session.getEndDate().getYear() : null)
                .status(status)
                .overallPercentage(overallPercentage)
                .overallGrade(overallGrade)
                .totalPresent(totalPresent)
                .totalAbsent(totalAbsent)
                .attendancePercentage(attendancePercentage)
                .examResults(examResults)
                .build();
    }
    
    private String calculateGradeFromPercentage(double percentage)
    {
        if (percentage >= 90) return "A+";
        if (percentage >= 80) return "A";
        if (percentage >= 70) return "B+";
        if (percentage >= 60) return "B";
        if (percentage >= 50) return "C";
        if (percentage >= 40) return "D";
        return "F";
    }

    @Override
    @Transactional
    public void reassignRollNumbers(Long classId, Long schoolId)
    {
        log.info("Reassigning roll numbers for class ID: {} in school ID: {}", classId, schoolId);
        
        // Get all active students in this class, ordered by current roll number
        List<Student> students = studentRepository.findByCurrentClassIdAndSchoolIdAndStatusActive(classId, schoolId);
        
        if (students.isEmpty()) {
            log.info("No students found in class ID: {}", classId);
            return;
        }
        
        // Sort by existing roll number to maintain order
        students.sort((s1, s2) -> {
            Integer roll1 = s1.getClassRollNumber() != null ? s1.getClassRollNumber() : 0;
            Integer roll2 = s2.getClassRollNumber() != null ? s2.getClassRollNumber() : 0;
            return roll1.compareTo(roll2);
        });
        
        // Reassign roll numbers sequentially starting from 1
        int rollNumber = 1;
        for (Student student : students) {
            student.setClassRollNumber(rollNumber++);
        }
        
        studentRepository.saveAll(students);
        log.info("Reassigned roll numbers for {} students in class ID: {}", students.size(), classId);
    }

    @Override
    @Transactional
    public void assignRollNumbersAlphabetically(Long classId, Long schoolId)
    {
        log.info("Assigning roll numbers alphabetically for class ID: {} in school ID: {}", classId, schoolId);
        
        // Get all active students in this class
        List<Student> students = studentRepository.findByCurrentClassIdAndSchoolIdAndStatusActive(classId, schoolId);
        
        if (students.isEmpty()) {
            log.info("No students found in class ID: {}", classId);
            return;
        }
        
        // Sort students alphabetically by name
        students.sort((s1, s2) -> {
            String name1 = s1.getName() != null ? s1.getName().toLowerCase() : "";
            String name2 = s2.getName() != null ? s2.getName().toLowerCase() : "";
            return name1.compareTo(name2);
        });
        
        // Assign roll numbers sequentially starting from 1
        int rollNumber = 1;
        for (Student student : students) {
            student.setClassRollNumber(rollNumber++);
        }
        
        studentRepository.saveAll(students);
        log.info("Assigned roll numbers alphabetically for {} students in class ID: {}", students.size(), classId);
    }

    @Override
    @Transactional
    public void swapRollNumbers(String panNumber1, String panNumber2, Long schoolId)
    {
        log.info("Swapping roll numbers between students: {} and {} in school ID: {}", panNumber1, panNumber2, schoolId);
        
        // Find both students
        Student student1 = studentRepository.findByPanNumberIgnoreCaseAndSchool_IdAndStatusActive(panNumber1, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with PAN: " + panNumber1));
        
        Student student2 = studentRepository.findByPanNumberIgnoreCaseAndSchool_IdAndStatusActive(panNumber2, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with PAN: " + panNumber2));
        
        // Verify they are in the same class
        if (!student1.getCurrentClass().getId().equals(student2.getCurrentClass().getId())) {
            throw new WrongArgumentException("Students must be in the same class to swap roll numbers");
        }
        
        // Swap roll numbers using temporary value to avoid unique constraint violation
        Integer roll1 = student1.getClassRollNumber();
        Integer roll2 = student2.getClassRollNumber();
        
        // Set student1 to negative temp value
        student1.setClassRollNumber(-9999);
        studentRepository.save(student1);
        studentRepository.flush(); // Force DB update
        
        // Set student2 to student1's original roll number
        student2.setClassRollNumber(roll1);
        studentRepository.save(student2);
        studentRepository.flush(); // Force DB update
        
        // Set student1 to student2's original roll number
        student1.setClassRollNumber(roll2);
        studentRepository.save(student1);
        
        log.info("Successfully swapped roll numbers: {} <-> {}", roll2, roll1);
    }
}
